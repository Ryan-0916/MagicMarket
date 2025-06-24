package com.magicrealms.magicmarket.api.category;

import com.magicrealms.magiclib.common.utils.Pair;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ryan-0916
 * @Desc 物品分类
 * @date 2025-06-09
 */
@Builder
@Getter
@SuppressWarnings("unused")
public class Category {

    /* 文件路径 */
    private String path;
    /* 类型名称 */
    private String name;
    /* 权重 */
    private int weight;
    /* 物品信息
     * First: 物品ID
     * Second: CustomData
     */
    private List<Pair<String, Integer>> items;

    /**
     * 检查物品是否属于当前分类。
     * @param item 待检查的物品
     * @return 如果物品的ID和CustomData与分类中某一项匹配，则返回true
     */
    public boolean containsItem(ItemStack item) {
        return items.stream().anyMatch(pair ->
                pair.first().equalsIgnoreCase(item.getType().name()) &&
                        pair.second() == (item.getItemMeta().hasCustomModelData() ?
                                item.getItemMeta().getCustomModelData() : 0));
    }

    /**
     * 获取该分类中的全部物品
     * @return 该分类中的全部物品
     */
    public List<ItemStack> getCategoryItems() {
        return items.stream().map(pair -> {
            Material material = Material.getMaterial(pair.first());
            if (material == null) { return null; }
            ItemStack item = new ItemStack(material);
            if (ItemUtil.isAirOrNull(item)) { return null; }
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(pair.second());
            item.setItemMeta(meta);
            return item;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
