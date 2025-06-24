package com.magicrealms.magicmarket.api.stall;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magicmarket.api.MagicMarket;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_CONFIG;

/**
 * @author Ryan-0916
 * @Desc 玩家摊位信息
 * @date 2025-06-21
 */
@Data
@NoArgsConstructor
public class PlayerStallData {

    /* 玩家的 UUID */
    private UUID playerUniqueId;

    /* 玩家的名称 */
    private String playerName;

    /* 玩家基础摊位数量 */
    private int defaultCount;

    /* 玩家购买的摊位数量 */
    private int purchasedCount;

    /* 玩家可购买的最大摊位数量 */
    private int maxPurchasableCount;

    public PlayerStallData(Player player, int defaultCount, int purchasedCount, int maxPurchasableCount) {
        this.playerUniqueId = player.getUniqueId();
        this.playerName = player.getName();
        this.defaultCount = defaultCount;
        this.purchasedCount = purchasedCount;
        this.maxPurchasableCount = maxPurchasableCount;
    }

    /**
     * 获取玩家的摊位数量
     * @return 玩家的摊位数量
     */
    public int getTotalCount() {
        /* 如果玩家的摊位数量 > 管理员配置的玩家最大摊位数量则返回玩家最大摊位数量 */
        return Math.min(defaultCount + purchasedCount, MagicMarket.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Max", 50, ParseType.INTEGER));
    }

}
