package com.magicrealms.magicmarket.core.exception;

/**
 * @author Ryan-0916
 * @Desc 商品状态异常
 * 当购买时，被采购商品状态为非上架状态时，则代表该商品状态异常
 * 当下架时，被下架商品状态为非上架状态时，则代表该商品状态异常
 * @date 2025-06-18
 */
@SuppressWarnings("unused")
public class ProductStatusException extends RuntimeException {

    public ProductStatusException() {
        super();
    }

    public ProductStatusException(String message) {
        super(message);
    }

    public ProductStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductStatusException(Throwable cause) {
        super(cause);
    }
    
}
