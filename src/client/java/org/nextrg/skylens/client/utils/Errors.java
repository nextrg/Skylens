package org.nextrg.skylens.client.utils;

import earth.terrarium.olympus.client.shader.builtin.RoundedRectShader;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.nextrg.skylens.client.ModConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Errors {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    static boolean showError = true;
    static String errorString = "";
    static int errorY = -160;
    public static void logErr(Exception e, String text) {
        if (showError) {
            errorString = text;
            System.err.println("[Skylens] " + text + ":\n" + e);
            showError = false;
            scheduler.schedule(() -> showError = true, 3, TimeUnit.SECONDS);
        }
    }
    
    public static void renderErrorMessage(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        try {
            if (ModConfig.showErrors) {
                if (!showError) {
                    if (errorY < 0) {
                        errorY += 10;
                    }
                } else if (errorY > -160) {
                    errorY -= 10;
                }
                if (errorY != -160) {
                    List<String> table = new ArrayList<>(List.of());
                    String text = errorString;
                    int sentences = text.length() - text.replaceAll(" ", "").length() + 1;
                    table.addAll(Arrays.asList(text.replace("error", "exception").split(" ")));
                    var x = Renderer.getScreenWidth(drawContext) - 149 - errorY;
                    var y = Renderer.getScreenHeight(drawContext) - 45;
                    RoundedRectShader.fill(drawContext, x, y, 144, 40, 0xd1271717, 0x00000000, 8, 0);
                    drawContext.fillGradient(x, y + 4, x + 144, y + 16, 0x00FF5555, 0x31FF5555);
                    Renderer.drawText(drawContext, "Error §7[Skylens]", x + 45, y + 5, 0xFFFF5555, 1F, true, true);
                    drawContext.drawHorizontalLine(x, x + 143, y + 16, 0x61FF5555);
                    for (int i = 0; i < sentences; i = i + 3) {
                        var string = table.get(i) + " " + table.get(i + 1) + ((i + 2 < sentences) ? " " + table.get(i + 2) : "");
                        Renderer.drawText(drawContext, string, x + 72, y + 20 + i * 3, 0xFFFFFFFF, 1F, true, true);
                    }
                }
            }
        } catch (Exception ignored) {}
    }
    
    public static void errorMessage() {
        HudLayerRegistrationCallback.EVENT.register((wrap) -> wrap.attachLayerAfter(IdentifiedLayer.CHAT, Identifier.of("skylens", "error"), Errors::renderErrorMessage));
    }
}
