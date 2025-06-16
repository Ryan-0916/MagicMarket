package com.magicrealms.magicmarket.core.command;

import com.magicrealms.magiclib.bukkit.command.annotations.Command;
import com.magicrealms.magiclib.bukkit.command.annotations.CommandListener;
import com.magicrealms.magiclib.bukkit.command.enums.PermissionType;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.magicrealms.magicmarket.core.menu.MarketMenu;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

/**
 * @author Ryan-0916
 * @Desc 环球市场部分命令
 * @date 2025-06-12
 */
@CommandListener
@SuppressWarnings("unused")
public class MarketController {

    @Command(text = "^\\s?$",
            permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.market", label = "^market$")
    public void market(Player sender, String[] args){
        new MarketMenu(sender);
    }

    @Command(text = "^Me$",
            permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.market.me", label = "^market$")
    public void marketMe(Player sender, String[] args){
        // new MarketMenu(sender);
    }

    /**
     * 上架商品，使用方法：</market sell 价格>
     * @param sender 发送人
     * @param args 参数类型
     */
    @Command(text = "^Sell\\s\\d{1,8}(\\.\\d{1,2})?$",
            permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.sell", label = "^market$")
    public void sellProduct(Player sender, String[] args){
        try {
            BukkitMagicMarket.getInstance().getProductManager().sellProduct(sender,
                    sender.getInventory().getItemInMainHand(),
                    new BigDecimal(args[1]));
        } catch (Exception e) {
            BukkitMagicMarket.getInstance().sendMessage(sender, "PlayerMessage.Error.ParameterError");
        }
    }

}
