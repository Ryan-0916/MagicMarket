package com.magicrealms.magicmarket.core.menu;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.MagicLib;
import com.magicrealms.magiclib.core.holder.PageMenuHolder;
import com.magicrealms.magiclib.core.menu.ConfirmMenu;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.MagicMarket;
import com.magicrealms.magicmarket.api.product.Product;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.magicrealms.magicmarket.core.menu.enums.MarketSort;
import com.magicrealms.magicmarket.core.menu.listener.ProductDetailMenuObserver;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_LANGUAGE;
import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_PRODUCT_DETAIL_MENU;

/**
 * @author Ryan-0916
 * @Desc 物品详情菜单
 * 显示该物品全部在售的商品
 * @date 2025-06-18
 */
public class ProductDetailMenu extends PageMenuHolder implements ProductDetailMenuObserver {

    private final Product DATA;
    private List<Product> marketItems;
    private List<Product> activeItems;
    protected MarketSort sort = MarketSort.NEWEST;
    protected final int PAGE_COUNT;
    /* 金币是否足够 */
    private boolean sufficientAmount;
    /* title 提示 */
    private String prompt;

    public ProductDetailMenu(Player player, Product product, @Nullable Runnable backMenu) {
        super(BukkitMagicMarket.getInstance(), player, YML_PRODUCT_DETAIL_MENU, "AB##C###DEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEF#####GHI", backMenu);
        this.DATA = product;
        this.marketItems = BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts();
        this.activeItems = marketItems.stream().filter(v -> !StringUtils.equals(v.getId(), DATA.getId())
                && StringUtils.equals(DATA.getType(), v.getType())).collect(Collectors.toList());
        sort.sort(activeItems);
        this.PAGE_COUNT = StringUtils.countMatches(getLayout(), "E");
        setMaxPage(PAGE_COUNT <= 0 || activeItems.isEmpty() ? 1 :
                activeItems.size() % PAGE_COUNT == 0 ?
                        activeItems.size() / PAGE_COUNT : activeItems.size() / PAGE_COUNT + 1);
        this.sufficientAmount = MagicLib.getInstance().getVaultManager().sufficientAmount(player, product.getPrice());
        this.prompt = getConfigValue("CustomPapi.Prompt.Default", "", ParseType.STRING);
        asyncOpenMenu();
    }

    @Override
    public void changeSort() {
        sort.sort(activeItems);
        /* 释放缓存 */
        cleanItemCache();
        /* 回退至第一页 */
        goToFirstPage();
        /* 处理菜单 */
        handleMenu(getLayout());
        /* 异步修改菜单标题 */
        asyncUpdateTitle();
    }

    @Override
    public void changeProducts() {
        this.activeItems = marketItems.stream().filter(v -> !StringUtils.equals(v.getId(), DATA.getId())
                && StringUtils.equals(DATA.getType(), v.getType())).collect(Collectors.toList());
        setMaxPage(PAGE_COUNT <= 0 || activeItems.isEmpty() ? 1 :
                activeItems.size() % PAGE_COUNT == 0 ?
                        activeItems.size() / PAGE_COUNT : activeItems.size() / PAGE_COUNT + 1);
        changeSort();
    }

    @Override
    protected void handleMenuUnCache(String layout) {
        int size = layout.length();
        int appearIndex = ((getPage() - 1) * PAGE_COUNT) - 1;
        for (int i = 0; i < size; i++){
            char c = layout.charAt(i);
            switch (c) {
                case 'A' -> setCheckBoxSlot(i, getBackMenuRunnable() != null);
                case 'C' -> setItemSlot(i, DATA.getDefaultLoreProduct());
                case 'D' -> setButtonSlot(i, !sufficientAmount);
                case 'E' -> {
                    if (activeItems.size() > ++appearIndex) {
                        setItemSlot(i, activeItems.get(appearIndex).getMenuProduct(getPlayer()));
                    } else {
                        setItemSlot(i, ItemUtil.AIR);
                    }
                }
                case 'F' -> setButtonSlot(i, !(getPage() > 1));
                case 'G' -> setButtonSlot(i, !(getPage() < getMaxPage()));
                case 'H' -> setItemSlot(i, sort.getItemSlot(c, getConfigPath()));
                default -> setItemSlot(i);
            }
        }
    }

    @Override
    protected LinkedHashMap<String, String> processHandTitle(LinkedHashMap<String, String> title) {
        Map<String, String> map = createPlaceholders();
        return title
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry)
                        -> StringUtil.replacePlaceholders(entry.getValue(), map), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    }

    public Map<String, String> createPlaceholders() {
        return Map.of("sort", getConfigValue(String.format("CustomPapi.Sort.%s", sort.getPath()), "", ParseType.STRING),
                "prompt", prompt, "buy_button", getCustomPapiText("BuyButton", sufficientAmount));
    }

    @Override
    public void topInventoryClickEvent(InventoryClickEvent event, int slot) {
        if (!tryCooldown(slot, getPlugin().getConfigManager()
                .getYmlValue(YML_LANGUAGE,
                        "PlayerMessage.Error.ButtonCooldown"))) {
            return;
        }
        char c = getLayout().charAt(slot);
        asyncPlaySound("Icons." + c + ".Display.Sound");
        switch (c) {
            case 'A' -> backMenu();
            case 'F' -> changePage(1, b -> {
                asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
                if (!b) return;
                handleMenu(getLayout());
                asyncUpdateTitle();
            });
            case 'G' -> changePage(-1, b -> {
                asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
                if (!b) return;
                handleMenu(getLayout());
                asyncUpdateTitle();
            });
            case 'H' -> {
                this.sort = sort.next();
                changeSort();
            }
            case 'D' -> {
                if (sufficientAmount) {
                   buyProduct(DATA);
                }
            }
            case 'E' -> clickProduct(event, slot);
            /* 刷新商品 */
            case 'B' -> {
                this.marketItems = BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts();
                this.sufficientAmount = MagicLib.getInstance().getVaultManager().sufficientAmount(getPlayer(), DATA.getPrice());
                changeProducts();
            }
            /* 我的市场 */
            case 'I' -> new MyMarketMenu(getPlayer(), this::asyncOpenMenu);
        }
    }

    private void buyProduct(Product product) {
        setDisabledCloseSound(true);
        new ConfirmMenu.Builder().player(getPlayer())
                .itemStack(product.getDefaultLoreProduct())
                .cancelOrCloseTask(this::asyncOpenMenu)
                .confirmTask(() -> MagicMarket.getInstance().getProductManager().buyProduct(getPlayer(), product))
                .open();
    }

    @SuppressWarnings("DuplicatedCode")
    private void clickProduct(InventoryClickEvent event, int slot) {
        int index = StringUtils.countMatches(getLayout().substring(0, slot), "E");
        Product product = activeItems.get((getPage() - 1) * PAGE_COUNT + index);
        /* 是否为下架商品 */
        boolean downProduct = getPlayer().getUniqueId().equals(product.getSellerUniqueId())
                || ((getPlayer().hasPermission("magic.command.magicmarket.taken.down") || getPlayer().hasPermission("magic.command.magicmarket.all")) && event.isShiftClick() && event.isLeftClick());
        if (!downProduct) {
            /* 购买商品 */
            if (MagicLib.getInstance().getVaultManager().sufficientAmount(getPlayer(), product.getPrice())) {
                buyProduct(product);
                return;
            }
            this.prompt = getConfigValue("CustomPapi.Prompt.InsufficientAmount", "", ParseType.STRING);
            asyncUpdateTitle();
            return;
        }
        /* 下架商品 */
        BukkitMagicMarket.getInstance().getProductManager()
                .removeProduct(getPlayer(), product, this::asyncOpenMenu, this::asyncCloseMenu, this::asyncCloseMenu);
    }

    @Override
    public void openEvent(InventoryOpenEvent e) {
        super.openEvent(e);
        this.prompt = getConfigValue("CustomPapi.Prompt.Default", "", ParseType.STRING);
    }

}
