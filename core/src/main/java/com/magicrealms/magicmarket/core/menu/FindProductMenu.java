package com.magicrealms.magicmarket.core.menu;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magiclib.core.MagicLib;
import com.magicrealms.magiclib.core.holder.PageMenuHolder;
import com.magicrealms.magiclib.core.menu.ConfirmMenu;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmail.api.util.PlayerInventoryUtil;
import com.magicrealms.magicmarket.api.MagicMarket;
import com.magicrealms.magicmarket.api.product.Product;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.magicrealms.magicmarket.core.menu.enums.MarketSort;
import com.magicrealms.magicmarket.core.menu.listener.FindProductMenuObserver;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_FIND_PRODUCT_MENU;
import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_LANGUAGE;

/**
 * @author Ryan-0916
 * @Desc 查找商品菜单
 * @date 2025-06-20
 */
public class FindProductMenu extends PageMenuHolder implements FindProductMenuObserver {

    /* 全部商品 */
    private List<Product> allProducts;

    /* 有效商品 */
    private List<Product> activeProducts;

    /* 排序 */
    private MarketSort sort = MarketSort.NEWEST;

    /* 搜索商品类型 */
    private String searchType;

    /* 搜索商品 */
    private ItemStack searchItem;

    /* 页面显示商品个数 */
    private final int PAGE_COUNT;

    /* 是否主动关闭 */
    private boolean manualClose;

    /* title 提示 */
    private String prompt;

    public FindProductMenu(Player player, @Nullable ItemStack itemStack) {
        this(player, itemStack, null);
    }

    public FindProductMenu(Player player, @Nullable ItemStack itemStack, @Nullable Runnable backMenu) {
        super(BukkitMagicMarket.getInstance(), player, YML_FIND_PRODUCT_MENU,
                "AB###C###DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDE#####FGH", false, true, backMenu);
        this.searchItem = ItemUtil.isAirOrNull(itemStack) ? ItemUtil.AIR : itemStack;
        this.searchType = ItemUtil.isAirOrNull(itemStack) ? "AIR::0" : searchItem.getType().name() + "::" + (searchItem.getItemMeta().hasCustomModelData() ? searchItem.getItemMeta().getCustomModelData() : "0");
        this.allProducts = BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts();
        this.activeProducts = allProducts.stream().filter(e -> StringUtils.equals(e.getType(), searchType))
                .collect(Collectors.toList());
        sort.sort(activeProducts);
        this.prompt = ItemUtil.isAirOrNull(searchItem) ?
                getConfigValue("CustomPapi.Prompt.Empty", "", ParseType.STRING):
                getConfigValue("CustomPapi.Prompt.Default", "", ParseType.STRING);
        this.PAGE_COUNT = StringUtils.countMatches(getLayout(), "D");
        setMaxPage(PAGE_COUNT <= 0 || activeProducts.isEmpty() ? 1 :
                activeProducts.size() % PAGE_COUNT == 0 ?
                        activeProducts.size() / PAGE_COUNT : activeProducts.size() / PAGE_COUNT + 1);
        asyncOpenMenu();
    }

    @Override
    protected void handleMenuUnCache(String layout) {
        int size =  layout.length();
        /* 当前显示的下标 */
        int appearIndex = ((getPage() - 1) * PAGE_COUNT) - 1;
        for (int i = 0; i < size; i++){
            char c = layout.charAt(i);
            switch (c) {
                case 'A' -> setCheckBoxSlot(i, getBackMenuRunnable() != null);
                case 'C' -> setItemSlot(i, searchItem);
                case 'D' -> {
                    if (activeProducts.size() > ++appearIndex){
                        setItemSlot(i, activeProducts.get(appearIndex).getMenuProduct(getPlayer()));
                    } else {
                        setItemSlot(i, ItemUtil.AIR);
                    }
                }
                case 'E' -> setButtonSlot(i, !(getPage() > 1));
                case 'F' -> setButtonSlot(i, !(getPage() < getMaxPage()));
                case 'G' -> setItemSlot(i, sort.getItemSlot(c, getConfigPath()));
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
                "prompt", prompt);
    }

    @Override
    public void changeSort() {
        sort.sort(activeProducts);
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
        this.activeProducts = allProducts.stream().filter(e -> StringUtils.equals(e.getType(), searchType))
                .collect(Collectors.toList());
        setMaxPage(PAGE_COUNT <= 0 || activeProducts.isEmpty() ? 1 :
                activeProducts.size() % PAGE_COUNT == 0 ?
                        activeProducts.size() / PAGE_COUNT : activeProducts.size() / PAGE_COUNT + 1);
        changeSort();
    }

    @Override
    public void changeSearch() {
        this.searchType = ItemUtil.isAirOrNull(searchItem) ? "AIR::0" : searchItem.getType().name() + "::" + (searchItem.getItemMeta().hasCustomModelData() ? searchItem.getItemMeta().getCustomModelData() : "0");
        this.prompt = ItemUtil.isAirOrNull(searchItem) ?
                getConfigValue("CustomPapi.Prompt.Empty", "", ParseType.STRING):
                getConfigValue("CustomPapi.Prompt.Default", "", ParseType.STRING);
        changeProducts();
    }

    @Override
    public void openEvent(InventoryOpenEvent e) {
        super.openEvent(e);
        this.manualClose = true;
        this.prompt = ItemUtil.isAirOrNull(searchItem) ?
                getConfigValue("CustomPapi.Prompt.Empty", "", ParseType.STRING):
                getConfigValue("CustomPapi.Prompt.Default", "", ParseType.STRING);
    }

    @Override
    public void closeEvent(InventoryCloseEvent e) {
        super.closeEvent(e);
        if (this.manualClose && ItemUtil.isNotAirOrNull(searchItem)) {
            /* 检索区物品退还逻辑 */
            PlayerInventoryUtil.givePlayerItems(super.getPlayer(), List.of(searchItem));
        }
    }

    @Override
    public void topInventoryClickEvent(InventoryClickEvent event, int slot) {
        if (!tryCooldown(slot, getPlugin().getConfigManager()
                .getYmlValue(YML_LANGUAGE,
                        "PlayerMessage.Error.ButtonCooldown"))) {
            return;
        }
        char c = getLayout().charAt(slot);
        event.setCancelled(c != 'C');
        asyncPlaySound("Icons." + c + ".Display.Sound");
        switch (c) {
            case 'A' -> backMenu();
            case 'E' -> changePage(-1, b -> {
                asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
                if (!b) return;
                handleMenu(getLayout());
                asyncUpdateTitle();
            });
            case 'F' -> changePage(1, b -> {
                asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
                if (!b) return;
                handleMenu(getLayout());
                asyncUpdateTitle();
            });
            case 'G' -> {
                this.sort = sort.next();
                changeSort();
            }
            case 'D' -> clickProduct(event, slot);
            /* 刷新商品 */
            case 'B' -> {
                this.allProducts = BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts();
                changeProducts();
            }
            /* C */
            case 'C' -> Bukkit.getScheduler().runTask(getPlugin(), () -> {
                searchItem = getInventory().getItem(slot);
                changeSearch();
            });
            case 'H' -> new MyMarketMenu(getPlayer(), this::asyncOpenMenu);
        }
    }

    @Override
    public void dragEvent(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    @SuppressWarnings("DuplicatedCode")
    private void clickProduct(InventoryClickEvent event, int slot) {
        if (ItemUtil.isAirOrNull(event.getCurrentItem())) {
            return;
        }
        int index = StringUtils.countMatches(getLayout().substring(0, slot), "D");
        Product product = activeProducts.get((getPage() - 1) * PAGE_COUNT + index);
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
        getPlayer().sendMessage("没做");
    }

    private void buyProduct(Product product) {
        setDisabledCloseSound(true);
        this.manualClose = false;
        new ConfirmMenu.Builder().player(getPlayer())
                .itemStack(product.getDefaultLoreProduct())
                .cancelOrCloseTask(() -> Bukkit.getScheduler().runTask(getPlugin(), () -> {
                    if (getPlayer().isOnline()) {
                        asyncOpenMenu();
                        return;
                    }
                    PlayerInventoryUtil.givePlayerItems(getPlayer(), List.of(searchItem));
                }))
                .confirmTask(() -> {
                    MagicMarket.getInstance().getProductManager().buyProduct(getPlayer(), product);
                    PlayerInventoryUtil.givePlayerItems(getPlayer(), List.of(searchItem));
                })
                .open();
    }




}
