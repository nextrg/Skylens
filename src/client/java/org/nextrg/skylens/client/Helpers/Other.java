package org.nextrg.skylens.client.Helpers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.Pair;
import org.nextrg.skylens.client.ModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Other {
    public static Pair<List<Text>, List<String>> getTabData(boolean getStyle) {
        List<Text> text = new ArrayList<>();
        List<String> string = new ArrayList<>();
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null) {
            if (getStyle) {
                text = networkHandler.getPlayerList()
                        .stream()
                        .map(PlayerListEntry::getDisplayName)
                        .filter(Objects::nonNull)
                        .toList();
            }
            string = networkHandler.getPlayerList()
                    .stream()
                    .map(PlayerListEntry::getDisplayName)
                    .filter(Objects::nonNull)
                    .map(Text::getString)
                    .map(String::strip)
                    .toList();
        }
        return Pair.of(text, string);
    }
    public static boolean onSkyblock() {
        if (ModConfig.onlySkyblock) {
            return MinecraftClient.getInstance().world != null || !MinecraftClient.getInstance().isInSingleplayer();
        } else {
            return true;
        }
    }
}
