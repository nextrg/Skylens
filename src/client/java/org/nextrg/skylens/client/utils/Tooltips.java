package org.nextrg.skylens.client.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static org.nextrg.skylens.client.utils.Text.*;

public class
Tooltips {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Pattern ENCHANT_PATTERN = Pattern.compile("\\b[IVXLCDM]+\\b");
    public static final Map<String, Integer> cache = new HashMap<>();
    public static List<Text> getLore(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines();
    }
    public static String getEnchantFromLine(Text line) {
        List<Text> siblings = line.getSiblings();
        int index = siblings.size() > 1 ? 1 : 0;
        return getLiteral(siblings.get(index).getContent().toString());
    }
    public static void tooltipMiddleCache() {
        scheduler.scheduleAtFixedRate(cache::clear, 1, 1, TimeUnit.MINUTES);
    }
    public static int getTooltipMiddle(List<Text> lines, NbtCompound nbt, int type) {
        if (cache.size() > 50) {
            cache.clear();
        }
        String cacheKey = lines.hashCode() + "_" + nbt.hashCode() + "_" + type;
        if (cache.containsKey(cacheKey)) {
            if (type == 1) {
                lines.add(cache.get(cacheKey), Text.literal(""));
            }
            return cache.get(cacheKey);
        }
        int targetIndex = 15;
        List<String> st = new java.util.ArrayList<>(Collections.emptyList());
        final boolean[] hasUlt = {false};
        nbt.getCompound("enchantments").map(NbtCompound::getKeys).orElse(Collections.emptySet()).forEach(en -> {
            if (!en.toLowerCase().contains("ultimate")) {
                st.add(capitalize(en.replace("_", " ")));
            } else {
                hasUlt[0] = true;
            }
        });
        if (!st.isEmpty()) {
            Collections.sort(st);
            var size = st.size() + (hasUlt[0] ? 1 : 0);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).toString().replace("-", " ").contains(st.getLast())) {
                    int offset = 2;
                    if (size <= 3) offset = 3;
                    if (size == 5) offset = 4;
                    targetIndex = i + offset;
                    break;
                }
            }
            if (!lines.get(targetIndex - 1).getSiblings().isEmpty()) {
                int foundEnchant = 0;
                // Find last enchant
                for (int i = 1; i < lines.size(); i++) {
                    var line = lines.get(i);
                    if (line.toString().contains("siblings")) {
                        var enchant = getEnchantFromLine(line);
                        if (ENCHANT_PATTERN.matcher(enchant).find()) {
                            foundEnchant = i - targetIndex;
                        }
                    }
                }
                // Check when last enchants description ends
                if (foundEnchant != 0) {
                    var lastEnchant = targetIndex + foundEnchant;
                    var lastEnchantDescription = lastEnchant;
                    for (var i = lastEnchant; i < lastEnchant + 5; i++) {
                        if (lines.get(i).getSiblings().isEmpty()) {
                            lastEnchantDescription = i;
                            break;
                        }
                    }
                    // And add an offset to it
                    targetIndex = lastEnchantDescription + 1;
                } else {
                    targetIndex += 1;
                }
            }
        }
        if (type == 1) {
            lines.add(targetIndex, Text.literal(""));
        }
        int result = Math.min(targetIndex, lines.size());
        cache.put(cacheKey, result);
        return result;
    }
    public static String getItemType(List<Text> lines) {
        var category = "OTHER";
        for (var line : lines) {
            if (Stream.of("COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE", "SPECIAL", "VERY SPECIAL", "ULTIMATE", "ADMIN").anyMatch(line.toString().toUpperCase()::contains)) {
                for (var string : line.getSiblings()) {
                    if (Stream.of("COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE", "SPECIAL", "VERY SPECIAL", "ULTIMATE", "ADMIN").anyMatch(string.toString().toUpperCase()::contains)) {
                        var tooltipCategory = getLiteral(string.getContent().toString().toLowerCase()).split(" ");
                        String categoryKey = tooltipCategory[tooltipCategory.length - 1];
                        category = Stream.of("sword", "bow", "hoe", "shears", "shovel", "axe", "helmet", "chestplate", "leggings", "boots", "pickaxe", "drill", "fishing rod", "fishing weapon", "wand", "necklace", "cloak", "belt", "gloves", "gauntlet")
                                .filter(categoryKey::equalsIgnoreCase)
                                .findFirst()
                                .orElse(category).toUpperCase();
                    }
                }
            }
        }
        return category;
    }
}
