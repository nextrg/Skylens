package org.nextrg.skylens.client.main;

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

import static org.nextrg.skylens.client.utils.Other.onSkyblock;

public class EnhancedNoteblockSounds {
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
                MinecraftClient.getInstance().getSoundManager().registerListener(EnhancedNoteblockSounds::onSound);
                initialized = true;
            }
        });
    }
    
    public static void playSound(SoundEvent sound, SoundInstance instance, float loudness) {
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(
                        sound,
                        instance.getPitch(),
                        instance.getVolume() * 100 * loudness *
                                MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS)
                )
        );
    }
    
    private static void onSound(SoundInstance soundInstance, WeightedSoundSet weightedSoundSet, float v) {
        if (onSkyblock() && ModConfig.enhancedSkyblockMusic) {
            Identifier id = soundInstance.getId();
            if (id != null) {
                var path = id.getPath();
                for (String instrument : instrumentList) {
                    if (path.contains("note_block." + instrument)) {
                        SoundEvent sound = bass;
                        float loudness = ModConfig.noteblockBassVolume;
                        switch (instrument) {
                            case "harp" -> {
                                sound = harp;
                                loudness = ModConfig.noteblockHarpVolume;
                            }
                            case "basedrum" -> {
                                sound = basedrum;
                                loudness = ModConfig.noteblockBasedrumVolume;
                            }
                            case "hat" -> {
                                sound = hat;
                                loudness = ModConfig.noteblockHatVolume;
                            }
                            case "snare" -> {
                                sound = snare;
                                loudness = 10F * ModConfig.noteblockBassVolume;
                            }
                        };
                        playSound(sound, soundInstance, ModConfig.noteblockGeneralVolume * loudness);
                        break;
                    }
                }
            }
        }
    }
}
