package com.magicrealms.magicmarket.core.blacklist;

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
@Slf4j
public class BlackListLoader {

    private final BukkitMagicMarket PLUGIN;
    @Getter
    private final List<Pair<String, Integer>> blackList = new ArrayList<>();

    public BlackListLoader(BukkitMagicMarket plugin) {
        this.PLUGIN = plugin;
        this.loadAllBlackList();
    }

    /**
     * 加载配置文件内所有物品，添加至 blackList
     */
    private void loadAllBlackList() {
        PLUGIN.getConfigManager().getYmlListValue(YML_BLACKLIST, "Items")
                .ifPresent(items -> items.forEach(item -> {
            String[] args = item.split("::");
            if (args.length > 2) { return; }
            if (args.length == 1) { blackList.add(Pair.of(args[0], 0)); return; }
            try {
                blackList.add(Pair.of(args[0], Integer.parseInt(args[1])));
            } catch (NumberFormatException e) {
                PLUGIN.getLoggerManager().warning("处理 blacklist.yml 时出现异常，原因：CustomData参数必须是整数: " + args[1]);
            }
        }));
    }

}
