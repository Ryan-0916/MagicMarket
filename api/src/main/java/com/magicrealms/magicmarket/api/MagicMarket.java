package com.magicrealms.magicmarket.api;

import com.magicrealms.magiclib.bukkit.MagicRealmsPlugin;
import com.magicrealms.magiclib.common.store.MongoDBStore;
import com.magicrealms.magiclib.common.store.RedisStore;
import com.magicrealms.magicmarket.api.blacklist.IBlackListManager;
import com.magicrealms.magicmarket.api.category.ICategoryManager;
import com.magicrealms.magicmarket.api.product.IProductManager;
import com.magicrealms.magicmarket.api.repository.IProductRepository;
import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.Map;

/**
 * @author Ryan-0916
 * @Desc 全球市场
 * @date 2025-06-09
 */
@SuppressWarnings("unused")
public abstract class MagicMarket extends MagicRealmsPlugin {

    protected MagicMarketAPI api;

    @Getter
    private static MagicMarket instance;

    @Getter
    protected RedisStore redisStore;

    @Getter
    protected MongoDBStore mongoDBStore;

    @Getter
    protected ICategoryManager categoryManager;

    @Getter
    protected IProductRepository productRepository;

    @Getter
    protected IProductManager productManager;

    @Getter
    protected IBlackListManager blacklistManager;

    public abstract void sendMessage(CommandSender sender, String languageKey);

    public abstract void sendMessage(CommandSender sender, String languageKey, Map<String, String> replacements);

    public abstract void sendBungeeMessage(String playerName, String languageKey);

    public abstract void sendBungeeMessage(String playerName, String languageKey, Map<String, String> replacements);

    protected MagicMarket() {
        instance = this;
    }

}
