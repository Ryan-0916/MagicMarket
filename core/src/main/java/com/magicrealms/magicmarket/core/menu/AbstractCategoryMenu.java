package com.magicrealms.magicmarket.core.menu;

import com.magicrealms.magiclib.core.holder.PageMenuHolder;
import com.magicrealms.magiclib.core.utils.ItemUtil;
import com.magicrealms.magicmarket.api.category.Category;
import com.magicrealms.magicmarket.api.product.Product;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ryan-0916
 * @Desc 抽象分类菜单
 * @date 2025-06-10
 */
public abstract class AbstractCategoryMenu extends PageMenuHolder {

    /* 所有物品 */
    protected List<Product> allProducts;
    /* 当前选中标签 */
    protected int cActive;
    /* 当前页数 */
    protected int cPage;
    /* 最大页数 */
    protected int cMaxPage;
    /* 全部标签 */
    private final List<Category> ALL_CATEGORIES;
    /* 按照分类后的标签 */
    protected final Map<String, List<Product>> CATEGORY_CACHE = new HashMap<>();
    /* 全部标签名称 */
    protected List<String> CATEGORY_NAMES = new ArrayList<>();
    /* 每页显示的分类个数 */
    protected final int CATEGORY_PAGE_COUNT;

    public AbstractCategoryMenu(BukkitMagicMarket plugin,
                                Player player,
                                List<Product> data,
                                String configPath,
                                String defLayout,
                                Runnable backMenuRunnable) {
        super(plugin, player, configPath, defLayout, backMenuRunnable);
        /* 市场上的全部商品 */
        this.allProducts = data;
        /* 每页显示的类型个数 */
        this.CATEGORY_PAGE_COUNT = StringUtils.countMatches(super.getLayout(), "D");
        /* 获取全部分类标签 */
        this.ALL_CATEGORIES = plugin
                .getCategoryManager()
                .getCategories();
        setUpCategoryData();
    }

    /**
     * 初始化标签数据
     */
    protected void setUpCategoryData() {
        CATEGORY_NAMES.add("All");
        CATEGORY_CACHE.put("All", allProducts);
        ALL_CATEGORIES.forEach(category ->
        {
            CATEGORY_NAMES.add(category.getName());
            CATEGORY_CACHE.put(category.getName(), allProducts.stream()
                    .filter(e -> category.containsItem(e.getProduct()))
                    .collect(Collectors.toList()));
        });
        if (CATEGORY_CACHE.size() > 1) {
            Set<Product> productsSet = CATEGORY_CACHE
                    .entrySet().stream()
                    .filter(entry -> !"All".equals(entry.getKey()) && !"Others".equals(entry.getKey()))
                    .flatMap(entity -> entity.getValue()
                            .stream()).collect(Collectors.toSet());
            CATEGORY_NAMES.add("Others");
            CATEGORY_CACHE.put("Others", allProducts.stream().filter(e ->
                    !productsSet.contains(e)).collect(Collectors.toList()));
        }
        this.cPage = 1;
        this.cMaxPage = CATEGORY_PAGE_COUNT > 0 && CATEGORY_CACHE.size()
                > CATEGORY_PAGE_COUNT ? (CATEGORY_CACHE.size() - CATEGORY_PAGE_COUNT) + 1 : 1;
    }

    @Override
    protected void handleMenuUnCache(String layout) {
        int currentAppearIndex = cPage - 2;
        for (int i = 0; i < layout.length(); i++) {
            char c = layout.charAt(i);
            if (c != 'C' && c != 'D' && c != 'E') { continue; }
            switch (c) {
                case 'C' -> setButtonSlot(i, !(cPage > 1));
                case 'E' -> setButtonSlot(i, !(cPage < cMaxPage));
                case 'D' -> {
                    ItemStack item = (++currentAppearIndex < CATEGORY_NAMES.size())
                            ? ItemUtil.getItemStackByConfig(
                            getPlugin().getConfigManager(),
                            getConfigPath(),
                            "Category." + CATEGORY_NAMES.get(currentAppearIndex) + ".Display")
                            : ItemUtil.AIR;
                    super.setItemSlot(i, item);
                }
            }
        }
    }

    protected Map<String, String> createPlaceholders() {
        Map<String, String> map = new HashMap<>();
        /* 变量部分处理 */
        final String CATEGORY = "selected_category_%s"; // 选中分类
        IntStream.rangeClosed(1, CATEGORY_NAMES.size())
                .forEach(i -> {
                    int index = cPage - 2 + i;
                    map.put(String.format(CATEGORY, i)
                            , getCustomPapiText("SelectedCategory_" + i, index == cActive));
                });
        return map;
    }

    public void changeCPage(int delta, Consumer<Boolean> callBack) {
        callBack.accept(setCPage(cPage + delta));
    }

    public boolean setCPage(int page) {
        if (page >= 1 && page <= cMaxPage) {
            this.cPage = page;
            return true;
        }
        return false;
    }
}
