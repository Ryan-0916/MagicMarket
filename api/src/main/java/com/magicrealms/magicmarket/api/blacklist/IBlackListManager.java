package com.magicrealms.magicmarket.api.blacklist;

import org.bukkit.inventory.ItemStack;

/**
 * @author Ryan-0916
 * @Desc 黑名单物品管理器
 * @date 2025-06-12
 */
public interface IBlackListManager {
    /**
     * 该物品是否存在于黑名单内
     * @param item 物品
     * @return 是否存在于黑名单内
     */
    boolean containsItem(ItemStack item);
}
