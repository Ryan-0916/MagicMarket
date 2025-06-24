package com.magicrealms.magicmarket.core.category;

import com.magicrealms.magicmarket.api.category.Category;
import com.magicrealms.magicmarket.api.category.ICategoryManager;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.Optional;

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
    public List<Category> getCategories() {
        return loader.getCategories();
    }

    @Override
    public Optional<Category> getCategory(String categoryName) {
        return getCategories().stream().filter(e -> StringUtils.equals(e.getName(), categoryName))
                .findFirst();
    }


}
