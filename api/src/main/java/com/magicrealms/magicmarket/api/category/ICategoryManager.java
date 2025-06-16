package com.magicrealms.magicmarket.api.category;

import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Ryan-0916
 * @Desc 分类管理器接口
 * @date 2025-06-09
 */
public interface ICategoryManager {

    boolean inCategory(String categoryName, ItemStack itemStack);

    List<ItemStack> getCategoryItems(String categoryName);

    List<Category> getCategories();

}
