package org.nextrg.skylens.client.Helpers;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import static org.nextrg.skylens.client.Helpers.Text.*;

public class Tooltips {
    public static List<Text> getLore(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines();
    }
    public static int getTooltipMiddle(List<Text> lines, NbtCompound nbt, int type) {
        int targetIndex = 15;
        List<String> st = new java.util.ArrayList<>(Collections.emptyList());
        final boolean[] hasUlt = {false};
        nbt.getCompound("enchantments").getKeys().forEach(en -> {
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
                    int test = 2;
                    if (size < 5) test = 3;
                    if (size == 4) test = 2;
                    if (size == 5) test = 4;
                    targetIndex = i + test;
                    break;
                }
            }
        }
        if (type == 1) {
            lines.add(targetIndex, Text.literal(""));
        }
        return Math.min(targetIndex, lines.size());
    }
    public static String getItemType(List<Text> lines) {
        var category = "OTHER";
        for (var line : lines) {
            if (Stream.of("COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE", "SPECIAL", "VERY SPECIAL", "ULTIMATE", "ADMIN").anyMatch(ucs(line.toString())::contains)) {
                for (var string : line.getSiblings()) {
                    if (Stream.of("COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE", "SPECIAL", "VERY SPECIAL", "ULTIMATE", "ADMIN").anyMatch(ucs(string.toString())::contains)) {
                        var tooltipCategory = getLiteral(lcs(string.getContent().toString())).split(" ");
                        String categoryKey = tooltipCategory[tooltipCategory.length - 1];
                        category = ucs(Stream.of("sword", "bow", "hoe", "shears", "shovel", "axe", "helmet", "chestplate", "leggings", "boots", "pickaxe", "drill", "fishing rod", "fishing weapon", "wand", "necklace", "cloak", "belt", "gloves", "gauntlet")
                                .filter(categoryKey::equals)
                                .findFirst()
                                .orElse(category));
                    }
                }
            }
        }
        return category;
    }
}
