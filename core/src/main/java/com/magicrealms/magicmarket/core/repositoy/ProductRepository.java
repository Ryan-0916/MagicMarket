package com.magicrealms.magicmarket.core.repositoy;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.repository.BaseRepository;
import com.magicrealms.magiclib.common.store.MongoDBStore;
import com.magicrealms.magiclib.common.store.RedisStore;
import com.magicrealms.magiclib.common.utils.MongoDBUtil;
import com.magicrealms.magicmarket.api.product.Product;
import com.magicrealms.magicmarket.api.product.ProductStatus;
import com.magicrealms.magicmarket.api.repository.IProductRepository;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.*;

/**
 * @author Ryan-0916
 * @Desc 全球市场商品数据存储类
 * @date 2025-06-10
 */
public class ProductRepository extends BaseRepository<Product> implements IProductRepository {

    public ProductRepository(MongoDBStore mongoDBStore, RedisStore redisStore) {
        super(mongoDBStore, MAGIC_MARKET_PRODUCT_TABLE_NAME,
                redisStore, Product.class);
    }

    @Override
    public void shellProduct(Product product) {
        super.insert(product);
        if (getRedisStore().exists(MAGIC_MARKET_VALID_PRODUCTS)) { getRedisStore().hSetObject(MAGIC_MARKET_VALID_PRODUCTS, product.getId(), product, BukkitMagicMarket.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Cache.ValidProducts", 3600L, ParseType.LONG)); }
    }

    @Override
    public List<Product> queryValidProducts() {
        Optional<List<Product>> cachedData = getRedisStore()
                .hGetAllObject(MAGIC_MARKET_VALID_PRODUCTS, Product.class);
        if (cachedData.isPresent()) {
            return cachedData.get();
        }
        List<Product> products = new ArrayList<>();
        try (MongoCursor<Document> cursor = getMongoDBStore().find(getTableName(), Filters.or(
                Filters.eq("status", ProductStatus.ON_SALE.getValue()),
                Filters.and(Filters.or(Filters.eq("status", ProductStatus.SELLER_REMOVAL.getValue()),
                                Filters.eq("status", ProductStatus.SYSTEM_REMOVAL.getValue()),
                                Filters.eq("status", ProductStatus.ADMIN_REMOVAL.getValue())),
                        Filters.eq("already_return", false))
        ))) {
            while (cursor.hasNext()) {
                products.add(MongoDBUtil.toObject(cursor.next(), Product.class));
            }
        }
        if (!products.isEmpty()) {
            cacheValidProducts(products);
        }
        return products;
    }

    private void cacheValidProducts(List<Product> products) {
        getRedisStore().hSetObject(MAGIC_MARKET_VALID_PRODUCTS, products.stream()
                .collect(Collectors.toMap(
                        Product::getId,    // Key: Product 的 ID
                        product -> product,   // Value: Product 对象本身
                        (existing, replacement) -> existing,  // 如果 Key 冲突，保留旧值
                        LinkedHashMap::new  // 使用 LinkedHashMap 保持顺序
                )), BukkitMagicMarket.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Cache.ValidProducts", 3600L, ParseType.LONG));
    }
}
