package com.magicrealms.magicmarket.core.listener;

import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeMutateEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.*;

/**
 * @author Ryan-0916
 * @Desc 权限组监听器
 * @date 2025-06-21
 */
public class LuckPermsListener implements Listener {

    private final BukkitMagicMarket PLUGIN;

    public LuckPermsListener(BukkitMagicMarket plugin, LuckPerms luckPerms) {
        this.PLUGIN = plugin;
        EventBus eventBus = luckPerms.getEventBus();
        eventBus.subscribe(plugin, NodeMutateEvent.class, this::onNodeMutateEvent);
    }

    /**
     * 权限节点变更事件
     * 当权限节点变更时，考虑到可能受影响的条数包含摊位信息。因此需要移除摊位信息的缓存
     * TODO: 需要具体判断权限节点内容。以免不必要的性能耗损
     * @param event 权限节点变更事件
     */
    private void onNodeMutateEvent(NodeMutateEvent event) {
        Bukkit.getScheduler().runTask(PLUGIN, () -> PLUGIN.getRedisStore().removeKey(MAGIC_MARKET_PLAYER_MARKET_DATA));
    }

}