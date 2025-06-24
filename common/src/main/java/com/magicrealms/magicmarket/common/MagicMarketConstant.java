package com.magicrealms.magicmarket.common;

/**
 * @author Ryan-0916
 * @Desc 常量
 * @date 2025-06-09
 */
public class MagicMarketConstant {
    /* 插件名称 */
    public static final String PLUGIN_NAME = "MagicMarket";

    /** 配置文件部分常量 */
    public static final String YML_CONFIG = "config";
    public static final String YML_REDIS = "redis";
    public static final String YML_LANGUAGE = "language";
    public static final String YML_MONGODB = "mongodb";
    public static final String YML_BLACKLIST = "blacklist";
    public static final String YML_CATEGORY_DEF_MOBILE = "category/category.yml";
    public static final String YML_MARKET_MENU = "menu/marketMenu";
    public static final String YML_PRODUCT_DETAIL_MENU = "menu/productDetailMenu";
    public static final String YML_PLAYER_MARKET_MENU = "menu/playerMarketMenu";
    public static final String YML_FIND_PRODUCT_MENU = "menu/findProductMenu";
    public static final String YML_MY_MARKET_MENU = "menu/myMarketMenu";

    /** Redis 相关 key */
    /* 跨服通讯频道 */
    public static final String BUNGEE_CHANNEL = "BUNGEE_CHANNEL_MAGIC_MARKET";
    /* 在售商品 */
    public static final String MAGIC_MARKET_ON_SALE_PRODUCTS = "MAGIC_MARKET_ON_SALE_PRODUCTS";
    /* 玩家市场信息 */
    public static final String MAGIC_MARKET_PLAYER_MARKET_DATA = "MAGIC_MARKET_PLAYER_MARKET_DATA";

    public static final String MAGIC_MARKET_PURCHASE_STALL_LOCK = "MAGIC_MARKET_PURCHASE_STALL_LOCK_%s";

    /** MongoDB部分常量 */
    /* 全球市场商品表 */
    public static final String MAGIC_MARKET_PRODUCT_TABLE_NAME = "magic_market_product";

}
