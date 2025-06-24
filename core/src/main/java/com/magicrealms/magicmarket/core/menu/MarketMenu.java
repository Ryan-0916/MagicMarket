package com.magicrealms.magicmarket.core.menu;

import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;
import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_LANGUAGE;
import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_MARKET_MENU;

/**
 * @author Ryan-0916
 * @Desc 全球市场菜单
 * @date 2025-06-12
 */
public class MarketMenu extends AbstractMarketMenu {

    public MarketMenu(Player player) {
        this(player, null);
    }

    public MarketMenu(Player player, @Nullable Runnable backMenu) {
        super(BukkitMagicMarket.getInstance(), player,
                BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts(),
                YML_MARKET_MENU, "ABCDDDDDEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFG#####HIJ", backMenu);
        asyncOpenMenu();
    }

    @Override
    protected void handleMenuUnCache(String layout) {
        super.handleMenuUnCache(layout);
        int size =  layout.length();
        /* 当前显示的下标 */
        int appearIndex = ((getPage() - 1) * PAGE_COUNT) - 1;
        for (int i = 0; i < size; i++){
            char c = layout.charAt(i);
            if (c == 'C' || c == 'D' || c == 'E') { continue; }
            switch (c) {
                case 'A' -> setCheckBoxSlot(i, getBackMenuRunnable() != null);
                case 'F' -> {
                    if (data.size() > ++appearIndex) {
                        setItemSlot(i, data.get(appearIndex).getMenuProduct(getPlayer()));
                    } else {
                        setItemSlot(i, ItemUtil.AIR);
                    }
                }
                case 'G' -> setButtonSlot(i, !(getPage() > 1));
                case 'H' -> setButtonSlot(i, !(getPage() < getMaxPage()));
                case 'I' -> setItemSlot(i, sort.getItemSlot(c, getConfigPath()));
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
                allProducts = BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts();
                changeProducts();
            }
            case 'J' -> new MyMarketMenu(getPlayer(), this::asyncOpenMenu);
        }
    }

}
