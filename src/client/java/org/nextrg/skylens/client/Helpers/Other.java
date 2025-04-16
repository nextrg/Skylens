package org.nextrg.skylens.client.Helpers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
    public static List<String> getScoreboardData() {
        List<String> scoreboardData = new ArrayList<>();
        ClientPlayerEntity playerEntity = MinecraftClient.getInstance().player;
        if (!(playerEntity == null)) {
            var scoreboard = playerEntity.getScoreboard();
            var title = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1));
            if (title != null) {
                scoreboardData.addFirst(title.getDisplayName().copy().getString());
            }
            for (ScoreHolder lines : scoreboard.getKnownScoreHolders()) {
                if (scoreboard.getScoreHolderObjectives(lines).containsKey(title)) {
                    Team team = scoreboard.getScoreHolderTeam(lines.getNameForScoreboard());
                    if (team != null) {
                        String strLine = team.getPrefix().getString() + team.getSuffix().getString();
                        if (!strLine.trim().isEmpty()) {
                            scoreboardData.add(Formatting.strip(strLine));
                        }
                    }
                }
            }
        }
        return scoreboardData;
    }
    public static boolean onSkyblock() {
        if (ModConfig.onlySkyblock) {
            return MinecraftClient.getInstance().world != null || !MinecraftClient.getInstance().isInSingleplayer();
        } else {
            return true;
        }
    }
}
