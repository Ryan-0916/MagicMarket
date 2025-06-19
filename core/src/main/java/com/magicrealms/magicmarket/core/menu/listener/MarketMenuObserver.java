package com.magicrealms.magicmarket.core.menu.listener;

/**
 * @author Ryan-0916
 * @Desc 全球市场菜单观察者
 * @date 2025-06-12
 */
public interface MarketMenuObserver {

    /**
     * 更改排序
     */
    void changeSort();

    /**
     * 更改分类
     */
    void changeCategory();

    /**
     * 更改商品
     */
    void changeProducts();
}
