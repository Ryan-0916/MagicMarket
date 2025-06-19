package com.magicrealms.magicmarket.api.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Ryan-0916
 * @Desc 商品管理器
 * @date 2025-06-10
 */
public interface IProductManager {

    /**
     * 上架物品至全球市场
     * @param seller 上架者
     * @param product 商品
     * @param price 价格
     */
    void sellProduct(Player seller, ItemStack product, BigDecimal price);

    /**
     * 查询正在上架的商品
     * @return 仅查询正在上架的商品
     */
    List<Product> queryOnSaleProducts();

    /**
     * 购买商品
     */
    void buyProduct(Player buyer, Product product);
}
