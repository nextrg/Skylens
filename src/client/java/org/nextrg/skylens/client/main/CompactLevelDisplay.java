package org.nextrg.skylens.client.main;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.nextrg.skylens.client.ModConfig;
import java.util.List;

import static org.nextrg.skylens.client.utils.Other.getPetRarity;
import static org.nextrg.skylens.client.utils.Other.onSkyblock;
import static org.nextrg.skylens.client.utils.Text.getColorCode;

public class CompactLevelDisplay {
    public static void shortenPetLevel(ItemStack stack, List<Text> lines) {
        if (ModConfig.compactLevel && onSkyblock()) {
            if (stack.getItemName().toString().contains("player_head") && stack.getCustomName() != null && !getPetRarity(stack).equals("X")) {
                var display = stack.getCustomName();
                var petRarity = getColorCode(getPetRarity(stack));
                int maxLevel = display.getString().contains("Golden Dragon") ? 200 : 100;
                lines.removeFirst();
                lines.addFirst(Text.literal(display.getString()
                        .replaceAll("\\[(\\d+)(✦)]", "§8[" + petRarity + "$1§4$2§8]")
                        .replace("[Lvl " + maxLevel, "§8[" + petRarity + maxLevel)
                        .replace("[Lvl ", "§8[§7")
                        .replace("]", "§8]§r" + petRarity)
                        .replaceAll(" ✦", getColorCode(display.getSiblings().getLast().getStyle().getColor().toString()) + " ✦")));
            }
        }
    }
}
