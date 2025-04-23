package org.nextrg.skylens.client.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.StringNbtReader;
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
import java.util.UUID;

import static org.nextrg.skylens.client.utils.Errors.logErr;
import static org.nextrg.skylens.client.utils.Files.readJSONFromNeu;
import static org.nextrg.skylens.client.utils.Text.getRarity;

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
    public static String getPetNameFromCustomName(Text customName) {
        var string = customName.getString();
        return string.substring(string.indexOf("]") + 2).replace(" ✦", "");
    }
    public static String getPetRarity(ItemStack stack) {
        var fallback = "common";
        var nbt = stack.getComponents().get(DataComponentTypes.CUSTOM_DATA);
        if (nbt != null) {
            var petInfo = nbt.copyNbt().get("petInfo");
            if (petInfo != null && petInfo.getType() == NbtElement.STRING_TYPE) {
                JsonObject workingPetInfo = new Gson().fromJson(petInfo.asString(), JsonObject.class);
                fallback = workingPetInfo.get("tier").getAsString().toLowerCase();
            } else {
                fallback = "X"; // <- Not a pet
            }
        } else {
            var name = stack.getCustomName();
            if (name != null && name.getSiblings() != null) {
                var siblings = name.getSiblings();
                if (siblings.size() > 1) {
                    fallback = getRarity(siblings.get(siblings.size() - 1).getStyle().getColor().toString());
                }
            }
        }
        return fallback;
    }
    public static ItemStack getPetTextureFromNEU(String petName) {
        ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);
        try {
            var petJson = readJSONFromNeu("/f75fb6876c1cc0179b47546e273389a21f8968a7/items/" +
                    petName.toUpperCase().replace(" ", "_") + "%3B4.json");
            var string = petJson.get("nbttag").getAsString()
                    .replaceAll("\\[\\d+:\\{", "[{")
                    .replaceAll("\\[\\d+:\"", "[\"")
                    .replaceAll(",\\d+:\"", ",\"")
                    .replaceAll("\\\\\"", "\"")
                    .replaceAll("([{,])([A-Za-z_][A-Za-z0-9_]*)\\:", "$1\"$2\":");
            
            String profileInfoLegacy = string.substring(string.indexOf("SkullOwner") - 1, string.indexOf("display") - 2);
            NbtCompound profileInfoLegacyNBT;
            profileInfoLegacyNBT = StringNbtReader.parse("{" + profileInfoLegacy + "}").getCompound("SkullOwner");
            var properties = profileInfoLegacyNBT.getCompound("Properties").asString();
            var texture = properties.substring(properties.indexOf("Value:") + 7, properties.lastIndexOf("\"}"));
            
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "CustomHead");
            gameProfile.getProperties().put("textures", new Property("textures", texture));
            itemStack.set(DataComponentTypes.PROFILE, new ProfileComponent(gameProfile));
        } catch (Exception e) {
            logErr(e, "Caught an error setting pet texture (fallback)");
            // In case this fails, set pet icon to bone
            itemStack = new ItemStack(Items.BONE);
        }
        return itemStack;
    }
    public static boolean onSkyblock() {
        if (ModConfig.onlySkyblock) {
            return MinecraftClient.getInstance().world != null || !MinecraftClient.getInstance().isInSingleplayer();
        } else {
            return true;
        }
    }
}
