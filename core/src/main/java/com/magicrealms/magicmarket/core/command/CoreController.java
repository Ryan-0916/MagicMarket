package com.magicrealms.magicmarket.core.command;

import com.magicrealms.magiclib.bukkit.command.annotations.Command;
import com.magicrealms.magiclib.bukkit.command.annotations.CommandListener;
import com.magicrealms.magiclib.bukkit.command.enums.PermissionType;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import org.bukkit.command.CommandSender;

import java.util.Locale;

import static com.magicrealms.magicplayer.common.MagicPlayerConstant.*;

/**
 * @author Ryan-0916
 * @Desc 核心部分命令
 * @date 2025-05-02
 */
@CommandListener
@SuppressWarnings("unused")
public class CoreController {

    private void setupCommon() {
        /* 重置分类部分 */
        BukkitMagicMarket.getInstance().setupCategoryManager();
        /* 重置黑名单部分 */
        BukkitMagicMarket.getInstance().setupBlacklistManager();
    }

    @Command(text = "^Reload$",
            permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.reload", label = "^magicMarket$")
    public void reload(CommandSender sender, String[] args){
        BukkitMagicMarket.getInstance().getConfigManager()
                .reloadConfig(YML_REDIS, YML_MONGODB);
        setupCommon();
        BukkitMagicMarket.getInstance().sendMessage(sender, "PlayerMessage.Success.ReloadFile");
    }

    @Command(text = "^Reload\\sAll$",
            permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.reload", label = "^magicMarket$")
    public void reloadAll(CommandSender sender, String[] args){
        BukkitMagicMarket.getInstance().getConfigManager().reloadAllConfig();
        /* 重置 Redis 部分 */
        BukkitMagicMarket.getInstance().setupRedisStore();
        /* 重置 MongoDB 部分 */
        BukkitMagicMarket.getInstance().setupMongoDB();
        setupCommon();
        BukkitMagicMarket.getInstance().sendMessage(sender, "PlayerMessage.Success.ReloadFile");
    }

    @Command(text = "^Reload\\s(?!all\\b)\\S+$", permissionType = PermissionType.CONSOLE_OR_PERMISSION,
            permission = "magic.command.magicmarket.all||magic.command.magicmarket.reload", label = "^magicMarket$")
    public void reloadBy(CommandSender sender, String[] args){
        BukkitMagicMarket.getInstance().getConfigManager().reloadConfig(args[1], e -> {
            if (!e) {
                BukkitMagicMarket.getInstance().sendMessage(sender, "PlayerMessage.Error.ReloadFile");
                return;
            }
            switch (args[1].toLowerCase(Locale.ROOT)) {
                case "config" -> BukkitMagicMarket.getInstance().setupCategoryManager();
                case "redis" ->  {
                    BukkitMagicMarket.getInstance().setupRedisStore();
                    BukkitMagicMarket.getInstance().setupRepository();
                }
                case "mongodb" -> {
                    BukkitMagicMarket.getInstance().setupMongoDB();
                    BukkitMagicMarket.getInstance().setupRepository();
                }
                case "blacklist" -> BukkitMagicMarket.getInstance().setupBlacklistManager();
            }
            BukkitMagicMarket.getInstance().sendMessage(sender, "PlayerMessage.Success.ReloadFile");
        });
    }
}
