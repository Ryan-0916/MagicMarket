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
import java.util.function.Consumer;
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
    public List<Product> queryOnSaleProducts() {
        Optional<List<Product>> cachedData = redisStore.hGetAllObject(MAGIC_MARKET_ON_SALE_PRODUCTS, Product.class);
        if (cachedData.isPresent()) { return cachedData.get(); }
        List<Product> products = new ArrayList<>();
        try (MongoCursor<Document> cursor = mongoDBStore.find(tableName, Filters.eq("status", ProductStatus.ON_SALE.getValue()))) {
            while (cursor.hasNext()) { products.add(MongoDBUtil.toObject(cursor.next(), Product.class)); }
        }
        if (!products.isEmpty()) {
            cacheOnSaleProducts(products);
        }
        return products;
    }

    private void cacheOnSaleProducts(List<Product> products) {
        redisStore.hSetObject(MAGIC_MARKET_ON_SALE_PRODUCTS, products.stream()
                .collect(Collectors.toMap(
                        Product::getId,    // Key: Product 的 ID
                        product -> product,   // Value: Product 对象本身
                        (existing, replacement) -> existing,  // 如果 Key 冲突，保留旧值
                        LinkedHashMap::new  // 使用 LinkedHashMap 保持顺序
                )), BukkitMagicMarket.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Cache.ValidProducts", 3600L, ParseType.LONG));
    }

    @Override
    public void insert(Product product) {
        super.insert(product);
        if (product.getStatus() == ProductStatus.ON_SALE && redisStore.exists(MAGIC_MARKET_ON_SALE_PRODUCTS)) {
            redisStore.hSetObject(MAGIC_MARKET_ON_SALE_PRODUCTS, product.getId(), product, BukkitMagicMarket.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Cache.ValidProducts", 3600L, ParseType.LONG));
        }
    }

    @Override
    public boolean update(String id, Consumer<Product> consumer, boolean removeCache) {
        if (!super.updateById(id, consumer)) {
            redisStore.removeKey(MAGIC_MARKET_ON_SALE_PRODUCTS);
            return false;
        }
        if (removeCache) {
            redisStore.removeHkey(MAGIC_MARKET_ON_SALE_PRODUCTS, id);
        }
        return true;
    }
}
