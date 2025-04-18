package org.nextrg.skylens.client.Main;

import earth.terrarium.olympus.client.shader.builtin.RoundedRectShader;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.tuple.Pair;
import org.nextrg.skylens.client.ModConfig;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.mojang.blaze3d.systems.RenderSystem.*;
import static org.nextrg.skylens.client.Helpers.Errors.logErr;
import static org.nextrg.skylens.client.Helpers.Other.*;
import static org.nextrg.skylens.client.Helpers.Text.*;
import static org.nextrg.skylens.client.Helpers.Renderer.*;
import static org.nextrg.skylens.client.Helpers.Tooltips.getLore;

public class PetOverlay {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Object lock = new Object();
    private static final Pattern XP_PATTERN = Pattern.compile("\\((\\d+(\\.\\d*)?)%\\)");
    private static final Pattern LEVEL_PATTERN = Pattern.compile("(?<=\\[Lvl\\s)(\\d+)(?=])");
    static Map<String, int[]> themeColors = Map.of(
            "special", new int[]{0xFFaa2121, 0xFFff3232, 0xFF771515},
            "divine", new int[]{0xFF085599, 0xFF11aadd, 0xFF053666},
            "mythic", new int[]{0xFF772269, 0xFFff55ff, 0xFF511141},
            "legendary", new int[]{0xFFb37700, 0xFFfca800, 0xFF603500},
            "epic", new int[]{0xFF5701b7, 0xFFa932d9, 0xFF240154},
            "rare", new int[]{0xFF3232a3, 0xFF5252f3, 0xFF111135},
            "uncommon", new int[]{0xFF158a15, 0xFF54fd54, 0xFF143a14}
    );
    static List<ItemStack> petCache = new java.util.ArrayList<>(Collections.emptyList());
    static ItemStack currentPet = new ItemStack(Items.BONE);
    private static long lastUpdate = System.currentTimeMillis();
    private static boolean petMenu = false;
    public static boolean isHudEditorEnabled = false;
    public static boolean forceShow = false;
    static boolean currentLevelUp = false;
    static boolean leveledEver = false;
    static boolean show = false;
    static String theme = "";
    static float xp = 1f, level = 1f;
    static float appearProgress = 0f;
    static float levelAnimProgress = 0f;
    static float xpBeforeLevel = 0;
    static int maxLevel = 100;
    static int globalY = -45;
    
    // For HUD editor
    public static void forceAnim(boolean state) {
        forceShow = state;
    }
    public static void setHudEditor(boolean state) {
        isHudEditorEnabled = state;
        if (!show) {
            fade();
        }
    }
    
    // XP animation
    public static void animateXp(float updatedXp) {
        if (!ModConfig.petOverlayAnimXP) {
            xp = updatedXp;
        } else {
            float currentXp = xp;
            float difference = (updatedXp - currentXp) / 60f;
            for (int i = 0; i < 60; i++) {
                int finalI = i;
                scheduler.schedule(() -> {
                    synchronized (lock) {
                        xp = currentXp + difference * (finalI + 1);
                    }
                }, i * 4L, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    // Reads data about the pet from the tab list.
    public static void getPetData() {
        if (System.currentTimeMillis() - lastUpdate < 3000) {
            return;
        }
        lastUpdate = System.currentTimeMillis();
        Pair<List<Text>, List<String>> result = getTabData(false);
        for (var a : result.getRight()) {
            if (a.contains("%)") && a.contains("XP")) {
                Matcher matcher = XP_PATTERN.matcher(a);
                if (matcher.find()) {
                    animateXp(Float.parseFloat(matcher.group(1)) / 100);
                }
            }
            if (a.contains("[Lvl") && !a.contains(":") && !currentLevelUp) {
                Matcher matcher = LEVEL_PATTERN.matcher(a);
                if (matcher.find()) {
                    level = Float.parseFloat(matcher.group(1)) / maxLevel;
                }
            }
        }
    }
    
    // Reads cache to get pet's data.
    public static void readCache(Text chatMessage) {
        try {
            String petNameFromMessage = getLiteral(chatMessage.getSiblings().get(1).getContent().toString());
            var foundPet = false;
            for (ItemStack pet : petCache) {
                if (pet.getCustomName() != null) {
                    var petNameString = pet.getCustomName().getString();
                    var petName = petNameString.substring(petNameString.indexOf("]") + 2).replace(" ✦", "");
                    if (petName.equalsIgnoreCase(petNameFromMessage)) {
                        foundPet = true;
                        var lines = getLore(pet);
                        for (var line : lines) {
                            if (line.toString().contains("Progress to")) {
                                String levelProgress = getLiteral(line.getSiblings().getFirst().getContent().toString()).replace("Progress to Level ", "").replace(":", "");
                                String xpProgress = getLiteral(line.getSiblings().getLast().getContent().toString().replace("%", ""));
                                maxLevel = (lines.toString().contains("Golden Dragon")) ? 200 : 100;
                                level = (Float.parseFloat(levelProgress) - 1) / maxLevel;
                                xp = Float.parseFloat(xpProgress) / 100;
                            } else if (line.toString().contains("MAX LEVEL")) {
                                level = 1f;
                                xp = 1f;
                            }
                        }
                        currentPet = pet.copy();
                        theme = currentPet.getCustomName().getSiblings().get(pet.getCustomName().getSiblings().size() - (pet.getCustomName().toString().contains("✦") ? 2 : 1)).getStyle().getColor().toString();
                        lastUpdate = System.currentTimeMillis();
                    }
                }
            }
            if (!foundPet) {
                // Fallback to read the pet's texture from NEU repo
                currentPet = getPetTextureFromNEU(petNameFromMessage);
            }
        } catch (Exception e) {
            logErr(e, "Caught an error while reading pet cache");
        }
    }
    
    // Updating cache when opening pet's menu
    static int petMenuTicks = 0;
    public static void updateCache(Screen screen) {
        if (onSkyblock() && screen instanceof GenericContainerScreen genericContainerScreen) {
            if (genericContainerScreen.getTitle().getString().startsWith("Pets")) {
                petMenu = true;
                ScreenEvents.afterTick(screen).register(screen1 -> {
                    if (petMenu) {
                        List<ItemStack> newCache = new ArrayList<>();
                        petMenuTicks++;
                        for (Slot slot : genericContainerScreen.getScreenHandler().slots) {
                            if ((slot.id >= 10 && slot.id <= 16)
                                    || (slot.id >= 19 && slot.id <= 25)
                                    || (slot.id >= 28 && slot.id <= 34)
                                    || (slot.id >= 37 && slot.id <= 43)) {
                                ItemStack stack = slot.getStack();
                                if (!stack.isEmpty()) {
                                    newCache.add(stack);
                                }
                            }
                        }
                        if (!newCache.isEmpty()) {
                            petCache.clear();
                            petCache.addAll(newCache);
                            petMenu = false;
                        }
                    }
                });
            }
        }
    }
    
    public static void show(Text chatMessage) {
        var fade = ModConfig.petOverlayAnimFade;
        show = !fade || !show;
        fade();
        if (fade) {
            if (!show) {
                scheduler.schedule(() -> {
                    show = true;
                    fade();
                    readCache(chatMessage);
                }, 300L, TimeUnit.MILLISECONDS);
            } else {
                readCache(chatMessage);
            }
        } else {
            readCache(chatMessage);
        }
    }
    
    public static void hide() {
        show = false;
        fade();
    }
    
    public static void init() {
        ClientReceiveMessageEvents.GAME.register((message, type) -> {
            var messageContent = message.toString();
            try {
                if (messageContent.contains("You summoned your")) {
                    show(message);
                }
                if (messageContent.contains("You despawned your")) {
                    hide();
                }
                if (messageContent.contains("leveled up to level") && messageContent.contains(getLiteral(currentPet.getName().getSiblings().getLast().getContent().toString()))) {
                    var petName = currentPet.getCustomName();
                    var st = Integer.parseInt(message.withoutStyle().get(message.withoutStyle().size() - 2).getString());
                    maxLevel = petName != null && petName.toString().contains("Golden Dragon") ? 200 : 100;
                    level = Math.clamp((float)(st / maxLevel), level, 1f);
                    currentLevelUp = true;
                    leveledEver = true;
                    levelAnimProgress = 1f;
                    xpBeforeLevel = xp;
                    pulse();
                }
            } catch (Exception ignored) {}
        });
        ScreenEvents.BEFORE_INIT.register((client, screen, in1, in2) -> updateCache(screen));
        HudLayerRegistrationCallback.EVENT.register((wrap) -> {
            wrap.attachLayerAfter(IdentifiedLayer.CHAT, Identifier.of("skylens", "pet-overlay"), PetOverlay::uib);
        });
    }
    
    private static void uib(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        prepare(drawContext, true);
    }

    public static void prepare(DrawContext drawContext, boolean isHud) {
        if (ModConfig.petOverlay || isHudEditorEnabled && onSkyblock()) {
            if ((isHud && !isHudEditorEnabled) || (!isHud && isHudEditorEnabled)) {
                try {
                    long currentTime = System.currentTimeMillis();
                    if (!(currentTime - lastUpdate < 3000)) {
                        PetOverlay.getPetData();
                        lastUpdate = currentTime;
                    }
                    setShader(ShaderProgramKeys.POSITION_COLOR);
                    String ModConfigTheme = ModConfig.petOverlayTheme.toLowerCase();
                    int[] colors = themeColors.getOrDefault(
                            ModConfig.petOverlayPetRarity ? getRarity(theme) : ModConfigTheme,
                            new int[]{0xFF9A9A9A, 0xFFFFFFFF, 0xFF636363}
                    );
                    if (!ModConfig.petOverlayPetRarity && ModConfigTheme.contains("custom")) {
                        colors = new int[]{
                                rgbToHexa(ModConfig.petOverlayColor2),
                                rgbToHexa(ModConfig.petOverlayColor1),
                                rgbToHexa(ModConfig.petOverlayColor3)
                        };
                    }
                    progress(String.valueOf(ModConfig.petOverlayStyle).toLowerCase(), drawContext, colors[0], colors[1], colors[2]);
                } catch (Exception e) {
                    logErr(e, "Caught an error while processing pet overlay data");
                }
            }
        }
    }
    
    public static void pulse() {
        scheduler.schedule(() -> {
            for (var i = 0; i < 30; i++) {
                final float progress = (float) i / 29f;
                scheduler.schedule(() -> {
                    synchronized (lock) {
                        levelAnimProgress = Math.max(0, 1 - easeInOutQuadratic(progress));
                    }
                }, i * 10L, TimeUnit.MILLISECONDS);
            }
            scheduler.schedule(() -> {
                for (var i = 0; i < 30; i++) {
                    final float progress = (float) i / 29f;
                    scheduler.schedule(() -> {
                        synchronized (lock) {
                            levelAnimProgress = Math.min(easeInOutQuadratic(progress), 1);
                        }
                    }, i * 10L, TimeUnit.MILLISECONDS);
                }
                currentLevelUp = false;
            }, 3, TimeUnit.SECONDS);
            scheduler.schedule(() -> {
                leveledEver = false;
            }, 6, TimeUnit.SECONDS);
        }, 0, TimeUnit.SECONDS);
    }
    
    public static void fade() {
        if (show || forceShow) {
            for (var i = 0; i < 60; i++) {
                final float progress = (float) i / 59f;
                scheduler.schedule(() -> {
                    synchronized (lock) {
                        appearProgress = Math.min(easeInOutQuadratic(progress), 1);
                    }
                }, i * 5L, TimeUnit.MILLISECONDS);
            }
        } else {
            for (var i = 0; i < 60; i++) {
                final float progress = (float) i / 59f;
                scheduler.schedule(() -> {
                    synchronized (lock) {
                        appearProgress = Math.max(0, 1 - easeInOutQuadratic(progress));
                    }
                }, i * 5L, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    public static void progress(String type, DrawContext drawContext, int color1, int color2, int color3) {
        int marginY = ModConfig.petOverlayY;
        if (!ModConfig.petOverlayAnimFade) {
            globalY = show || isHudEditorEnabled ? 0 : -45 - marginY;
        } else {
            var h = 45 + marginY;
            globalY = -h + (int) (h * appearProgress);
        }
        if (globalY > -45 - marginY) {
            enableBlend();
            var matrix = drawContext.getMatrices().peek().getPositionMatrix();
            var screenWidth = getScreenWidth(drawContext);
            var screenHeight = getScreenHeight(drawContext);
            float amount = (float) (Util.getMeasuringTimeMs() / 1700.0) % 1;
            var textColor = hexaToHex(color2);
            
            // Levels
            boolean showLevel = ModConfig.petOverlayShowLvl;
            if (ModConfig.petOverlayHideLvlFull && level == 1f) {
                showLevel = false;
            }
            int padding = (showLevel || level == 1f) ? 0 : 3;
            String displayLvl = "Lvl " + Math.min(maxLevel, Math.round(level * maxLevel));
            String displayXP = "LV UP";
            var fadeProgressAnim = appearProgress;
            if (!currentLevelUp) {
                if (level == 1f) {
                    displayLvl = "LV MX";
                    displayXP = showLevel ? "100%" : "MAX";
                    xp = 1f;
                    fadeProgressAnim = 1f;
                } else {
                    displayXP = String.format("%." + ((xp >= 0.1) ? 1 : 2) + "f%%", xp * 100).replace(",", ".");
                }
            }
            
            // Animations
            int animtext = 0;
            if (leveledEver) {
                animtext = (int) (7 - levelAnimProgress * 7);
            }
            levelAnimProgress = level == 1f ? 0f : (!leveledEver ? 1f : levelAnimProgress);
            if (currentLevelUp) {
                xp = xpBeforeLevel * levelAnimProgress;
            } else if (level == 1f) {
                levelAnimProgress = showLevel ? 1f : 0f;
            }
            var leveltextColor = leveledEver ? hexToHexa(color2, Math.max(10, (int) (levelAnimProgress * 255))) : textColor;
            
            // Styles
            boolean isBar = Objects.equals(type, "style1");
            if (ModConfig.petOverlayInvert) {var temp = color2; color2 = color1; color1 = temp;}
            
            // Position
            int marginX = ModConfig.petOverlayX;
            String position = ModConfig.petOverlayPosition;
            boolean flipSide = "Inventory_Right".equals(position) || "Left".equals(position);
            int orientation = switch (position) {
                case "Left" -> -90;
                case "Right" -> screenWidth + 90;
                default -> screenWidth / 2;
            };
            int flip = flipSide ? 1 : -1;
            int x = orientation + marginX * flip + (isBar ? (flipSide ? 94 : -144) : flip * 108);
            int y = screenHeight - 11 - globalY - marginY - (isBar ? 0 : 5);
            // Rendering
            if (isBar) {
                int align = !ModConfig.petOverlayIconAlign ? 29 : 0;
                int textAlign = !ModConfig.petOverlayIconAlign ? 0 : 15;
                if (ModConfig.petOverlayAnimIdle) {
                    fillRoundRect(matrix, x + 2 - amount * 6, y + 2 - amount * 6, 46 + (amount * 12), 4 + (amount * 12), 12, hexToHexa(color2, (int) (255 - amount * 255)));
                }
                RoundedRectShader.fill(drawContext, x, y, 50, 8, color3, 0x00000000, 4.5f, 0);
                RoundedRectShader.fill(drawContext, x, y, Math.max(8, (int) (50 * level * fadeProgressAnim)), 8, color2, 0x00000000, 4.5f, 0);
                RoundedRectShader.fill(drawContext, x + 2, y + 2, Math.max(2, (int) (46 * xp * fadeProgressAnim)), 4, color1, 0x00000000, 2.5f, 0);
                drawItem(drawContext, currentPet, x + 2 + align, y - 17, 0.95F);
                if (showLevel) {
                    drawText(drawContext, displayLvl, x + 17 + textAlign, y - 16 + animtext, leveltextColor, 0.8F, true, true);
                }
                drawText(drawContext, displayXP, x + 17 + textAlign, y - 13 + (int) (3 * levelAnimProgress) - padding, textColor, 1F, true, true);
            } else {
                if (ModConfig.petOverlayAnimIdle) {
                    drawCircle(matrix, x, y + 1, 10.5F + 5F * amount, 0, 360, hexToHexa(color2, (int) (255 - amount * 255)), 0);
                }
                RoundedRectShader.fill(drawContext, x - 12, y - 11, 24, 24, color2, 0x00000000, 13, 0);
                drawCircle(matrix, x, y + 1, 12.5F, 0, (int) (360 - (level * fadeProgressAnim * 360)), color3, 0);
                int circleStyle = Objects.equals(type, "style3") ? 2 : 1;
                if (circleStyle == 2) {
                    RoundedRectShader.fill(drawContext, x - 10, y - 9, 20, 20, color3, 0x00000000, 11, 0);
                }
                RoundedRectShader.fill(drawContext, x - (10 - circleStyle + 1), y - (9 - circleStyle + 1), 22 - circleStyle * 2, 22 - circleStyle * 2, color1, 0x00000000, 11 - (circleStyle - 1), 0);
                drawCircle(matrix, x, y + 1, 10.08F, 0, (int) (360 - (xp * fadeProgressAnim * 360)), color3, 0);
                RoundedRectShader.fill(drawContext, x - 7, y - 6, 14, 14, color3, 0x00000000, 9, 0);
                drawItem(drawContext, currentPet, x - 8, y - 7, 1F);
                if (showLevel) {
                    drawText(drawContext, displayLvl, x, y - 27 + animtext, leveltextColor, 0.75F, true, true);
                }
                drawText(drawContext, displayXP, x, y - 23 + (int) (2 * levelAnimProgress) - padding, textColor, 1F, true, true);
            }
            disableBlend();
        }
    }
}