package com.magicrealms.magicmarket.core.menu;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.StringUtil;
import com.magicrealms.magicmarket.api.product.Product;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import com.magicrealms.magicmarket.core.menu.enums.MarketSort;
import com.magicrealms.magicmarket.core.menu.listener.MarketMenuObserver;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ryan-0916
 * @Desc 全球市场菜单
 * @date 2025-06-10
 */
public abstract class AbstractMarketMenu extends AbstractCategoryMenu implements MarketMenuObserver {

    protected List<Product> data;

    protected MarketSort sort = MarketSort.NEWEST;

    protected final int PAGE_COUNT;

    public AbstractMarketMenu(BukkitMagicMarket plugin, Player player, List<Product> data, String configPath, String defLayout, Runnable backMenuRunnable) {
        super(plugin, player, data, configPath, defLayout, backMenuRunnable);
        this.data = CATEGORY_CACHE.get(CATEGORY_NAMES.get(cActive));
        sortData();
        this.PAGE_COUNT = StringUtils.countMatches(getLayout(), "F");
        setMaxPage(PAGE_COUNT <= 0 || data.isEmpty() ? 1 :
                data.size() % PAGE_COUNT == 0 ?
                        data.size() / PAGE_COUNT : data.size() / PAGE_COUNT + 1);
        asyncOpenMenu();
    }

    @Override
    public void changeSort() {
        sortData();
        /* 释放缓存 */
        cleanItemCache();
        /* 回退至第一页 */
        goToFirstPage();
        /* 处理菜单 */
        handleMenu(getLayout());
        /* 异步修改菜单标题 */
        asyncUpdateTitle();
    }

    @Override
    public void changeCategory() {
        this.data = CATEGORY_CACHE.get(CATEGORY_NAMES.get(cActive));
        changeSort();
    }

    public void sortData() {
        switch (sort) {
            case NEWEST -> data.sort(Comparator.comparingLong(Product::getShelfTime).reversed());
            case OLDEST -> data.sort(Comparator.comparingLong(Product::getShelfTime));
            case EXPENSIVE -> data.sort(Comparator.comparingDouble(Product::getPrice).reversed());
            case CHEAPEST -> data.sort(Comparator.comparingDouble(Product::getPrice));
        }
    }

    @Override
    protected LinkedHashMap<String, String> processHandTitle(LinkedHashMap<String, String> title) {
        Map<String, String> map = createPlaceholders();
        return title
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry)
                        -> StringUtil.replacePlaceholders(entry.getValue(), map), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    }

    public Map<String, String> createPlaceholders() {
        Map<String, String> map = super.createPlaceholders();
        map.put("sort", getConfigValue(String.format("CustomPapi.Sort.%s", sort.getPath()), "", ParseType.STRING));
        return map;
    }

    protected void clickCategory(int slot) {
        int index = cPage - 1 + StringUtils.countMatches(getLayout().substring(0, slot), "D");
        if (index < CATEGORY_NAMES.size() && index != cActive) {
            cActive = index;
            changeCategory();
        }
    }

    protected void changeCPage(int delta, char c) {
        changeCPage(delta, b -> {
            asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
            if (!b) return;
            super.handleMenuUnCache(getLayout());
            asyncUpdateTitle();
        });
    }

    protected void changePage(int delta, char c) {
        changePage(delta, b -> {
            asyncPlaySound(b ? "Icons." + c + ".ActiveDisplay.Sound" : "Icons." + c + ".DisabledDisplay.Sound");
            if (!b) return;
            handleMenu(getLayout());
            asyncUpdateTitle();
        });
    }

    protected void clickSort() {
        this.sort = sort.next();
        changeSort();
    }

}
