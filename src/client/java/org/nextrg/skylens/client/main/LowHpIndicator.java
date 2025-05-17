package org.nextrg.skylens.client.main;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.nextrg.skylens.client.rendering.RoundGradShader;

import static org.nextrg.skylens.client.rendering.Renderer.getScreenHeight;
import static org.nextrg.skylens.client.rendering.Renderer.getScreenWidth;
import static org.nextrg.skylens.client.utils.Other.onSkyblock;
import static org.nextrg.skylens.client.utils.Text.hexToHexa;

public class LowHpIndicator {
    public static void init() {
        HudLayerRegistrationCallback.EVENT.register((wrap) ->
                wrap.attachLayerBefore(IdentifiedLayer.HOTBAR_AND_BARS, Identifier.of("skylens", "lowhp-indicator"), LowHpIndicator::render));
    }
    
    private static void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        render(drawContext);
    }
    
    public static void render(DrawContext drawContext) {
        var player = MinecraftClient.getInstance().player;
        if (player != null && onSkyblock()) {
            float health = player.getHealth() / player.getMaxHealth();
            var transparency = Math.clamp(1.0f - health * 2, 0f, 1f);
            if (health <= 0.5f) {
                float time = 500f + 1000f * health;
                float animPhase = (Util.getMeasuringTimeMs() % (long) time) / time;
                animPhase = Math.clamp((float) Math.round(animPhase * 100) / 100, 0f, 1f);
                float baseTransparency = (0.5f - health) * 2.25f;
                float pulseEffect = 0.25f * (1f - animPhase);
                var effect = (210f * Math.max(0f, baseTransparency + pulseEffect)) * transparency;
                
                int colorMain = hexToHexa(0xFFAA0000, (int) effect);
                int colorFade = hexToHexa(0xFFAA0000, (int) (effect / 3f));
                
                RoundGradShader.fill(drawContext, 0, 0, getScreenWidth(drawContext), getScreenHeight(drawContext), 0f,
                        colorMain, colorFade, 0, 0f, 0, 0);
                
                RoundGradShader.fill(drawContext, 0, 0, getScreenWidth(drawContext), getScreenHeight(drawContext), 0f,
                        colorMain, colorFade, 1, 0f, 0, 0);
            }
        }
    }
}
