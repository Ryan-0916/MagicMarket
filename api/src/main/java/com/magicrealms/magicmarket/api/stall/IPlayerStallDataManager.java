package com.magicrealms.magicmarket.api.stall;

import org.bukkit.entity.Player;

/**
 * @author Ryan-0916
 * @Desc 玩家市场信息管理器
 * @date 2025-06-21
 */
public interface IPlayerStallDataManager {

    /**
     * 获取玩家市场信息
     * @param player 玩家
     * @return 玩家市场信息
     */
    PlayerStallData queryStallData(Player player);

    /**
     * 开启购买摊位操作
     * @param player 玩家
     * @param cancelTask 操作取消事件
     * @param errorTask 操作失败事件
     * @param successTask 操作成功事件
     */
    void purchaseStall(Player player, Runnable cancelTask, Runnable errorTask, Runnable successTask);

}
