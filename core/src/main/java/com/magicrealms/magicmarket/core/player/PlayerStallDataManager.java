package com.magicrealms.magicmarket.core.player;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.FormatUtil;
import com.magicrealms.magiclib.common.utils.RedissonUtil;
import com.magicrealms.magiclib.core.MagicLib;
import com.magicrealms.magiclib.core.menu.ConfirmMenu;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.stall.IPlayerStallDataManager;
import com.magicrealms.magicmarket.api.stall.PlayerStallData;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.*;

/**
 * @author Ryan-0916
 * @Desc 玩家市场信息管理器
 * @date 2025-06-21
 */
public class PlayerStallDataManager implements IPlayerStallDataManager {

    private final BukkitMagicMarket PLUGIN;

    private final static String PERMS_DEFAULT_STALL_COUNT_KEY = "magic.market.stall.default.";
    private final static String PERMS_PURCHASED_STALL_KEY = "magic.market.stall.purchased.";
    private final static String PERMS_MAX_PURCHASED_STALL_KEY = "magic.market.stall.purchased.max.";

    public PlayerStallDataManager(BukkitMagicMarket plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public PlayerStallData queryStallData(Player player) {
        String subKey = StringUtils.upperCase(player.getName());
        Optional<PlayerStallData> cacheData = PLUGIN.getRedisStore().hGetObject(MAGIC_MARKET_PLAYER_MARKET_DATA, subKey, PlayerStallData.class);
        if (cacheData.isPresent()) {
            return cacheData.get();
        }
        PlayerStallData data = new PlayerStallData(player,
                getMaxNumberByLuckPerms(player, PERMS_DEFAULT_STALL_COUNT_KEY),
                getMaxNumberByLuckPerms(player, PERMS_PURCHASED_STALL_KEY),
                getMaxNumberByLuckPerms(player, PERMS_MAX_PURCHASED_STALL_KEY));
        PLUGIN.getRedisStore().hSetObject(MAGIC_MARKET_PLAYER_MARKET_DATA, subKey, data, PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Cache.PlayerStallData", 3600, ParseType.INTEGER));
        return data;
    }

    @Override
    public void purchaseStall(Player player, Runnable cancelTask, Runnable errorTask, Runnable successTask) {
        String subKey = StringUtils.upperCase(player.getName());
        PlayerStallData data = queryStallData(player);
        /* 玩家可上架的最大数量 >= 服务器支持的最大数量 */
        if (data.getTotalCount() >= PLUGIN.getConfigManager()
                .getYmlValue(YML_CONFIG, "Settings.SellNumber.Max", 50, ParseType.INTEGER)) {
            BukkitMagicMarket.getInstance().sendMessage(player, "PlayerMessage.Error.BeyondTheScopeOfPurchase");
            return;
        }
        /* 玩家购买的上架数量 >= 玩家可购买的最大上架数量 */
        if (data.getPurchasedCount() >= data.getMaxPurchasableCount()) {
            BukkitMagicMarket.getInstance().sendMessage(player, "PlayerMessage.Error.BeyondTheScopeOfPurchase");
            return;
        }
        BigDecimal amount = BigDecimal.valueOf(Math.min(PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.First", 50D, ParseType.DOUBLE) +
                        PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.Growth", 50D, ParseType.DOUBLE) * data.getPurchasedCount(),
                PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.Max", 20000D, ParseType.DOUBLE)));
        if (!MagicLib.getInstance().getVaultManager().sufficientAmount(player, amount)) {
            BukkitMagicMarket.getInstance().sendMessage(player, "PlayerMessage.Error.PurchaseStallInsufficientBalance");
            return;
        }
        new ConfirmMenu.Builder()
                .player(player)
                .itemStack(ItemUtil.getItemStackByConfig(PLUGIN.getConfigManager(),
                        YML_CONFIG, "Settings.SellNumber.Buy.Display", Map.of("amount", FormatUtil.formatAmount(amount))))
                .cancelOrCloseTask(() -> {
                    BukkitMagicMarket.getInstance().sendMessage(player, "PlayerMessage.Success.PurchaseStallCancel");
                    cancelTask.run();
                })
                .confirmTask(() -> RedissonUtil.doAsyncWithLock(PLUGIN.getRedisStore(), String.format(MAGIC_MARKET_PURCHASE_STALL_LOCK, subKey), subKey, 5000L, () -> {
                    PlayerStallData newData = queryStallData(player);
                    BigDecimal newAmount = BigDecimal.valueOf(Math.min(PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.First", 50D, ParseType.DOUBLE) +
                                    PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.Growth", 50D, ParseType.DOUBLE) * newData.getPurchasedCount(),
                            PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.Max", 20000D, ParseType.DOUBLE)));
                    if (newAmount.compareTo(amount) > 0) {
                        /* 价格变动 */
                        BukkitMagicMarket.getInstance().sendMessage(player, "PlayerMessage.Error.PurchaseStallBalanceChange");
                        errorTask.run();
                        return;
                    }
                    if (!MagicLib.getInstance().getVaultManager().withdrawAmount(player, newAmount)) {
                        BukkitMagicMarket.getInstance().sendMessage(player, "PlayerMessage.Error.PurchaseStallInsufficientBalance");
                        errorTask.run();
                        return;
                    }
                    if (addMaxNumberByLuckPerms(player, PERMS_PURCHASED_STALL_KEY)) {
                        BukkitMagicMarket.getInstance().sendMessage(player, "PlayerMessage.Success.PurchaseStall");
                        PLUGIN.getRedisStore().removeHkey(MAGIC_MARKET_PLAYER_MARKET_DATA,
                                StringUtils.upperCase(player.getName()));
                        successTask.run();
                        return;
                    }
                    BukkitMagicMarket.getInstance().sendMessage(player, "SystemUnKnowError");
                    MagicLib.getInstance().getVaultManager().depositAmount(player, newAmount);
                    errorTask.run();
                }))
                .open();
    }

    /**
     * 查询权限组中的某个 Node 的最大数量
     * @param player 玩家
     * @param permsKey 权限的 Key
     * @return 查询权限组中的某个 Node 的最大数量
     */
    private int getMaxNumberByLuckPerms(Player player, String permsKey) {
        Optional<LuckPerms> luckPermsAPI = PLUGIN.getLuckPermsAPI();
        if (luckPermsAPI.isEmpty()) { return 0; }
        /* 根据权限组获取玩家最大的数量 */
        CompletableFuture<Integer[]> maxNumberCompletable = luckPermsAPI.get().getUserManager().loadUser(player.getUniqueId()).thenApply(user -> {
            final Integer[] max = {0};
            String regex = "^"+ permsKey +"[1-9]\\d*$";
            Collection<Node> nodes = user.resolveInheritedNodes(QueryOptions.nonContextual());
            nodes.forEach(e -> {
                if (e.getKey().matches(regex)) {
                    try {
                        int i = Integer.parseInt(e.getKey().replace(permsKey, ""));
                        if (i > max[0]) {
                            max[0] = i;
                        }
                    } catch (Exception exception) {
                        PLUGIN.getLoggerManager().warning("玩家 " + player.getName() + " 上架商品数值超过 2147483647 这是一个错误的数值");
                    }
                }
            });
            return max;
        });
        return maxNumberCompletable.join()[0];
    }

    /**
     * 增加权限组中的某个 Node 的最大数量
     * @param player 玩家
     * @param permsKey 权限的 Key
     * @return 增加成功 / 失败
     */
    public boolean addMaxNumberByLuckPerms(Player player, String permsKey) {
        try {
            Optional<LuckPerms> luckPermsOptional = PLUGIN.getLuckPermsAPI();
            if (luckPermsOptional.isEmpty()) {
                PLUGIN.getLoggerManager().warning("为玩家" + player.getName() + "添加可上架商品数量时出现异常，无法调用 LuckPerms 插件");
                return false;
            }
            LuckPerms luckPerms = luckPermsOptional.get();
            return luckPerms.getUserManager().loadUser(player.getUniqueId()).thenApply(user -> {
                final Integer[] max = {0};
                String regex = "^"+ permsKey +"[1-9]\\d*$";
                Collection<Node> nodes = user.getNodes();
                nodes.forEach(e -> {
                    if (e.getKey().matches(regex)) {
                        user.data().remove(e);
                        try {
                            int i = Integer.parseInt(e.getKey().replace(PERMS_PURCHASED_STALL_KEY, ""));
                            if (i > max[0]) {
                                max[0] = i;
                            }
                        } catch (Exception exception) {
                            PLUGIN.getLogger().warning("玩家 " + player.getName() + "购买的上架商品数量数值超过 2147483647 这是一个错误的数值");
                        }
                    }
                });
                user.data().add(Node.builder(PERMS_PURCHASED_STALL_KEY + (max[0] + 1)).build());
                luckPerms.getUserManager().saveUser(user);
                return true;
            }).join();
        } catch (Exception e) {
            PLUGIN.getLoggerManager().error("为玩家" + player.getName() + "添加可上架商品数量时出现异常", e);
            return false;
        }
    }

}
