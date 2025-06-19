package com.magicrealms.magicmarket.api.repository;

import com.magicrealms.magiclib.common.repository.IBaseRepository;
import com.magicrealms.magicmarket.api.product.Product;

import java.util.List;

/**
 * @author Ryan-0916
 * @Desc 全球市场商品数据存储类
 * @date 2025-06-10
 */
public interface IProductRepository extends IBaseRepository<Product> {

    /**
     * 上架商品至全球市场
     * @param product 商品
     */
    void shellProduct(Product product);

    /**
     * 查询全球市场内所有有效商品
     * 有效判断逻辑：在售商品，已下架但未退还至玩家背包中的商品
     * @return 商品列表
     */
    List<Product> queryValidProducts();

    boolean buyProduct(String id);

}
