package com.magicrealms.magicmarket.core.menu.listener;

/**
 * @author Ryan-0916
 * @Desc 查找商品观察器
 * @date 2025-06-20
 */
public interface FindProductMenuObserver {

    /**
     * 更改排序
     */
    void changeSort();

    /**
     * 更改商品列表
     */
    void changeProducts();

    /**
     * 更改查找商品
     */
    void changeSearch();

}
