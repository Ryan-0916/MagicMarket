package com.magicrealms.magicmarket.core.product;

import com.magicrealms.magiclib.bukkit.message.helper.AdventureHelper;
import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.FormatUtil;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.MagicLib;
import com.magicrealms.magiclib.core.entity.InputValidatorResult;
import com.magicrealms.magiclib.core.menu.ConfirmMenu;
import com.magicrealms.magiclib.core.menu.InputMenu;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.MagicMailAPI;
import com.magicrealms.magicmail.api.mail.AttachmentItem;
import com.magicrealms.magicmail.api.mail.Mail;
import com.magicrealms.magicmail.api.mail.MailAttachment;
import com.magicrealms.magicmail.api.util.PlayerInventoryUtil;
import com.magicrealms.magicmarket.api.product.IProductManager;
import com.magicrealms.magicmarket.api.product.Product;
import com.magicrealms.magicmarket.api.product.ProductStatus;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.magicrealms.magicmarket.core.exception.BuyProductException;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.*;

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
    public List<Product> queryOnSaleProducts() {
        return PLUGIN.getProductRepository().queryOnSaleProducts().stream()
                .filter(Objects::nonNull).collect(Collectors.toList());
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
            /* 可上架的最大数量 */
            if (PLUGIN.getPlayerStallDataManager().queryStallData(seller).getTotalCount() <= queryOnSaleProducts().stream().
                    filter(e -> e.getSellerUniqueId().equals(seller.getUniqueId())).count()){
                PLUGIN.sendMessage(seller, "PlayerMessage.Error.ProductReachedLimit");
                return;
            }
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
                map.putAll(FormatUtil.formatDateTime(marketProduct.getExpirationTime(), "expiration_time_"));
            }
            PLUGIN.getConfigManager().getYmlListValue(YML_CONFIG, "Settings.ProductLore.SellLore")
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
                        PLUGIN.getProductRepository().insert(marketProduct);
                        seller.getInventory().setItemInMainHand(ItemUtil.AIR);
                        PLUGIN.sendMessage(seller, "PlayerMessage.Success.SellProduct");
                    }).open();
        } catch (Exception e) {
            PLUGIN.getLoggerManager().error("玩家上架物品时出现未知错误", e);
            PLUGIN.sendMessage(seller, "SystemUnKnowError");
        }
    }

    @Override
    public void buyProduct(Player buyer, Product product) {
        /* 玩家的余额无法购买该商品时 */
        if (!MagicLib.getInstance().getVaultManager().withdrawAmount(buyer, product.getPrice())) {
            PLUGIN.sendMessage(buyer, "PlayerMessage.Error.BuyProductInsufficientBalance");
            return;
        }
        /* 修改数据缓存中的状态 */
        boolean success = PLUGIN.getProductRepository().update(product.getId(), e -> {
            if (e.getStatus() != ProductStatus.ON_SALE) { throw new BuyProductException("购买失败，商品已经不在市场内"); }
            e.setStatus(ProductStatus.BE_SALE);
        }, true);
        if (!success) {
            PLUGIN.sendMessage(buyer, "PlayerMessage.Error.ProductStatusChange");
            /* 此步骤需要回滚玩家余额 */
            MagicLib.getInstance().getVaultManager().depositAmount(buyer, product.getPrice());
            return;
        }
        /* 发货 */
        PlayerInventoryUtil.givePlayerItems(buyer, List.of(product.getProduct().clone()));
        /* 给予卖家金钱 */
        String formatPrice = FormatUtil.formatAmount(product.getPrice());
        if (!MagicLib.getInstance().getVaultManager().depositAmount(product.getSellerUniqueId(), product.getPrice())) {
            PLUGIN.getLoggerManager().warning("玩家 " + product.getSellerName() + " 有一款商品被出售，但是该玩家未能正常的获取到这笔筹款，总金额：" + formatPrice);
        }
        PLUGIN.sendMessage(buyer, "PlayerMessage.Success.BuyProduct");
        PLUGIN.sendBungeeMessage(product.getSellerName(), "PlayerMessage.Success.ProductForSale", Map.of(
                "player_name", buyer.getName(),
                "product_price", formatPrice,
                "product_name", AdventureHelper.serializeComponent(ItemUtil.getItemName(product.getProduct())))
        );
    }

    /* Todo: 待优化 */

    @Override
    public void removeProduct(Player remover, Product product, Runnable cancelTask, Runnable errorTask, Runnable successTask) {
        /* 如果是本人下架则无需理由 */
        if (remover.getUniqueId().equals(product.getSellerUniqueId())) {
            /* 构建确认操作所显示的 ICON */
            ItemStack confirmItem = product.getProduct().clone();
            List<Component> lore = Optional.ofNullable(confirmItem.lore()).orElse(new ArrayList<>());
            PLUGIN.getConfigManager().getYmlListValue(YML_CONFIG, "Settings.ProductLore.SelfSoldOut")
                    .ifPresent(sellLore ->
                            lore.addAll(sellLore.stream()
                                    .map(e -> ItemUtil.UN_ITALIC.append(AdventureHelper.getMiniMessage().deserialize(AdventureHelper.legacyToMiniMessage(e)))).toList()));
            confirmItem.lore(lore);
            new ConfirmMenu.Builder().player(remover)
                    .itemStack(confirmItem)
                    .cancelOrCloseTask(cancelTask)
                    .confirmTask(() -> {
                        /* 下架操作 */
                        boolean success = PLUGIN.getProductRepository().update(product.getId(), e -> {
                            if (e.getStatus() != ProductStatus.ON_SALE) { throw new BuyProductException("下架失败，商品已经不在市场内"); }
                            e.setStatus(ProductStatus.SELLER_REMOVAL);
                            e.setRemovalName(remover.getName());
                            e.setRemovalReasons("本人下架");
                        }, true);
                        if (!success) {
                            PLUGIN.sendMessage(remover, "PlayerMessage.Error.ProductStatusChange");
                            errorTask.run();
                            return;
                        }
                        /* 发货至邮箱 */
                        MagicMailAPI.getInstance().sendMail(Mail.builder(remover)
                                .content(PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.MailFormat.Removal.Self.Content"))
                                .subject(PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.MailFormat.Removal.Self.Subject"))
                                .attachment(MailAttachment.builder()
                                        .items(List.of(new AttachmentItem(product.getProduct())))
                                        .build()).build());
                        PLUGIN.sendMessage(remover, "PlayerMessage.Success.RemoveProduct");
                        successTask.run();
                    })
                    .open();
            return;
        }

        /* 非本人 */
        /* TODO: 优化 */
        ItemStack itemStack = new ItemStack(Material.STONE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(Component.text("请输入下架理由"));
        itemStack.setItemMeta(itemMeta);
        new InputMenu.Builder().player(remover)
                .plugin(PLUGIN)
                .cancelTask(cancelTask)
                .itemStack(itemStack)
                .customValidator(e -> InputValidatorResult.ofValid(StringUtils.EMPTY))
                .confirmConsumer(reasons -> {
                    /* 确认 */
                    /* 构建确认操作所显示的 ICON */
                    ItemStack confirmItem = product.getProduct().clone();
                    Map<String, String> map = Map.of("removal_reasons", reasons, "removal_name", remover.getName());
                    List<Component> lore = Optional.ofNullable(confirmItem.lore()).orElse(new ArrayList<>());
                    PLUGIN.getConfigManager().getYmlListValue(YML_CONFIG, "Settings.ProductLore.OpSoldOut")
                            .ifPresent(sellLore ->
                                    lore.addAll(sellLore.stream()
                                            .map(l -> ItemUtil.UN_ITALIC.append(AdventureHelper.getMiniMessage().deserialize(AdventureHelper.legacyToMiniMessage(StringUtil.replacePlaceholders(l, map))))).toList()));
                    confirmItem.lore(lore);
                    new ConfirmMenu.Builder().player(remover)
                            .itemStack(confirmItem)
                            .cancelOrCloseTask(cancelTask)
                            .confirmTask(() -> {
                                /* 下架操作 */
                                boolean success = PLUGIN.getProductRepository().update(product.getId(), e -> {
                                    if (e.getStatus() != ProductStatus.ON_SALE) { throw new BuyProductException("下架失败，商品已经不在市场内"); }
                                    e.setStatus(ProductStatus.SELLER_REMOVAL);
                                    e.setRemovalName(remover.getName());
                                    e.setRemovalReasons(reasons);
                                }, true);
                                if (!success) {
                                    PLUGIN.sendMessage(remover, "PlayerMessage.Error.ProductStatusChange");
                                    errorTask.run();
                                    return;
                                }
                                /* 发货至邮箱 */
                                MagicMailAPI.getInstance().sendMail(Mail.builder(remover, product.getSellerUniqueId(), product.getSellerName())
                                        .content(PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.MailFormat.Removal.Admin.Content"))
                                        .subject(StringUtil.replacePlaceholders(PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, "Settings.MailFormat.Removal.Admin.Subject"), map))
                                        .attachment(MailAttachment.builder()
                                                .items(List.of(new AttachmentItem(product.getProduct())))
                                                .build()).build());
                                PLUGIN.sendMessage(remover, "PlayerMessage.Success.RemoveProduct");
                                successTask.run();
                            })
                            .open();
                }).resultSlot(1)
                .open();
    }


    @Override
    public void removeProductBySystem(Product product, String reasons) {

    }

}
