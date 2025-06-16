package com.magicrealms.magicmarket.core.product;

import com.magicrealms.magiclib.bukkit.message.helper.AdventureHelper;
import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.FormatUtil;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.MagicLib;
import com.magicrealms.magiclib.core.menu.ConfirmMenu;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.product.IProductManager;
import com.magicrealms.magicmarket.api.product.Product;
import com.magicrealms.magicmarket.api.product.ProductStatus;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_CONFIG;

/**
 * @author Ryan-0916
 * @Desc 商品管理器
 * @date 2025-06-12
 */
public class ProductManager implements IProductManager {

    private final BukkitMagicMarket PLUGIN;

    public ProductManager(BukkitMagicMarket plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void sellProduct(Player seller, ItemStack product, BigDecimal price) {
        try {
            /* 物品为空 */
            if (ItemUtil.isAirOrNull(product)) {
                PLUGIN.sendMessage(seller, "PlayerMessage.Error.NullProduct");
                return;
            }
            /* 存在黑名单物品 */
            if (PLUGIN.getBlacklistManager().containsItem(product)) {
                if (product.getItemMeta() instanceof BundleMeta || product.getType() == Material.SHULKER_BOX) {
                    PLUGIN.sendMessage(seller, "PlayerMessage.Error.ProhibitedProductInContainers");
                } else {
                    PLUGIN.sendMessage(seller, "PlayerMessage.Error.ProhibitedProduct");
                }
                return;
            }
            /* TODO: 判断玩家上架商品的数量是否已经超出最大数量限制 */
            /* 根据税率计算服务费 */
            BigDecimal sellTax = BigDecimal.valueOf(PLUGIN.getConfigManager()
                    .getYmlValue(YML_CONFIG, "Settings.SellTax", 0D, ParseType.DOUBLE))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal serviceCharge = price.multiply(sellTax)
                    .setScale(2, RoundingMode.HALF_UP);
            Product marketProduct = Product
                    .builder(seller.getPlayer(), product, price)
                    .serviceCharge(serviceCharge)
                    .shelfLife(PLUGIN.getConfigManager()
                            .getYmlValue(YML_CONFIG, "Settings.ShelfLife", 7, ParseType.INTEGER))
                    .build();
            /* 构建确认操作所需的商品 */
            ItemStack confirmItem = product.clone();
            List<Component> lore = Optional.ofNullable(confirmItem.lore()).orElse(new ArrayList<>());
            Map<String, String> map = new HashMap<>();
            map.put("service_charge", FormatUtil.formatAmount(serviceCharge));
            map.put("price", FormatUtil.formatAmount(price));
            if (marketProduct.getShelfLife() > 0) {
                map.putAll(FormatUtil.formatDateTime(marketProduct.getExpirationTime(),
                        "expiration_time_"));
            }
            PLUGIN.getConfigManager().getYmlListValue(YML_CONFIG, "Settings.SellLore")
                    .ifPresent(sellLore ->
                    lore.addAll(sellLore.stream()
                            .map(e -> ItemUtil.UN_ITALIC.append(AdventureHelper.getMiniMessage().deserialize(AdventureHelper.legacyToMiniMessage(StringUtil.replacePlaceholders(e, map))))).toList()));
            confirmItem.lore(lore);
            /* 构建确认菜单 */
            new ConfirmMenu.Builder()
                    .player(seller)
                    .itemStack(confirmItem)
                    .cancelOrCloseTask(() -> PLUGIN.sendMessage(seller, "PlayerMessage.Success.SellCancel"))
                    .confirmTask(() -> {
                        if (!MagicLib.getInstance().getVaultManager().withdrawAmount(seller, serviceCharge)) {
                            PLUGIN.sendMessage(seller, "PlayerMessage.Error.ExpensiveServiceCharge");
                            return;
                        }
                        PLUGIN.getProductRepository().shellProduct(marketProduct);
                        seller.getInventory().setItemInMainHand(ItemUtil.AIR);
                        PLUGIN.sendMessage(seller, "PlayerMessage.Success.SellProduct");
                    }).open();
        } catch (Exception e) {
            PLUGIN.getLoggerManager().error("玩家上架物品时出现未知错误", e);
            PLUGIN.sendMessage(seller, "SystemUnKnowError");
        }
    }

    @Override
    public List<Product> queryOnSaleProducts() {
        return PLUGIN.getProductRepository().queryValidProducts().stream()
                .filter(e -> e != null && e.getStatus() == ProductStatus.ON_SALE)
                .collect(Collectors.toList());
    }

}
