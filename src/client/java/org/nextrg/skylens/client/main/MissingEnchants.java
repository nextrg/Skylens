package org.nextrg.skylens.client.main;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.nextrg.skylens.client.utils.Files.readJSONFromNeu;
import static org.nextrg.skylens.client.utils.Other.*;
import static org.nextrg.skylens.client.utils.Text.*;
import static org.nextrg.skylens.client.utils.Tooltips.getItemType;
import static org.nextrg.skylens.client.utils.Tooltips.getTooltipMiddle;

public class MissingEnchants {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Map<String, List<String>> enchantCache = new HashMap<>();
    static JsonObject enchants = null;
    
    public static void init() {
        enchants = readJSONFromNeu("/refs/heads/master/constants/enchants.json");
        scheduler.scheduleAtFixedRate(enchantCache::clear, 1, 1, TimeUnit.MINUTES);
    }
    
    private static String fixOutdatedNames(String input) {
        return input.replaceAll("pristine", "prismatic");
    }
    
    public static void getMissingEnchantments(ItemStack stack, List<Text> lines) {
        if (enchants != null && ModConfig.missingEnchants && onSkyblock() && stack.getCustomName() != null) {
            String category = getItemType(lines);
            var custom_data = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
            if (category.equalsIgnoreCase("other") || custom_data == null) return;
            if (enchantCache.size() > 50) {
                enchantCache.clear();
            }
            String cacheKey = lines.hashCode() + "_" + custom_data.copyNbt();
            if (enchantCache.containsKey(cacheKey)) {
                List<String> cachedEnchantments = enchantCache.get(cacheKey);
                displayMissingEnchantments(lines, cachedEnchantments, custom_data.copyNbt());
                return;
            }
            List<String> itemEnchants = new java.util.ArrayList<>(Collections.emptyList());
            List<String> missingEnchants = new java.util.ArrayList<>(Collections.emptyList());
            List<String> ultimateEnchants = new java.util.ArrayList<>(Collections.emptyList());
            
            String displayName = getLiteral(stack.getCustomName().withoutStyle().getFirst().getContent().toString().toLowerCase());
            if (displayName.contains("Gemstone Gauntlet")) { category = "GAUNTLET"; }
            
            custom_data.copyNbt().getCompound("enchantments").getKeys().forEach(en -> itemEnchants.add(en.toLowerCase()));
            JsonObject neuEnchants = enchants.get("enchants").getAsJsonObject();
            JsonArray neuEnchantPools = enchants.get("enchant_pools").getAsJsonArray();
            if (neuEnchants.get(category) != null) {
                for (JsonElement encElement : neuEnchants.get(category).getAsJsonArray()) {
                    String enc = encElement.getAsString().toLowerCase();
                    boolean conflictFound = false;
                    for (JsonElement conflictGroup : neuEnchantPools) {
                        if (conflictGroup.toString().toLowerCase().contains(enc.toLowerCase())) {
                            var array = conflictGroup.getAsJsonArray();
                            for (var i = 0; i < array.size(); i++) {
                                if (itemEnchants.contains(array.get(i).getAsString().toLowerCase())) {
                                    conflictFound = true;
                                    break;
                                }
                            }
                        }
                        if (conflictFound) break;
                    }
                    if (!conflictFound && !itemEnchants.contains(enc.toLowerCase()) && !itemEnchants.contains("one_for_all")) {
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
                enchantCache.put(cacheKey, missingEnchants);
                displayMissingEnchantments(lines, missingEnchants, custom_data.copyNbt());
            }
        }
    }
    
    public static void displayMissingEnchantments(List<Text> lines, List<String> enchants, NbtCompound nbt) {
        var maxLinePosition = getTooltipMiddle(lines, nbt, 1);
        var symbol = Screen.hasShiftDown() ? "✦" : "✧";
        var color = rgbToHexa(Screen.hasShiftDown() ? ModConfig.missingEnchantsEnabled : ModConfig.missingEnchantsDisabled);
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
