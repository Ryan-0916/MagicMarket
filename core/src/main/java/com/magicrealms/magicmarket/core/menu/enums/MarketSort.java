package com.magicrealms.magicmarket.core.menu.enums;

import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.product.Product;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;

/**
 * @author Ryan-0916
 * @Desc 市场排序方式枚举
 * @date 2025-06-12
 */
@Getter
public enum MarketSort {

    /* 按收件时间从新到旧 (值: 0) */
    NEWEST(0, "Newest"),

    /* 按收件时间从旧到新 (值: 1) */
    OLDEST(1, "Oldest"),

    /* 按照价格从高到低 (值: 3)*/
    EXPENSIVE(3, "Expensive"),

    /* 按照价格从低到高 (值：4)*/
    CHEAPEST(4, "Cheapest");

    /* 枚举值对应的整数值 */
    private final int value;

    /* Menu YML 中对应的 Path */
    private final String path;

    MarketSort(int value, String path) {
        this.value = value;
        this.path = path;
    }

    /**
     * 获取下一个排序方式（循环切换）
     * @return 下一个排序方式枚举值
     */
    public MarketSort next() {
        MarketSort[] values = MarketSort.values();
        int nextOrdinal = (this.ordinal() + 1) % values.length;
        return values[nextOrdinal];
    }

    /**
     * 获取排序方式在 YML 中的显示物品
     * @param key YML 所对应的 Key
     * @param yml YML 文件目录
     * @return 返回排序方式在 YML 中的物品
     */
    public ItemStack getItemSlot(char key, String yml) {
        String path = String.format("Icons.%s.%s", key, this.path + "Display");
        return ItemUtil.getItemStackByConfig(BukkitMagicMarket.getInstance().getConfigManager(),
                yml, path);
    }

    /**
     * 将商品排序
     * @param data 排序后的商品
     */
    public void sort(List<Product> data) {
        switch (this) {
            case NEWEST -> data.sort(Comparator.comparingLong(Product::getShelfTime).reversed());
            case OLDEST -> data.sort(Comparator.comparingLong(Product::getShelfTime));
            case EXPENSIVE -> data.sort(Comparator.comparing(Product::getPrice).reversed());
            case CHEAPEST -> data.sort(Comparator.comparing(Product::getPrice));
        }
    }
}
