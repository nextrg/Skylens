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
import java.util.Map;

import static org.nextrg.skylens.client.utils.Other.onSkyblock;

public class EnhancedNoteblockSounds {
    public static List<String> instrumentList = new ArrayList<>();
    
    public static final SoundEvent harp = registerSound("harp");
    public static final SoundEvent bass = registerSound("bass");
    public static final SoundEvent snare = registerSound("snare");
    public static final SoundEvent basedrum = registerSound("basedrum");
    public static final SoundEvent hat = registerSound("hat");
    
    public static Map<String, SoundEvent> instruments = Map.of(
            "harp", harp,
            "bass", bass,
            "basedrum", basedrum,
            "hat", hat,
            "snare", snare
    );
    
    public static boolean initialized = false;
    
    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of("skylens", id);
        if (instrumentList == null) {
            instrumentList = new ArrayList<>();
        }
        instrumentList.add(id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }
    
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (MinecraftClient.getInstance() != null) {
                var clientInstance = MinecraftClient.getInstance();
                if (clientInstance.getSoundManager() != null && !initialized) {
                    clientInstance.getSoundManager().registerListener(EnhancedNoteblockSounds::onSound);
                    initialized = true;
                }
            }
        });
    }
    
    public static void playSound(SoundEvent sound, SoundInstance instance, float loudness) {
        var client = MinecraftClient.getInstance();
        client.getSoundManager().play(
                PositionedSoundInstance.master(
                        sound,
                        instance.getPitch(),
                        instance.getVolume() * 100 * loudness *
                                client.options.getSoundVolume(SoundCategory.RECORDS)
                )
        );
    }
    
    public static boolean isSkyblockMusic(SoundInstance sound) {
        var player = MinecraftClient.getInstance().player;
        if (player == null || sound == null) return false;
        
        return Math.abs(sound.getX() - player.getX()) <= 5 &&
                Math.abs(sound.getY() - player.getY()) <= 5 &&
                Math.abs(sound.getZ() - player.getZ()) <= 5;
    }
    
    private static void onSound(SoundInstance sound, WeightedSoundSet weightedSoundSet, float v) {
        if (onSkyblock() && ModConfig.enhancedNoteblockSounds && isSkyblockMusic(sound)) {
            Identifier id = sound.getId();
            if (id != null) {
                var path = id.getPath();
                for (String instrument : instrumentList) {
                    if (path.contains("note_block." + instrument)) {
                        SoundEvent event = instruments.get(instrument);
                        float volume = getVolume(instrument);
                        playSound(event, sound, ModConfig.noteblockGeneralVolume * volume);
                        break;
                    }
                }
            }
        }
    }
    
    public static float getVolume(String instrument) {
        Map<String, Float> volumes = Map.of(
                "harp", ModConfig.noteblockHarpVolume * 0.5F,
                "bass", ModConfig.noteblockBassVolume * 0.4F,
                "basedrum", ModConfig.noteblockBasedrumVolume * 1.6F,
                "hat", ModConfig.noteblockHatVolume * 0.25F,
                "snare", ModConfig.noteblockSnareVolume * 10F
        );
        return volumes.get(instrument);
    }
}
