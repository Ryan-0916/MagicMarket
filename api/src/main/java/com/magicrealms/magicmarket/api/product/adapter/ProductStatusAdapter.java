package com.magicrealms.magicmarket.api.product.adapter;

import com.magicrealms.magiclib.common.adapt.FieldAdapter;
import com.magicrealms.magicmarket.api.product.ProductStatus;

import java.util.Optional;

/**
 * @author Ryan-0916
 * @Desc 商品状态转换器
 * @date 2025-06-10
 */
public class ProductStatusAdapter extends FieldAdapter<ProductStatus, Integer> {

    @Override
    public Integer write(ProductStatus writer) {
        return writer == null ? 0 : writer.getValue();
    }

    @Override
    public ProductStatus read(Integer reader) {
        return Optional.ofNullable(reader)
                .map(ProductStatus::fromCode)
                .orElse(ProductStatus.INVALID);
    }

}
