package com.magicrealms.magicmarket.core.command;

import com.magicrealms.magiclib.bukkit.command.annotations.Command;
import com.magicrealms.magiclib.bukkit.command.annotations.CommandListener;
import com.magicrealms.magiclib.bukkit.command.enums.PermissionType;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.magicrealms.magicmarket.core.menu.FindProductMenu;
import com.magicrealms.magicmarket.core.menu.MarketMenu;
import com.magicrealms.magicmarket.core.menu.MyMarketMenu;
import com.magicrealms.magicmarket.core.menu.PlayerMarketMenu;
import com.magicrealms.magicplayer.api.MagicPlayerAPI;
import com.magicrealms.magicplayer.api.player.PlayerData;
import com.magicrealms.magicplayer.api.player.PlayerMenu;
import com.magicrealms.magicplayer.api.player.click.ClickAction;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_CONFIG;

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

    /**
     * 打开个人市场：使用方法：</market me>
     * @param sender 发送人
     * @param args 参数类型
     */
    @Command(text = "^Me$",
            permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.market.me", label = "^market$")
    public void marketMe(Player sender, String[] args){
        new MyMarketMenu(sender);
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

    /**
     * 打开在线玩家的市场：使用方法：</market open>
     * @param sender 发送人
     * @param args 参数类型
     */
    @Command(text = "^Open$",
            permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.see", label = "^market$")
    public void open(Player sender, String[] args){
        List<PlayerData> data = MagicPlayerAPI.getInstance().getOnlinePlayerNames()
                .stream()
                .filter(name -> !StringUtils.equalsIgnoreCase(name, sender.getName()))
                .map(name -> MagicPlayerAPI.getInstance().queryPlayerData(name))
                .filter(Objects::nonNull)
                .toList();
        new PlayerMenu.Builder()
                .leftAction(ClickAction.of(BukkitMagicMarket.getInstance().getConfigManager().getYmlValue(YML_CONFIG, "Settings.OpenPlayerMarketLore"),
                        e ->
                                new PlayerMarketMenu(e.clicker(), e.clickData())))
                .data(data)
                .player(sender)
                .open();
    }

    /**
     * 打开其他玩家的市场：使用方法：</market open PlayerName>
     * @param sender 发送人
     * @param args 参数类型
     */
    @Command(text = "^Open\\s\\S+$",
            permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.see", label = "^market$")
    public void openByPlayerName(Player sender, String[] args){
        PlayerData playerData = MagicPlayerAPI.getInstance().queryPlayerData(args[1]);
        if (playerData == null) {
            BukkitMagicMarket.getInstance().sendMessage(sender, "PlayerMessage.Error.UnKnowPlayer");
            return;
        }
        new PlayerMarketMenu(sender, playerData);
    }

    /**
     * 查询市场内的物品：使用方法：</market find>
     * @param sender 发送人
     * @param args 参数类型
     */
    @Command(text = "^Find$",
            permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.find", label = "^market$")
    public void find(Player sender, String[] args){
        new FindProductMenu(sender,
                sender.getInventory().getItemInMainHand());
        sender.getInventory().setItemInMainHand(ItemUtil.AIR);
    }

}
