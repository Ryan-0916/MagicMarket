package com.magicrealms.magicmarket.api.category;

import java.util.List;
import java.util.Optional;

/**
 * @author Ryan-0916
 * @Desc 分类管理器接口
 * @date 2025-06-09
 */
@SuppressWarnings("unused")
public interface ICategoryManager {

    /**
     * 获取全部类型
     * @return 返回所有类型
     */
    List<Category> getCategories();

    /**
     * 根据分类名称获取该分类
     * @return 分类
     */
    Optional<Category> getCategory(String categoryName);

}
