package com.magicrealms.magicmarket.api.product;

import com.magicrealms.magiclib.bukkit.message.helper.AdventureHelper;
import com.magicrealms.magiclib.common.adapt.BigDecimalFieldAdapter;
import com.magicrealms.magiclib.common.adapt.UUIDFieldAdapter;
import com.magicrealms.magiclib.common.annotations.FieldId;
import com.magicrealms.magiclib.common.annotations.MongoField;
import com.magicrealms.magiclib.common.utils.FormatUtil;
import com.magicrealms.magiclib.common.utils.IdGeneratorUtil;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.adapt.ItemStackFieldAdapter;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.MagicMarket;
import com.magicrealms.magicmarket.api.product.adapter.ProductStatusAdapter;
import com.magicrealms.magicplayer.api.MagicPlayerAPI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public double getDoublePrice() {
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
        return shelfLife > 0 && System.currentTimeMillis() > getExpirationTime();
    }

    private List<String> getYmlProductLore(String key) {
        return MagicMarket.getInstance().getConfigManager()
                .getYmlListValue(YML_CONFIG, String.format("Settings.ProductLore.%s", key))
                .orElse(new ArrayList<>());
    }

    private Map<String, String> createDefaultPlaceholders() {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("price", FormatUtil.formatAmount(price));
        placeholders.put("seller_name", sellerName);
        placeholders.put("seller_avatar", MagicPlayerAPI.getInstance().getPlayerAvatar(sellerName));
        placeholders.putAll(FormatUtil.formatDateTime(getExpirationTime(), "expiration_time_"));
        return placeholders;
    }

    private List<Component> processLoreLines(List<String> loreLines, Map<String, String> placeholders) {
        return loreLines.stream()
                .map(line -> ItemUtil.UN_ITALIC.append(
                        AdventureHelper.getMiniMessage().deserialize(
                                placeholders != null ? StringUtil.replacePlaceholders(line, placeholders) : line
                        )
                ))
                .collect(Collectors.toList());
    }

    private ItemStack buildItemWithLore(ItemStack baseItem, List<Component> additionalLore) {
        ItemStack item = baseItem.clone();
        List<Component> lore = new ArrayList<>();
        Optional.ofNullable(item.lore()).ifPresent(lore::addAll);
        lore.addAll(additionalLore);
        item.lore(lore);
        return item;
    }

    public ItemStack getDefaultLoreProduct() {
        List<Component> lore = processLoreLines(getYmlProductLore("Default"), createDefaultPlaceholders());
        return buildItemWithLore(getProduct(), lore);
    }

    public ItemStack getMenuProduct(Player player) {
        Map<String, String> placeholders = createDefaultPlaceholders();
        List<Component> lore = processLoreLines(getYmlProductLore("Default"), placeholders);
        if (sellerUniqueId.equals(player.getUniqueId())) {
            if (status == ProductStatus.ON_SALE) {
                lore.addAll(processLoreLines(getYmlProductLore("Self.OnSale"), null));
            } else {
                Map<String, String> takenDownPlaceholders = new HashMap<>();
                takenDownPlaceholders.put("removal_name", removalName);
                takenDownPlaceholders.put("removal_reasons", removalReasons);
                lore.addAll(processLoreLines(getYmlProductLore("Self.TakenDown"), takenDownPlaceholders));
            }
            return buildItemWithLore(getProduct(), lore);
        }
        lore.addAll(processLoreLines(getYmlProductLore("Player"), null));
        if (player.hasPermission("magic.command.magicmarket.taken.down") ||
                player.hasPermission("magic.command.magicmarket.all")) {
            lore.addAll(processLoreLines(getYmlProductLore("Op"), null));
        }
        return buildItemWithLore(getProduct(), lore);
    }

    public String getType() {
        return product.getType().name() + "::" + (product.getItemMeta().hasCustomModelData() ? product.getItemMeta().getCustomModelData() : "0");
    }

}
