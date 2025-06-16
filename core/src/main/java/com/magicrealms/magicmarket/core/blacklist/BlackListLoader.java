package com.magicrealms.magicmarket.core.blacklist;

import com.magicrealms.magiclib.bukkit.manage.ConfigManager;
import com.magicrealms.magiclib.common.utils.Pair;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.*;

/**
 * @author Ryan-0916
 * @Desc 黑名单加载器
 * @date 2025-06-12
 */
@Getter
@Slf4j
public class BlackListLoader {

    private final List<Pair<String, Integer>> blackList = new ArrayList<>();

    public BlackListLoader(BukkitMagicMarket plugin) {
        this.loadAllCategory(plugin.getConfigManager());
    }

    private void loadAllCategory(ConfigManager configManager) {
        configManager.getYmlListValue(YML_BLACKLIST, "Items")
                .ifPresent(items -> items.forEach(item -> {
            String[] args = item.split("::");
            if (args.length > 2) {
                return;
            }
            if (args.length == 1) {
                blackList.add(Pair.of(args[0], 0));
                return;
            }
            try {
                blackList.add(Pair.of(args[0], Integer.parseInt(args[1])));
            } catch (NumberFormatException e) {
                log.warn("CustomData argument must be an integer: {}"
                        , args[1]);
            }
        }));
    }

}
