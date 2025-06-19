package com.magicrealms.magicmarket.core.command;

import com.magicrealms.magiclib.bukkit.command.annotations.CommandListener;
import com.magicrealms.magiclib.bukkit.command.annotations.TabComplete;
import com.magicrealms.magiclib.bukkit.command.enums.PermissionType;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Ryan-0916
 * @Desc 环球市场部分命令补全
 * @date 2025-06-19
 */
@CommandListener
@SuppressWarnings("unused")
public class MarketTabController {

    @TabComplete(text = "^\\s?$", permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.market.me", label = "^market$")
    public List<String> firstMe(CommandSender sender, String[] args) {
        return Stream.of("me")
                .toList();
    }

    @TabComplete(text = "^\\s?$", permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.sell", label = "^market$")
    public List<String> firstSell(CommandSender sender, String[] args) {
        return Stream.of("sell")
                .toList();
    }

    @TabComplete(text = "^\\S+$", permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.market.me", label = "^level$")
    public List<String> firstMeTab(CommandSender sender, String[] args) {
        return Stream.of("me")
                .filter(e ->
                        StringUtils.startsWithIgnoreCase(e, args[0]))
                .toList();
    }

    @TabComplete(text = "^\\S+$", permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.sell", label = "^market$")
    public List<String> firstSellTab(CommandSender sender, String[] args) {
        return Stream.of("sell")
                .filter(e ->
                        StringUtils.startsWithIgnoreCase(e, args[0]))
                .toList();
    }

    @TabComplete(text = "^Sell\\s$", permissionType = PermissionType.PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.sell", label = "^market$")
    public List<String> secondSell(CommandSender sender, String[] args) {
        return List.of("价格");
    }

}
