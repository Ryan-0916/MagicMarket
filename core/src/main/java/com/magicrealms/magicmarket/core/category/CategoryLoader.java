package com.magicrealms.magicmarket.core.category;

import com.magicrealms.magiclib.common.enums.ParseType;
import com.magicrealms.magiclib.common.utils.Pair;
import com.magicrealms.magicmarket.api.category.Category;
import com.magicrealms.magicmarket.core.BukkitMagicMarket;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_CATEGORY_DEF_MOBILE;
import static com.magicrealms.magicmarket.common.MagicMarketConstant.YML_CONFIG;

/**
 * @author Ryan-0916
 * @Desc 分类加载器
 * @date 2025-06-09
 */
@Log4j2
public class CategoryLoader {

    private final BukkitMagicMarket PLUGIN;

    @Getter
    private final List<Category> categories = new ArrayList<>();

    public CategoryLoader(BukkitMagicMarket plugin) {
        this.PLUGIN = plugin;
        this.loadAllCategory();
        categories.sort(Comparator.comparingInt(Category::getWeight).reversed());
    }

    /**
     * 加载配置文件内所有分类，并添加至 categories
     */
    private void loadAllCategory() {
        PLUGIN.getConfigManager().getYmlSubKeys(YML_CONFIG, "Category", false)
                .ifPresent(allCategory -> {
                    if (!allCategory.isEmpty()) {
                        allCategory.forEach(categoryName -> {
                            /* 文件路径 */
                            String key = "Category." + categoryName;
                            String path = PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, key + ".Path");
                            if (StringUtils.isBlank(path)) { return; }
                            Category category = Category.builder()
                                    .path(path)
                                    .name(categoryName)
                                    .weight(PLUGIN.getConfigManager().getYmlValue(YML_CONFIG, key + ".Weight", 0, ParseType
                                                    .INTEGER))
                                    .items(new ArrayList<>())
                                    .build();
                            /* 加载配置文件 */
                            PLUGIN.getConfigManager().loadMirrorConfig(path, YML_CATEGORY_DEF_MOBILE);
                            /* 加载物品 */
                            PLUGIN.getConfigManager().getYmlListValue(path, "Items").ifPresent(items -> items.forEach(item -> {
                                String[] args = item.split("::");
                                if (args.length > 2) { return; }
                                if (args.length == 1) { category.getItems().add(Pair.of(args[0], 0)); return; }
                                try { category.getItems().add(Pair.of(args[0], Integer.parseInt(args[1])));
                                } catch (NumberFormatException e) {
                                    log.warn("处理 category.yml 时出现异常，原因：CustomData参数必须是整数: {}", args[1]);
                                }
                            }));
                            categories.add(category);
                        });
                    }
        });
    }
}
