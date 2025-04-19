package org.nextrg.skylens.client.Main;

import com.google.gson.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.nextrg.skylens.client.ModConfig;
import java.util.*;
import java.util.List;
import static org.nextrg.skylens.client.Helpers.Other.*;
import static org.nextrg.skylens.client.Helpers.Text.*;
import static org.nextrg.skylens.client.Helpers.Tooltips.getItemType;
import static org.nextrg.skylens.client.Helpers.Tooltips.getTooltipMiddle;

public class MissingEnchantments {
    static JsonObject enchants = null;
    public static void init() {
        getJson();
    }
    public static void getJson() {
        enchants = readJSONFromNeu("/refs/heads/master/constants/enchants.json");
    }
    private static String fixOutdatedNames(String input) {
        return input.replaceAll("pristine", "prismatic");
    }
    public static void getMissingEnchantments(ItemStack stack, List<Text> lines) {
        if (enchants != null && ModConfig.missingEnchants && onSkyblock() && stack.getCustomName() != null) {
            String category = getItemType(lines);
            var custom_data = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
            if (category.equalsIgnoreCase("other") || custom_data == null) return;
            List<String> itemEnchants = new java.util.ArrayList<>(Collections.emptyList());
            List<String> missingEnchants = new java.util.ArrayList<>(Collections.emptyList());
            List<String> ultimateEnchants = new java.util.ArrayList<>(Collections.emptyList());
            String displayName = getLiteral(lcs(stack.getCustomName().withoutStyle().getFirst().getContent().toString()));
            if (displayName.contains("Gemstone Gauntlet")) { category = "GAUNTLET"; }
            custom_data.copyNbt().getCompound("enchantments").getKeys().forEach(en -> itemEnchants.add(lcs(en)));
            JsonObject neuEnchants = enchants.get("enchants").getAsJsonObject();
            JsonArray neuEnchantPools = enchants.get("enchant_pools").getAsJsonArray();
            if (neuEnchants.get(category) != null) {
                for (JsonElement encElement : neuEnchants.get(category).getAsJsonArray()) {
                    String enc = lcs(encElement.getAsString());
                    boolean conflictFound = false;
                    for (JsonElement conflictGroup : neuEnchantPools) {
                        if (conflictGroup.toString().contains(enc)) {
                            var array = conflictGroup.getAsJsonArray();
                            for (var i = 0; i < array.size(); i++) {
                                if (itemEnchants.contains(array.get(i).getAsString())) {
                                    conflictFound = true;
                                    break;
                                }
                            }
                        }
                        if (conflictFound) break;
                    }
                    if (!conflictFound && !itemEnchants.contains(enc) && !itemEnchants.contains("one_for_all")) {
                        var result = capitalize(fixOutdatedNames(enc.replaceAll("_", " ")));
                        if (!enc.contains("ultimate")) {
                            missingEnchants.add(result);
                        } else {
                            ultimateEnchants.add(result);
                        }
                    }
                }
            }
            if (!missingEnchants.isEmpty() && !itemEnchants.isEmpty()) {
                if (!ultimateEnchants.isEmpty()) {
                    missingEnchants.add(getFormat("bold") + "Any Ultimate");
                }
                displayMissingEnchantments(lines, missingEnchants, custom_data.copyNbt());
            }
        }
    }
    
    public static void displayMissingEnchantments(List<Text> lines, List<String> enchants, NbtCompound nbt) {
        var maxLinePosition = getTooltipMiddle(lines, nbt, 1);
        var symbol = Screen.hasShiftDown() ? "✦" : "✧";
        var color = rgbToHexa(Screen.hasShiftDown() ? ModConfig.me_enabled : ModConfig.me_disabled);
        if (Screen.hasShiftDown()) {
            List<Text> reversedLines = new ArrayList<>();
            for (int i = enchants.size() - 1; i >= 0; i -= 3) {
                StringBuilder sb = new StringBuilder();
                for (int j = i; j >= Math.max(0, i - 2); j--) {
                    sb.append(enchants.get(j));
                    if (!(j == 0)) {
                        sb.append(getColorCode("gray")).append(getFormat("reset")).append(", ");
                    }
                }
                reversedLines.add(Text.literal("⋗ " + sb.toString().trim()).formatted(Formatting.GRAY));
            }
            lines.addAll(maxLinePosition, reversedLines);
        } else {
            lines.add(maxLinePosition, Text.literal("⋗ Press [SHIFT] to see").formatted(Formatting.GRAY));
        }
        lines.add(maxLinePosition, Text.literal(symbol + " Missing enchantments:").withColor(color));
    }
}
