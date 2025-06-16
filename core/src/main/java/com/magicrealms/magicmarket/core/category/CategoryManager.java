package com.magicrealms.magicmarket.core.category;

import com.magicrealms.magicmarket.api.category.Category;
import com.magicrealms.magicmarket.api.category.ICategoryManager;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Ryan-0916
 * @Desc 分类管理器实现类
 * @date 2025-06-09
 */
public class CategoryManager implements ICategoryManager {

    private final CategoryLoader loader;

    public CategoryManager(BukkitMagicMarket plugin) {
        this.loader = new CategoryLoader(plugin);
    }

    @Override
    public boolean inCategory(String categoryName, ItemStack itemStack) {
        return false;
    }

    @Override
    public List<ItemStack> getCategoryItems(String categoryName) {
        return List.of();
    }

    @Override
    public List<Category> getCategories() {
        return loader.getCategories();
    }

}
