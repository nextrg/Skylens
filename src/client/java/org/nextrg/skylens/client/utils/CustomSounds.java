package org.nextrg.skylens.client.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.nextrg.skylens.client.ModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.nextrg.skylens.client.utils.Other.onSkyblock;

public class CustomSounds {
    public static List<String> instrumentList = new ArrayList<>();
    public static final SoundEvent bass = registerSound("bass");
    public static final SoundEvent harp = registerSound("harp");
    public static final SoundEvent snare = registerSound("snare");
    public static final SoundEvent basedrum = registerSound("basedrum");
    public static final SoundEvent hat = registerSound("hat");
    
    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of("skylens", id);
        if (instrumentList == null) { // in case something pulls a dirty trick
            instrumentList = new ArrayList<>();
        }
        instrumentList.add(id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }
    
    static boolean initialized = false;
    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getSoundManager() != null && !initialized) {
                MinecraftClient.getInstance().getSoundManager().registerListener(CustomSounds::onSound);
                initialized = true;
            }
        });
    }
    
    private static void onSound(SoundInstance soundInstance, WeightedSoundSet weightedSoundSet, float v) {
        if (onSkyblock() && ModConfig.enhancedSkyblockMusic) {
            Identifier id = soundInstance.getId();
            if (id != null) {
                var path = id.getPath();
                for (String instrument : instrumentList) {
                    if (path.contains("note_block." + instrument)) {
                        float loudness;
                        SoundEvent sound = switch (instrument) {
                            case "harp" -> harp;
                            case "basedrum" -> basedrum;
                            case "hat" -> hat;
                            case "snare" -> snare;
                            default -> bass;
                        };
                        if (instrument.equals("snare")) {
                            loudness = 10f; // The sound itself is quiet
                        } else {
                            loudness = 1f;
                        }
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().getSoundManager().play(
                                    PositionedSoundInstance.master(
                                            sound,
                                            soundInstance.getPitch(),
                                            soundInstance.getVolume() * 100 * loudness *
                                                    MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS)
                                    )
                            );
                        });
                        break;
                    }
                }
            }
        }
    }
}
