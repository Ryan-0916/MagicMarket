package com.magicrealms.magicmarket.core.menu;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.FormatUtil;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.stall.PlayerStallData;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.magicrealms.magicplayer.api.MagicPlayerAPI;
import com.magicrealms.magicplayer.api.player.PlayerData;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.*;

/**
 * @author Ryan-0916
 * @Desc 我的市场
 * @date 2025-06-21
 */
public class MyMarketMenu extends AbstractMarketMenu {

    private final PlayerData OWNER;
    /* 服务器最大上架数量 */
    private int serverMaxSellNumber;
    /* 玩家摊位信息 */
    private PlayerStallData stallData;

    public MyMarketMenu(Player player) {
        this(player, null);
    }

    public MyMarketMenu(Player player, @Nullable Runnable backMenu) {
        super(BukkitMagicMarket.getInstance(), player,
                BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts().stream().filter(e -> e.getSellerUniqueId()
                        .equals(player.getUniqueId())).collect(Collectors.toList()),
                YML_MY_MARKET_MENU, "ABCDDDDDEFFFFFFFFFFFFFFFFFFFFFFFFFFFG#####HIJ",
                backMenu);
        this.OWNER = MagicPlayerAPI.getInstance().queryPlayerData(player);
        this.serverMaxSellNumber = Math.max(getPlugin().getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Max", 50, ParseType.INTEGER), 0);
        this.stallData = BukkitMagicMarket.getInstance().getPlayerStallDataManager().queryStallData(player);
        /* 当前分类可上架的物品 */
        this.serverMaxSellNumber = Math.min(stallData.getTotalCount(), Math.max((serverMaxSellNumber - allProducts.size() + data.size()), 0));
        setMaxPage(PAGE_COUNT <= 0 || serverMaxSellNumber == 0
                ? 1 : (int) Math.ceil((double) serverMaxSellNumber / PAGE_COUNT));
        asyncOpenMenu();
    }

    @Override
    public void changeCategory() {
        this.data = CATEGORY_CACHE.get(CATEGORY_NAMES.get(cActive));
        this.serverMaxSellNumber =  Math.max((serverMaxSellNumber - allProducts.size() + data.size()), 0);
        setMaxPage(PAGE_COUNT <= 0 || serverMaxSellNumber == 0
                ? 1 : (int) Math.ceil((double) serverMaxSellNumber / PAGE_COUNT));
        changeSort();
    }

    @Override
    protected void handleMenuUnCache(String layout) {
        super.handleMenuUnCache(layout);
        int size =  layout.length();
        /* 当前显示的下标 */
        int appearIndex = ((getPage() - 1) * PAGE_COUNT) - 1;

        Map<String, String> map = new HashMap<>();
        map.put("max_sell_number", String.valueOf(serverMaxSellNumber));
        map.put("player_name", OWNER.getName());
        map.put("sell_number", String.valueOf(allProducts.size()));
        for (int i = 0; i < size; i++){
            char c = layout.charAt(i);
            if (c == 'C' || c == 'D' || c == 'E') { continue; }
            switch (c) {
                case 'A' -> setCheckBoxSlot(i, getBackMenuRunnable() != null);
                case 'F' -> {
                    if (serverMaxSellNumber > ++appearIndex){
                        if (data.size() > appearIndex) {
                            /* 如果有商品正在出售 */
                            setItemSlot(i, data.get(appearIndex).getMenuProduct(getPlayer()));
                        } else if (appearIndex < stallData.getTotalCount() - (allProducts.size() - data.size())){
                            /* 如果未被解锁 */
                            setItemSlot(i, ItemUtil.getItemStackByConfig(getPlugin().getConfigManager(), getConfigPath(), "Icons.F.UnlockDisplay"));
                        } else if (stallData.getTotalCount() + stallData.getMaxPurchasableCount() - stallData.getPurchasedCount() > appearIndex) {
                            /* 计算该槽位是否是玩家可购买的槽位 */
                            BigDecimal amount = BigDecimal.valueOf(Math.min(getPlugin().getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.First", 50D, ParseType.DOUBLE) +
                                    getPlugin().getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.Growth", 50D, ParseType.DOUBLE) * stallData.getPurchasedCount(),
                                    getPlugin().getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Buy.Max", 20000D, ParseType.DOUBLE)));
                            map.put("amount", FormatUtil.formatAmount(amount));
                            setItemSlot(i, ItemUtil.getItemStackByConfig(getPlugin().getConfigManager(), getConfigPath(), "Icons.F.LockDisplay", map));
                        } else {
                            /* 该槽位非玩家可购买的槽位 */
                            setItemSlot(i, ItemUtil.getItemStackByConfig(getPlugin().getConfigManager(), getConfigPath(), "Icons.F.DisabledLockDisplay"));
                        }
                    } else {
                        setItemSlot(i, ItemUtil.AIR);
                    }
                }
                case 'G' -> setButtonSlot(i, !(getPage() > 1));
                case 'H' -> setButtonSlot(i, !(getPage() < getMaxPage()));
                case 'I' -> setItemSlot(i, sort.getItemSlot(c, getConfigPath()));
                case 'J' -> setItemSlot(i, ItemUtil.setItemStackByConfig(OWNER.getHeadStack(),
                        getPlugin().getConfigManager(), getConfigPath(), "Icons.J.Display", getPlayer()));
                default -> setItemSlot(i);
            }
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
        asyncPlaySound("Icons." + c + ".Display.Sound");
        switch (c) {
            case 'A' -> backMenu();
            case 'C' -> changeCPage(-1, c);
            case 'D' -> clickCategory(slot);
            case 'E' -> changeCPage(1, c);
            case 'G' -> changePage(-1, c);
            case 'H' -> changePage(1, c);
            case 'I' -> clickSort();
            case 'F' -> clickProduct(event, slot);
            /* 刷新商品 */
            case 'B' -> {
                this.serverMaxSellNumber = Math.max(getPlugin().getConfigManager().getYmlValue(YML_CONFIG, "Settings.SellNumber.Max", 50, ParseType.INTEGER), 0);
                this.stallData = BukkitMagicMarket.getInstance().getPlayerStallDataManager().queryStallData(getPlayer());
                this.allProducts = BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts()
                        .stream().filter(e -> e.getSellerUniqueId().equals(OWNER.getUniqueId())).collect(Collectors.toList());
                changeProducts();
            }
        }
    }

    /**
     * 选中某个商品或解锁某项
     * @param event 事件
     * @param clickSort 选中的槽
     */
    @Override
    protected void clickProduct(InventoryClickEvent event, int clickSort) {
        Player player = getPlayer();
        int index = (getPage() - 1) * PAGE_COUNT + StringUtils.countMatches(getLayout().substring(0, clickSort), "F");
        /* 禁止播放关闭声音 */
        setDisabledCloseSound(false);
        if (data.size() > index) {
            /* 下架商品 */
            BukkitMagicMarket.getInstance().getProductManager()
                    .removeProduct(getPlayer(), data.get(index), this::asyncOpenMenu, this::asyncCloseMenu, this::asyncCloseMenu);
        } else if (index + 1 > stallData.getTotalCount() - (allProducts.size() - data.size())) {
            /* 购买槽位 */
            BukkitMagicMarket.getInstance().getPlayerStallDataManager().purchaseStall(player, this::asyncOpenMenu,
                    this::asyncCloseMenu, () -> {
                        stallData = BukkitMagicMarket.getInstance().getPlayerStallDataManager().queryStallData(getPlayer());
                        if (stallData.getTotalCount() > PAGE_COUNT) {
                            setCurrentPage((int) Math.ceil((double) stallData.getTotalCount() / PAGE_COUNT));
                        } else {
                            goToFirstPage();
                        }
                        cleanItemCache();
                        asyncOpenMenu();
                    });
        }
    }

}

