package com.magicrealms.magicmarket.api.product;

import lombok.Getter;

/**
 * @author Ryan-0916
 * @Desc 商品状态
 * @date 2025-06-10
 */
@Getter
public enum ProductStatus {
    INVALID(0, "无效"),
    ON_SALE(1, "正在出售"),
    BE_SALE(2, "已出售"),
    SELLER_REMOVAL(3, "卖家下架"),
    SYSTEM_REMOVAL(4, "系统下架"),
    ADMIN_REMOVAL(5, "管理员下架");

    private final int value;
    private final String name;

    ProductStatus(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public static ProductStatus fromCode(int code) {
        for (ProductStatus status : ProductStatus.values()) {
            if (status.getValue() == code) {
                return status;
            }
        }
        return INVALID;
    }
}
