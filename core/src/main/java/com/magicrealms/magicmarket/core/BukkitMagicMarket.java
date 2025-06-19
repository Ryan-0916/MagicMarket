package com.magicrealms.magicmarket.core;

import com.magicrealms.magiclib.bukkit.manage.BungeeMessageManager;
import com.magicrealms.magiclib.bukkit.manage.CommandManager;
import com.magicrealms.magiclib.bukkit.manage.ConfigManager;
import com.magicrealms.magiclib.bukkit.manage.PacketManager;
import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.store.MongoDBStore;
import com.magicrealms.magiclib.common.store.RedisStore;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.dispatcher.MessageDispatcher;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.MagicMarket;
import com.magicrealms.magicmarket.api.MagicMarketAPI;
import com.magicrealms.magicmarket.core.blacklist.BlackListManager;
import com.magicrealms.magicmarket.core.category.CategoryManager;
import com.magicrealms.magicmarket.core.product.ProductManager;
import com.magicrealms.magicmarket.core.repositoy.ProductRepository;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Optional;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.*;

/**
 * @author Ryan-0916
 * @Desc 全球市场
 * @date 2025-06-09
 */
public class BukkitMagicMarket extends MagicMarket {

    @Getter
    private static BukkitMagicMarket instance;

    @Getter
    private BungeeMessageManager bungeeMessageManager;

    public BukkitMagicMarket() {
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        dependenciesCheck(() -> {
            /* 加载配置文件 */
            loadConfig(configManager);
            /* 注册指令 */
            registerCommand(commandManager);
            /* 注册数据包监听器 */
            registerPacketListener(packetManager);
            /* 初始化 Redis */
            setupRedisStore();
            /* 初始化 MongoDB */
            setupMongoDB();
            /* 初始化 Repository*/
            setupRepository();
            /* 初始化分类器*/
            setupCategoryManager();
            /* 初始化黑名单管理器 */
            setupBlacklistManager();
            /* 初始化商品管理器 */
            setupProductManager();
            /* 初始化 API */
            setupAPI();
        });
    }

    public void setupBlacklistManager() {
        this.blacklistManager = new BlackListManager(this);
    }

    public void setupRepository() {
        this.productRepository = new ProductRepository(this.mongoDBStore, this.redisStore);
    }

    private void setupProductManager() {
        this.productManager = new ProductManager(this);
    }

    public void setupCategoryManager() {
        this.categoryManager = new CategoryManager(this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        unsubscribe();
    }

    public void setupRedisStore() {
        String host = getConfigManager().getYmlValue(YML_REDIS, "DataSource.Host"), password = getConfigManager().getYmlValue(YML_REDIS, "DataSource.Password");
        int port = getConfigManager().getYmlValue(YML_REDIS, "DataSource.Port", 6379, ParseType.INTEGER);
        boolean redisPasswordModel = getConfigManager().getYmlValue(YML_REDIS, "DataSource.PasswordModel", false, ParseType.BOOLEAN);
        /* ItemUtil GSON 的目的是为了让 Gson 兼容 ItemStack 的转换 */
        this.redisStore = new RedisStore(ItemUtil.GSON, host, port, redisPasswordModel ? password : null);
        this.unsubscribe();
        this.bungeeMessageManager = new BungeeMessageManager.Builder().channel(BUNGEE_CHANNEL)
                .plugin(this)
                .host(host)
                .port(port)
                .passwordModel(redisPasswordModel)
                .password(password)
                .messageListener(e -> {
                    switch (e.getType()) {
                        case SERVER_MESSAGE
                                -> MessageDispatcher.getInstance().sendBroadcast(this, e.getMessage());
                        case PLAYER_MESSAGE
                                -> Optional.ofNullable(Bukkit.getPlayerExact(e.getRecipientName())).ifPresent(player ->
                                        MessageDispatcher.getInstance().sendMessage(this, player, e.getMessage()));
                    }
                }).build();
    }

    public void setupMongoDB() {
        String host = getConfigManager().getYmlValue(YML_MONGODB, "DataSource.Host")
                , database = getConfigManager().getYmlValue(YML_MONGODB, "DataSource.Database");
        int port = getConfigManager().getYmlValue(YML_MONGODB, "DataSource.Port", 27017, ParseType.INTEGER);
        this.mongoDBStore = new MongoDBStore(host, port, database);
    }

    private void setupAPI() {
        this.api = new MagicMarketAPI(this);
    }

    private void unsubscribe() {
        Optional.ofNullable(bungeeMessageManager)
                .ifPresent(BungeeMessageManager::unsubscribe);
    }

    public void sendMessage(CommandSender sender, String languageKey) {
        MessageDispatcher.getInstance().sendMessage(this, sender, getConfigManager().getYmlValue(YML_LANGUAGE, languageKey));
    }

    public void sendMessage(CommandSender sender, String languageKey, Map<String, String> replacements) {
        MessageDispatcher.getInstance().sendMessage(this, sender,
                StringUtil.replacePlaceholders(getConfigManager().getYmlValue(YML_LANGUAGE, languageKey), replacements));
    }

    public void sendBungeeMessage(String playerName, String languageKey) {
        MessageDispatcher.getInstance().sendBungeeMessage(this.getRedisStore(), BUNGEE_CHANNEL, playerName, getConfigManager().getYmlValue(YML_LANGUAGE, languageKey));
    }

    public void sendBungeeMessage(String playerName, String languageKey,  Map<String, String> replacements) {
        MessageDispatcher.getInstance().sendBungeeMessage(this.getRedisStore(), BUNGEE_CHANNEL, playerName, StringUtil.replacePlaceholders(getConfigManager().getYmlValue(YML_LANGUAGE, languageKey), replacements));
    }

    @Override
    protected void loadConfig(ConfigManager configManager) {
        configManager.loadConfig(YML_CONFIG, YML_LANGUAGE,
                YML_MONGODB, YML_REDIS, YML_BLACKLIST, YML_MARKET_MENU, YML_PRODUCT_DETAIL_MENU, YML_PLAYER_MARKET_MENU);
    }

    @Override
    protected void registerCommand(CommandManager commandManager) {
        commandManager.registerCommand(PLUGIN_NAME, e -> {
            switch (e.cause()) {
                case NOT_PLAYER -> sendMessage(e.sender(), "ConsoleMessage.Error.NotPlayer");
                case NOT_CONSOLE -> sendMessage(e.sender(), "PlayerMessage.Error.NotConsole");
                case UN_KNOWN_COMMAND -> sendMessage(e.sender(), "PlayerMessage.Error.UnknownCommand");
                case PERMISSION_DENIED -> sendMessage(e.sender(), "PlayerMessage.Error.PermissionDenied");
            }
        });
    }

    @Override
    protected void registerPacketListener(PacketManager packetManager) {}

}
