package com.magicrealms.magicmarket.core.exception;

/**
 * @author Ryan-0916
 * @Desc 购买商品异常
 * @date 2025-06-18
 */
@SuppressWarnings("unused")
public class BuyProductException extends RuntimeException {

    public BuyProductException() {
        super();
    }

    public BuyProductException(String message) {
        super(message);
    }

    public BuyProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuyProductException(Throwable cause) {
        super(cause);
    }
}
