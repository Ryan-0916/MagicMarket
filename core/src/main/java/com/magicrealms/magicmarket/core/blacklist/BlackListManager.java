package com.magicrealms.magicmarket.core.blacklist;

import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.blacklist.IBlackListManager;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;

/**
 * @author Ryan-0916
 * @Desc 黑名单管理器
 * @date 2025-06-12
 */
public class BlackListManager implements IBlackListManager {

    private final BlackListLoader loader;

    public BlackListManager(BukkitMagicMarket plugin) {
        this.loader = new BlackListLoader(plugin);
    }

    @Override
    public boolean containsItem(ItemStack item) {
        if (ItemUtil.isAirOrNull(item)) { return false; }
        if (loader.getBlackList().stream().anyMatch(pair ->
                pair.first().equalsIgnoreCase(item.getType().name()) &&
                        pair.second() == (item.getItemMeta().hasCustomModelData() ?
                                item.getItemMeta().getCustomModelData() : 0))) {
            return true;
        }
        if (item.getType() == Material.SHULKER_BOX && item.getItemMeta()
                instanceof BlockStateMeta itemMeta) {
            /* 物品如果为潜影盒，那么将会检测潜影盒中的每一项物品是否合规 */
            ShulkerBox shulkerBox = (ShulkerBox) itemMeta.getBlockState();
            for (ItemStack subItem : shulkerBox.getInventory()) {
                if (containsItem(subItem)) {
                    return true;
                }
            }
        } else if (item.getItemMeta() instanceof BundleMeta itemMeta) {
            return itemMeta.getItems().stream().anyMatch(this::containsItem);
        }
        return false;
    }
}
