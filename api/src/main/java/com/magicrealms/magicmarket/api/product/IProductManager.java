package com.magicrealms.magicmarket.api.product;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Ryan-0916
 * @Desc 商品管理接口
 * @date 2025-06-10
 */
public interface IProductManager {

    /**
     * 查询在售商品列表
     * @return 查询市场内所有在售商品
     */
    List<Product> queryOnSaleProducts();

    /**
     * 上架一件商品至全球市场
     * @param seller 上架者
     * @param product 商品
     * @param price 价格
     */
    void sellProduct(Player seller, ItemStack product, BigDecimal price);

    /**
     * 购买商品
     * @param buyer 买家
     * @param product 商品
     */
    void buyProduct(Player buyer, Product product);

    /**
     * 从市场上将商品移除
     * @param remover 移除者
     */
    void removeProduct(Player remover, Product product, Runnable cancelTask, Runnable errorTask, Runnable successTask);

    /**
     * 从市场上强制将商品移除 - 系统管理员身份
     * @param reasons 移除理由
     */
    void removeProductBySystem(Product product, String reasons);


}
