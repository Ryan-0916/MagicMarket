package com.magicrealms.magicmarket.core.menu;

import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.magicrealms.magicplayer.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.*;

/**
 * @author Ryan-0916
 * @Desc 玩家市场
 * @date 2025-06-19
 */
@SuppressWarnings("unused")
public class PlayerMarketMenu  extends AbstractMarketMenu {

    private final PlayerData OWNER;

    public PlayerMarketMenu(Player player, PlayerData owner) {
        this(player, owner, null);
    }

    public PlayerMarketMenu(Player player, PlayerData owner, @Nullable Runnable backMenu) {
        super(BukkitMagicMarket.getInstance(), player,
                BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts().stream().filter(e -> e.getSellerUniqueId().equals(owner.getUniqueId())).collect(Collectors.toList()),
                YML_PLAYER_MARKET_MENU, "ABCDDDDDEFFFFFFFFFFFFFFFFFFFFFFFFFFFG#####HIJ", backMenu);
        this.OWNER = owner;
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
                case 'J' -> setItemSlot(i, ItemUtil.setItemStackByConfig(OWNER.getHeadStack(),
                        getPlugin().getConfigManager(), getConfigPath(), "Icons.K.Display", Map.of("owner_name", OWNER.getName())));
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
                allProducts = BukkitMagicMarket.getInstance().getProductManager().queryOnSaleProducts()
                        .stream().filter(e -> e.getSellerUniqueId().equals(OWNER.getUniqueId())).collect(Collectors.toList());
                changeProducts();
            }
            case 'K' -> new MyMarketMenu(getPlayer(), this::asyncOpenMenu);
        }
    }

    public Map<String, String> createPlaceholders() {
        Map<String, String> map = super.createPlaceholders();
        map.put("owner_name", OWNER.getName());
        return map;
    }


}
