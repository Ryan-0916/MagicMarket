package com.magicrealms.magicmarket.api.repository;

import com.magicrealms.magiclib.common.repository.IBaseRepository;
import com.magicrealms.magicmarket.api.product.Product;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Ryan-0916
 * @Desc 全球市场商品数据存储类
 * @date 2025-06-10
 */
public interface IProductRepository extends IBaseRepository<Product> {

    /**
     * 查询在售商品列表
     * @return 查询市场内所有在售商品
     */
    List<Product> queryOnSaleProducts();

    /**
     * 修改商品
     * @param id 商品编号
     * @param consumer 修改内容
     * @param removeCache 是否移除缓存
     * @return 修改成功 / 失败
     */
    boolean update(String id, Consumer<Product> consumer, boolean removeCache);
}
