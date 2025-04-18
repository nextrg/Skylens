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
    public static void getMissingEnchantments(ItemStack stack, List<Text> lines) {
        if (enchants != null && ModConfig.missingEnchants && onSkyblock() && stack.getCustomName() != null) {
            var category = getItemType(lines);
            var custom_data = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
            if (category.equalsIgnoreCase("other") || custom_data == null) return;
            List<String> ienc = new java.util.ArrayList<>(Collections.emptyList());
            List<String> menc = new java.util.ArrayList<>(Collections.emptyList());
            List<String> ult = new java.util.ArrayList<>(Collections.emptyList());
            String displayName = getLiteral(lcs(stack.getCustomName().withoutStyle().getFirst().getContent().toString()));
            if (displayName.contains("Gemstone Gauntlet")) { category = "GAUNTLET"; }
            custom_data.copyNbt().getCompound("enchantments").getKeys().forEach(en -> ienc.add(lcs(en)));
            var g = enchants.get("enchants").getAsJsonObject();
            var d = enchants.get("enchant_pools").getAsJsonArray();
            if (g.get(category) != null) {
                for (JsonElement encElement : g.get(category).getAsJsonArray()) {
                    String enc = lcs(encElement.getAsString());
                    boolean conflictFound = false;
                    for (JsonElement conflictgroup : d) {
                        if (conflictgroup.toString().contains(enc)) {
                            var array = conflictgroup.getAsJsonArray();
                            for (var i = 0; i < array.size(); i++) {
                                if (ienc.contains(array.get(i).getAsString())) {
                                    conflictFound = true;
                                    break;
                                }
                            }
                        }
                        if (conflictFound) break;
                    }
                    if (!conflictFound && !ienc.contains(enc) && !ienc.contains("one_for_all")) {
                        var result = capitalize(enc.replaceAll("_", " "));
                        if (!enc.contains("ultimate")) {
                            menc.add(result);
                        } else {
                            ult.add(result);
                        }
                    }
                }
            }
            if (!menc.isEmpty() && !ienc.isEmpty()) {
                if (!ult.isEmpty()) {
                    menc.add(getFormat("bold") + "Any Ultimate");
                }
                displayMissingEnchantments(lines, menc, custom_data.copyNbt());
            }
        }
    }
    
    public static void displayMissingEnchantments(List<Text> lines, List<String> encs, NbtCompound nbts) {
        var maxIndex = getTooltipMiddle(lines, nbts, 1);
        var symbol = Screen.hasShiftDown() ? "✦" : "✧";
        var color = rgbToHexa(Screen.hasShiftDown() ? ModConfig.me_enabled : ModConfig.me_disabled);
        if (Screen.hasShiftDown()) {
            List<Text> reversedLines = new ArrayList<>();
            for (int i = encs.size() - 1; i >= 0; i -= 3) {
                StringBuilder sb = new StringBuilder();
                for (int j = i; j >= Math.max(0, i - 2); j--) {
                    sb.append(encs.get(j));
                    if (!(j == 0)) {
                        sb.append(getColorCode("gray")).append(getFormat("reset")).append(", ");
                    }
                }
                reversedLines.add(Text.literal("⋗ " + sb.toString().trim()).formatted(Formatting.GRAY));
            }
            lines.addAll(maxIndex, reversedLines);
        } else {
            lines.add(maxIndex, Text.literal("⋗ Press [SHIFT] to see").formatted(Formatting.GRAY));
        }
        lines.add(maxIndex, Text.literal(symbol + " Missing enchantments:").withColor(color));
    }
}
