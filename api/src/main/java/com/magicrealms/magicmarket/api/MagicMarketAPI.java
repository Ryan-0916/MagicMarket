package com.magicrealms.magicmarket.api;

import com.magicrealms.magicmarket.api.product.IProductManager;
import com.magicrealms.magicmarket.api.product.Product;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Ryan-0916
 * @Desc MagicMarket API
 * @date 2025-06-09
 */
@SuppressWarnings("unused")
public record MagicMarketAPI(MagicMarket plugin) {

    private static MagicMarketAPI instance;

    public MagicMarketAPI(MagicMarket plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static MagicMarketAPI getInstance() {
        if (instance == null) {
            throw new RuntimeException("MagicMarket API 未被初始化");
        }
        return instance;
    }

    /**
     * Sell products on the market
     * {@link IProductManager#sellProduct(Player, ItemStack, BigDecimal)}
     */
    public void sellProduct(Player seller, ItemStack product, BigDecimal price) {
        plugin.getProductManager().sellProduct(seller, product, price);
    }

    /**
     * Fetch all items in the market
     * {@link IProductManager#queryOnSaleProducts()}
     */
    public List<Product> queryOnSaleProducts() {
        return plugin.getProductManager().queryOnSaleProducts();
    }

}
