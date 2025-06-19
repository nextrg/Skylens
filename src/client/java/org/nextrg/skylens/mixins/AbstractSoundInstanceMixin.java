package org.nextrg.skylens.mixins;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import org.nextrg.skylens.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static org.nextrg.skylens.client.main.EnhancedNoteblockSounds.instrumentList;
import static org.nextrg.skylens.client.main.EnhancedNoteblockSounds.isSkyblockMusic;
import static org.nextrg.skylens.client.utils.Other.onSkyblock;

@Mixin(AbstractSoundInstance.class)
public class AbstractSoundInstanceMixin {
    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void modifyVolume(CallbackInfoReturnable<Float> cir) {
        try {
            SoundInstance sound = (SoundInstance) this;
            if (ModConfig.enhancedNoteblockSounds && onSkyblock() && isSkyblockMusic(sound)) {
                var path = sound.getId().getPath();
                for (String instrument : instrumentList) {
                    if (path.contains("note_block." + instrument)) {
                        cir.setReturnValue(shouldReplace(instrument) ? cir.getReturnValue() * 0.01f : cir.getReturnValue());
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
    
    @Unique
    private static boolean shouldReplace(String instrument) {
        Map<String, Float> volumes = Map.of(
                "bass", ModConfig.noteblockBassVolume,
                "basedrum", ModConfig.noteblockBasedrumVolume,
                "hat", ModConfig.noteblockHatVolume,
                "snare", ModConfig.noteblockSnareVolume
        );
        boolean replace = true;
        Float volume = volumes.get(instrument);
        if (volume == null || volume <= 0f) {
            replace = false;
        }
        return replace;
    }
}