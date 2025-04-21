package org.nextrg.skylens.mixins;

import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import org.nextrg.skylens.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.nextrg.skylens.client.main.EnhancedNoteblockSounds.instrumentList;
import static org.nextrg.skylens.client.utils.Other.onSkyblock;

@Mixin(AbstractSoundInstance.class)
public class AbstractSoundInstanceMixin {
    @Inject(method = "getVolume", at = @At("RETURN"), cancellable = true)
    private void modifyVolume(CallbackInfoReturnable<Float> cir) {
        try {
            SoundInstance sound = (SoundInstance) (Object) this;
            var path = sound.getId().getPath();
            for (String instrument : instrumentList) {
                if (path.contains(getNoteBlock(instrument))) {
                    cir.setReturnValue(ModConfig.enhancedSkyblockMusic && onSkyblock() ? cir.getReturnValue() * 0.01f : cir.getReturnValue());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }
    
    @Unique
    private String getNoteBlock(String instrument) {
        return "note_block." + instrument;
    }
}