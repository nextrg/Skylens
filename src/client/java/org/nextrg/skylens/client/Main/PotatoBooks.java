package org.nextrg.skylens.client.Main;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.nextrg.skylens.client.ModConfig;
import java.util.List;
import java.util.stream.Stream;
import static org.nextrg.skylens.client.Helpers.Errors.logErr;
import static org.nextrg.skylens.client.Helpers.Other.*;
import static org.nextrg.skylens.client.Helpers.Text.rgbToHexa;
import static org.nextrg.skylens.client.Helpers.Tooltips.getItemType;
import static org.nextrg.skylens.client.Helpers.Tooltips.getTooltipMiddle;

public class PotatoBooks {
    public static void showMissingPotatoBooks(ItemStack stack, List<Text> lines) {
        var custom_data = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (ModConfig.missingPotatoBooks && onSkyblock() && custom_data != null) {
            try {
                var missingPotato = 0;
                boolean isLegibleForPotatoBooks = Stream.of("sword", "bow", "helmet", "chestplate", "leggings", "boots", "fishing rod", "fishing weapon")
                        .anyMatch(type -> type.equalsIgnoreCase(getItemType(lines)));
                if (!getItemType(lines).equalsIgnoreCase("other") && isLegibleForPotatoBooks) {
                    var hotPotatoCount = custom_data.copyNbt().getInt("hot_potato_count");
                    missingPotato = 15 - hotPotatoCount;
                }
                if (missingPotato != 0) {
                    var st = ModConfig.missingPotatoBooksStyle;
                    int style = Integer.parseInt(st.substring(st.length() - 1));
                    var symbol = "≈";
                    var color = rgbToHexa(ModConfig.missingPotatoBooksColor);
                    var a = style >= 2 ? "⊰" : "";
                    var b = style >= 2 ? "⊱" : "";
                    var missingPotatoText = "";
                    if (missingPotato <= 5) {
                        missingPotatoText = (char) ('\u2786' + missingPotato + 3) + "";
                    } else {
                        missingPotatoText = (char) ('\u2793' - 5) + (style == 1 || style == 2 ? "§7+" : "") + "§e" + (char) ('\u2793' + (missingPotato - 15));
                    }
                    var maxIndex = getTooltipMiddle(lines, custom_data.copyNbt(), 2);
                    lines.add(maxIndex, Text.literal(symbol + " Missing potato book" + (missingPotato == 1 ? "" : "s") + ": §5" + a + missingPotatoText + b).withColor(color));
                }
            } catch (Exception e) {
                logErr(e, "Caught an error in potato books helper");
            }
        }
    }
}
