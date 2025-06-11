package org.nextrg.skylens.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.nextrg.skylens.client.ModConfig;
import org.nextrg.skylens.client.main.CustomPetScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.nextrg.skylens.client.utils.Other.onSkyblock;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!onSkyblock()) return;
        var client = MinecraftClient.getInstance();
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        String title = screen.getTitle().getString();
        
        boolean petScreenTitle = title.equals("Pets") || title.contains("Pets (") || title.contains("Pets:") || title.contains("Choose Pet") || title.contains("Exp Sharing");
        if (petScreenTitle && ModConfig.customPetMenu && client.player != null) {
            ci.cancel();
            client.setScreen(new CustomPetScreen(screen.getScreenHandler(), client.player.getInventory(), title));
            CustomPetScreen.refresh = true;
        }
    }
}