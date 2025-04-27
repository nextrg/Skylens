package org.nextrg.skylens.client.main;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.nextrg.skylens.client.ModConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nextrg.skylens.client.utils.Other.*;
import static org.nextrg.skylens.client.rendering.Renderer.*;
import static org.nextrg.skylens.client.utils.Text.getColorCode;
import static org.nextrg.skylens.client.utils.Text.hexToHexa;

public class SlayerBossIntros {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Object lock = new Object();
    private static final Pattern pattern = Pattern.compile("\\b\\w+\\s+\\w+\\s+(I|II|III|IV|V)\\b");
    private static float value = 0f;
    private static float flash = 0f;
    public static boolean cooldown = false;
    public static boolean updateBossName = false;
    public static boolean bossExists = false;
    public static boolean slayerQuestActive = false;
    private static long lastUpdate = System.currentTimeMillis();
    public static String boss;
    public static String tier;
    
    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, type) -> {
            var messageContent = message.getString();
            try {
                switch(messageContent.replaceFirst("^\\s+", "")) {
                    case "NICE! SLAYER BOSS SLAIN!", "SLAYER QUEST FAILED!", "SLAYER QUEST COMPLETE!",
                         "Your Slayer Quest has been cancelled!" -> {
                        bossExists = false;
                        slayerQuestActive = false;
                        value = 0f;
                    }
                }
            } catch (Exception ignored) {}
        });
        HudLayerRegistrationCallback.EVENT.register((wrap) -> wrap.attachLayerAfter(IdentifiedLayer.CHAT, Identifier.of("skylens", "slayer-intros"), SlayerBossIntros::initializeRendering));
    }
    
    public static void getBossName() {
        var scoreboardData = getScoreboardData();
        if (scoreboardData.contains("Slayer Quest")) {
            if (scoreboardData.contains("Slay the boss!")) return;
            if (scoreboardData.contains("Boss slain!")) {
                cooldown = false;
                bossExists = false;
                slayerQuestActive = false;
            } else {
                slayerQuestActive = true;
            }
            if (scoreboardData.size() >= 5) {
                if (System.currentTimeMillis() - lastUpdate < 1000) {
                    return;
                }
                lastUpdate = System.currentTimeMillis();
                for (var string : scoreboardData) {
                    Matcher matcher = pattern.matcher(string);
                    if (matcher.find()) {
                        boss = string;
                        tier = switch (boss.substring(Math.max(0, boss.length() - 2)).trim()) {
                            case "V" -> getColorCode("darkpurple");
                            case "IV" -> getColorCode("darkred");
                            case "III" -> getColorCode("red");
                            case "II" -> getColorCode("yellow");
                            default -> getColorCode("green");
                        };
                        updateBossName = false;
                    }
                }
            }
        }
    }
    
    public static void bossExists() {
        if (slayerQuestActive) {
            bossExists = true;
        }
    }
    
    private static void initializeRendering(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        prepare(drawContext);
    }
    
    public static void prepare(DrawContext d) {
        if (!onSkyblock()) return;
        if (!ModConfig.slayerIntros) return;
        getBossName();
        if (bossExists && !cooldown) {
            cooldown = true;
            textAnimation();
        }
        intro(d);
    }

    public static void textAnimation() {
        scheduler.schedule(() -> {
            for (var i = 0; i < 90; i++) {
                final float progress = (float) i / 89f;
                scheduler.schedule(() -> {
                    synchronized (lock) {
                        value = Math.min(easeInOutCubic(progress), 1);
                    }
                }, i * 12L, TimeUnit.MILLISECONDS);
            }
            scheduler.schedule(() -> {
                for (var i = 0; i < 120; i++) {
                    final float progress = (float) i / 119f;
                    scheduler.schedule(() -> {
                        synchronized (lock) {
                            value *= 1 + Math.min(progress, 1) / 10;
                        }
                    }, i * 2L, TimeUnit.MILLISECONDS);
                }
                scheduler.schedule(() -> {
                    value = 0f;
                    flash = 1f;
                    for (var i = 0; i < 50; i++) {
                        final float progress = (float) i / 49f;
                        scheduler.schedule(() -> {
                            synchronized (lock) {
                                flash = Math.max(0, 1 - easeInOutQuadratic(progress));
                            }
                        }, i * 16L, TimeUnit.MILLISECONDS);
                    }
                }, 240L, TimeUnit.MILLISECONDS);
            }, 1133L, TimeUnit.MILLISECONDS);
        }, 0, TimeUnit.SECONDS);
    }
    
    public static void intro(DrawContext drawContext) {
        int transparency = switch (ModConfig.slayerIntrosBackground) {
            case "Opaque" -> 255;
            case "Half_Opaque" -> 130;
            default -> 0;
        };
        if (value != 0f && boss != null) {
            var fontWeight = MinecraftClient.getInstance().textRenderer.fontHeight;
            var x = getScreenWidth(drawContext) / 2;
            var y = (float) getScreenHeight(drawContext) / 2 - fontWeight * 2 * value;
            drawContext.fill(0, 0, getScreenWidth(drawContext), getScreenHeight(drawContext), hexToHexa(0xFF000000, Math.clamp((int) (transparency * value), 0, transparency)));
            drawText(drawContext, tier + boss, x, (int) y, hexToHexa(0xFFFFFFFF, (int) value * 255), 4F * value, true, true);
        }
        drawContext.fill(0, 0, getScreenWidth(drawContext), getScreenHeight(drawContext), hexToHexa(0xFFffffff, (int) (transparency * flash)));
    }
}
