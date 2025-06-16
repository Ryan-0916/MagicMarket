package com.magicrealms.magicmarket.api.product;

import com.magicrealms.magiclib.common.adapt.BigDecimalFieldAdapter;
import com.magicrealms.magiclib.common.adapt.UUIDFieldAdapter;
import com.magicrealms.magiclib.common.annotations.FieldId;
import com.magicrealms.magiclib.common.annotations.MongoField;
import com.magicrealms.magiclib.common.utils.IdGeneratorUtil;
import com.magicrealms.magiclib.core.adapt.ItemStackFieldAdapter;
import com.magicrealms.magicmarket.api.MagicMarket;
import com.magicrealms.magicmarket.api.product.adapter.ProductStatusAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_CONFIG;

/**
 * @author Ryan-0916
 * @Desc 市场商品
 * @date 2025-06-10
 */
@Data
@Builder(builderClassName = "ProductBuilder", toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    /* 商品编号 */
    @MongoField(id = @FieldId(enable = true))
    private String id;
    /* 商品状态 */
    @MongoField(adapter = ProductStatusAdapter.class)
    private ProductStatus status;
    /* 价格 */
    @MongoField(adapter = BigDecimalFieldAdapter.class)
    private BigDecimal price;
    /* 服务费 */
    @MongoField(adapter = BigDecimalFieldAdapter.class)
    private BigDecimal serviceCharge;
    /* 商品 */
    @MongoField(adapter = ItemStackFieldAdapter.class)
    private ItemStack product;
    /* 上架者ID */
    @MongoField(adapter = UUIDFieldAdapter.class, name = "seller_uuid")
    private UUID sellerUniqueId;
    /* 上架者名称 */
    @MongoField
    private String sellerName;
    /* 上架时间 */
    @MongoField
    private long shelfTime;
    /* 上架天数 */
    @MongoField
    private int shelfLife;
    /* 商品已返还 */
    @MongoField
    private boolean alreadyReturn;
    /* 下架者名称 */
    @MongoField
    private String removalName;
    /* 下架理由 */
    @MongoField
    private String removalReasons;

    private final Map<String, List<String>> LORE_CACHE = new HashMap<>();

    public static Product.ProductBuilder builder(Player seller, ItemStack product, BigDecimal price) {
        Objects.requireNonNull(seller, "Seller cannot be null");
        Objects.requireNonNull(product, "Product cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        return new ProductBuilder()
                .id(IdGeneratorUtil.getId())
                .sellerUniqueId(seller.getUniqueId())
                .sellerName(seller.getName())
                .product(product)
                .price(price)
                .status(ProductStatus.ON_SALE)
                .shelfTime(System.currentTimeMillis());
    }

    public double getPrice() {
        return price.doubleValue();
    }

    /**
     * 获取商品过期时间的时间戳（毫秒）
     * @return 过期时间的时间戳，如果 shelfLife <= 0 则返回 Long.MAX_VALUE 表示永不过期
     */
    public long getExpirationTime() {
        if (shelfLife <= 0) {
            return Long.MAX_VALUE; // 表示永不过期
        }
        return shelfTime + TimeUnit.DAYS.toMillis(shelfLife);
    }

    /**
     * 判断商品是否已过期
     * @return true 如果已过期，false 如果未过期或永不过期
     */
    public boolean isExpired() {
        if (shelfLife <= 0) {
            return false; // 永不过期
        }
        return System.currentTimeMillis() > getExpirationTime();
    }

    private List<String> getYmlProductLore(String key) {
        return LORE_CACHE.computeIfAbsent(key,
                k ->MagicMarket.getInstance().getConfigManager()
                        .getYmlListValue(YML_CONFIG, String.format("Settings.ProductLore.%s", key))
                        .orElse(new ArrayList<>()));
    }

//    public ItemStack getProductInMenu(Player player) {
//        ItemStack itemStack = getProduct().clone();
//        List<Component> lore = new ArrayList<>();
//        /* 商品本身的 Lore */
//        Optional.ofNullable(itemStack.lore()).ifPresent(lore::addAll);
//        List<String> newLore = getYmlProductLore("Default");
//        boolean isSelf = StringUtils.equalsIgnoreCase(player.getName(), sellerName);
//        if (isSelf) {
//            if (productStatus == ProductStatus.ON_SALE) {
//                newLore.addAll(selfOnSaleLore);
//            } else {
//                Map<String, String> map = new HashMap<>();
//                map.put("removal_name", removalName);
//                map.put("removal_reasons", removalReasons);
//                newLore.addAll(selfTakenDownLore.stream().map(e -> StringUtil.replacePlaceholder(e, map)).toList());
//            }
//        } else {
//            newLore.addAll(playerLore);
//            if (player.hasPermission("magic.command.globalmarket.operation")) {
//                newLore.addAll(opLore);
//            }
//        }
//        lore.addAll(newLore.stream().map(e -> ItemUtil.UN_ITALIC.append(AdventureHelper.getMiniMessage().deserialize(e))).toList());
//        itemStack.lore(lore);
//        return itemStack;
//    }
}
