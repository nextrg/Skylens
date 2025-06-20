package org.nextrg.skylens.client.main;

import earth.terrarium.olympus.client.pipelines.RoundedRectangle;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.nextrg.skylens.client.ModConfig;
import org.nextrg.skylens.client.widgets.Button;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.nextrg.skylens.client.main.PetOverlay.*;
import static org.nextrg.skylens.client.rendering.Renderer.*;
import static org.nextrg.skylens.client.utils.Text.getColorCode;
import static org.nextrg.skylens.client.utils.Text.hexToHexa;

public class HudEditor extends Screen {
    public HudEditor(Text title) {
        super(title);
    }
    
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Object lock = new Object();
    public static float anim = 0f;
    public static float transit = 0f;
    private int windowwidth;
    private int windowheight;
    public static boolean shouldanim = true;
    
    public static int currentPage = 1;
    public static int pages = 3;
    
    public static void setCurrentPage(int page) {
        currentPage = Math.clamp(page, 1, pages);
        openScreen(parent, false);
    }
    
    @Override
    protected void init() {
        var marginTop = 0; // <- A thing to change in future
        var paginationY = 132;
        var pagetwoX = 23 + textRenderer.getWidth("Page 1") + 6;
        if (currentPage == 1) {
            Button position = new Button(2, marginTop + 41, 151, 37,
                    ModConfig.petOverlayPosition.replace("_", " "),
                    "Placement of the overlay.", 1);
            this.addDrawableChild(position);
            Button style = new Button(2, marginTop + 41 + 37 + 2, 151, 24,
                    ModConfig.petOverlayStyle.replace("Style1", "Bar")
                            .replace("Style2", "Circular")
                            .replace("Style3", "Circular" + getColorCode("gray") + " (alt)"),
                    "", 2);
            this.addDrawableChild(style);
            Button showLvl = new Button(2, marginTop + 41 + 37 + 2 + 24 + 2, 151, 24,
                    "", "", 5);
            this.addDrawableChild(showLvl);
        }
        if (currentPage == 2) {
            Button theme = new Button(2, marginTop + 41, 151, 37,
                    getColorCode(ModConfig.petOverlayTheme.toLowerCase()) + (ModConfig.petOverlayTheme.equals("Custom") ? "§n" : "") + ModConfig.petOverlayTheme,
                    "Custom can be changed in mod's config.", 10);
            this.addDrawableChild(theme);
            Button petRarity = new Button(2, marginTop + 41 + 37 + 2, 151, 24,
                    "",
                    "", 3);
            this.addDrawableChild(petRarity);
        }
        if (currentPage == 3) {
            Button iconAlign = new Button(2, marginTop + 41, 151, 37,
                    String.valueOf(ModConfig.petOverlayIconAlign).replace("true", "Left").replace("false", "Right"),
                    "For bar style only.", 9);
            this.addDrawableChild(iconAlign);
            Button invertProgress = new Button(2, marginTop + 41 + 37 + 2, 151, 24,
                    String.valueOf(ModConfig.petOverlayInvert).replace("true", "Inverted").replace("false", "Default"),
                    "", 6);
            this.addDrawableChild(invertProgress);
            Button hideLevelIfFull = new Button(2, marginTop + 41 + 37 + 2 + 24 + 2, 151, 24,
                    "",
                    "", 11);
            this.addDrawableChild(hideLevelIfFull);
        }
        Button pageone = new Button(2, marginTop + paginationY, 19, 18,
                "",
                "", 7);
        this.addDrawableChild(pageone);
        Button pagetwo = new Button(pagetwoX, marginTop + paginationY, 19, 18,
                "",
                "", 8);
        this.addDrawableChild(pagetwo);
        Button exit = new Button(pagetwoX + 21, marginTop + paginationY, 63 + 6, 18,
                "Close",
                "", 4);
        this.addDrawableChild(exit);
    }
    
    static Screen parent = null;
    
    public static void openScreen(Screen screen, boolean doAnim) {
        parent = screen;
        final boolean[] open = {false};
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!open[0]) {
                if (doAnim) {
                    scheduler.schedule(() -> {
                    }, 290L, TimeUnit.MILLISECONDS);
                    shouldanim = true;
                    animationFade(true);
                    forceAnim(true);
                    setHudEditor(true);
                } else {
                    Button.setProgress(1f);
                    transit = 1f;
                }
                open[0] = true;
                client.setScreen(new HudEditor(Text.literal("HudEditor")));
            }
        });
    }
    
    public static boolean clickCooldown = false;
    
    public static void animationMargin(boolean show) {
        for (var i = 0; i < 60; i++) {
            final float progress = (float) i / 59f;
            scheduler.schedule(() -> {
                synchronized (lock) {
                    float easing = easeInOutCubic(progress);
                    anim = show ? Math.min(easing, 1) : Math.max(0, 1 - easing);
                }
            }, i * 5L, TimeUnit.MILLISECONDS);
        }
    }
    
    public static void animationFade(boolean show) {
        for (var i = 0; i < 72; i++) {
            final float progress = (float) i / 71f;
            scheduler.schedule(() -> {
                synchronized (lock) {
                    float easing = easeInOutCubic(progress);
                    transit = show ? Math.min(easing, 1) : Math.max(0, 1 - easing);
                    Button.setProgress(transit);
                }
            }, i * 5L, TimeUnit.MILLISECONDS);
        }
    }
    
    public boolean withinArea(double x, double y) {
        var dwidth = switch (ModConfig.petOverlayPosition) {
            case "Left" -> -90;
            case "Right" -> windowwidth + 90;
            default -> windowwidth / 2;
        };
        boolean flipSide = "Inventory_Right".equals(ModConfig.petOverlayPosition) || "Left".equals(ModConfig.petOverlayPosition);
        var inventoryX = dwidth + (flipSide ? 90 : -90 - (122 + 130));
        var inventoryY = windowheight - 95;
        return x >= inventoryX && x <= inventoryX + 122 + 130 &&
                y >= inventoryY && y <= inventoryY + 95;
    }
    
    public boolean hovered;
    
    @Override
    public final boolean mouseClicked(double a, double b, int c) {
        if (withinArea(a, b) && !clickCooldown) {
            clickCooldown = true;
            animationMargin(true);
            hovered = true;
            setMargin(a, b);
        }
        return super.mouseClicked(a, b, c);
    }
    
    public void setMargin(double a, double b) {
        boolean flipSide = "Inventory_Right".equals(ModConfig.petOverlayPosition) || "Left".equals(ModConfig.petOverlayPosition);
        var dwidth = switch (ModConfig.petOverlayPosition) {
            case "Left" -> -90;
            case "Right" -> windowwidth + 90;
            default -> windowwidth / 2;
        };
        var x = ((flipSide ? (a - dwidth) : (dwidth - a)) - 90 - 25);
        ModConfig.petOverlayX = Math.clamp((int) x, 0, 195);
        ModConfig.petOverlayY = Math.clamp((int) (windowheight - b - 15), 0, 65);
    }
    
    @Override
    public final boolean mouseDragged(double mouseX, double mouseY, int a, double b, double c) {
        if (hovered) {
            setMargin(mouseX, mouseY);
        }
        return super.mouseDragged(mouseX, mouseY, a, b, c);
    }
    
    @Override
    public final boolean mouseReleased(double a, double b, int c) {
        if (hovered) {
            scheduler.schedule(() -> {
                clickCooldown = false;
            }, 230L, TimeUnit.MILLISECONDS);
            animationMargin(false);
            hovered = false;
        }
        return super.mouseReleased(a, b, c);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        windowwidth = context.getScaledWindowWidth();
        windowheight = context.getScaledWindowHeight();
        renderPetOverlayEditor(context);
        renderHeader(context);
        prepare(context, false);
        var currenttextpage = "Page " + currentPage;
        drawText(context, currenttextpage, -148 + (int) (transit * 148) + 23 + textRenderer.getWidth(currenttextpage) / 2 + 2, 132 + textRenderer.fontHeight / 2 + 1, 0xFFFFFFFF, 1F, true, true);
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }
    
    public void renderHeader(DrawContext context) {
        float amount = (float) (Util.getMeasuringTimeMs() / 2600.0) % 1;
        var color = hexToHexa(0xFF131313, (int) (215 * transit));
        RoundedRectangle.draw(context, -4, -4 - 30 + (int) (30 * transit), 158, 44, color, 0x00000000, 3, 1);
        context.enableScissor(42, (int) (20 * transit), 144, (int) (50 * transit));
        drawText(context, "for Skylens", -90 + 42 + (int) (amount * 210), -21 + (int) (30 * transit) + 16, hexToHexa(0xFF9F9F9F, (int) (10 + transit * 245)), 1F, false, false);
        context.disableScissor();
        drawText(context, "\uD83D\uDD27", 8 + 2, 4 + (int) (transit * 45) - 45, hexToHexa(0xFFFFFFFF, (int) (10 + transit * 245)), 3F + transit, false, false);
        drawText(context, "Hud Editor", 42, -21 + (int) (30 * transit), hexToHexa(0xFFFFFFFF, (int) (10 + transit * 245)), 2F, false, false);
        var helperText = "To quickly access this menu, use /skylens hudedit".split("(?<=,)(?=\\s*)");
        for (var i = 0; i < helperText.length; i++) {
            var text = helperText[i];
            drawText(context, text, windowwidth - this.textRenderer.getWidth(text) - 10, -21 + (int) (30 * transit) + this.textRenderer.fontHeight * i, hexToHexa(0xFFFFFFFF, (int) (10 + transit * 245)), 1F, false, true);
        }
    }
    
    public void renderPetOverlayEditor(DrawContext context) {
        var dwidth = switch (ModConfig.petOverlayPosition) {
            case "Left" -> -90;
            case "Right" -> windowwidth + 90;
            default -> windowwidth / 2;
        };
        boolean flipSide = "Inventory_Right".equals(ModConfig.petOverlayPosition) || "Left".equals(ModConfig.petOverlayPosition);
        var inventoryX = dwidth + (flipSide ? 90 : -90 - (123 + 130));
        var inventoryY = windowheight - 95;
        var marginX = ModConfig.petOverlayX;
        var marginY = ModConfig.petOverlayY;
        var x = inventoryX + (flipSide ? marginX : (65 + 130) - marginX);
        var y = inventoryY + (65 - ModConfig.petOverlayY);
        var isBar = Objects.equals(ModConfig.petOverlayStyle, "Style1");
        var stuff = (isBar ? 0 : (flipSide ? 22 : 0));
        var bstuff = (isBar ? 0 : (flipSide ? 0 : 22));
        var cstuff = (isBar ? 0 : (flipSide ? 22 : -22));
        var barColor = hexToHexa(0xFFFFFFFF, (int) (12 * transit) + (int) (anim * 148));
        var textColor = hexToHexa(0xFFFFFFFF, 10 + (int) (12 * transit) + (int) (anim * 148));
        
        var arg1 = marginX <= (62 + 130);
        var arg2 = marginX >= 3;
        if (flipSide && arg2 || !flipSide && arg1) {
            var center = inventoryX + bstuff + ((x - 2 + bstuff) - (inventoryX + bstuff)) / 2;
            context.drawHorizontalLine(inventoryX + bstuff + 1, x - 2 + bstuff, y + 15, barColor);
            var display = flipSide ? marginX : 65 + 130 - marginX;
            context.drawText(textRenderer, String.valueOf(display), center - (display <= 9 ? 2 : 5), y + 6, textColor, false);
        }
        if (flipSide && arg1 || !flipSide && arg2) {
            var center = x + 59 - stuff + ((inventoryX + 120 + 130 - stuff) - (x + 59 - stuff)) / 2;
            context.drawHorizontalLine(x + 59 - stuff, inventoryX + 120 + 130 - stuff + 1, y + 15, barColor);
            var display = flipSide ? (65 + 130) - marginX : marginX;
            context.drawText(textRenderer, String.valueOf(display), center - (display <= 9 ? 2 : 5), y + 6, textColor, false);
        }
        if (marginY <= 62) {
            var center = inventoryY - (isBar ? 0 : 15) + ((inventoryY + 65 - marginY) - inventoryY) / 2;
            context.drawVerticalLine(x + 29 - cstuff / 2, inventoryY - (isBar ? 0 : 15), inventoryY + 65 - marginY - 1 - (isBar ? 0 : 15), barColor);
            var display = 65 - marginY;
            context.drawText(textRenderer, String.valueOf(display), (x + 8 + (display <= 9 ? 13 : 8)) - cstuff / 2, center - 3, textColor, false);
        }
        if (marginY >= 3) {
            var center = windowheight - marginY / 2;
            context.drawVerticalLine(x + 29 - cstuff / 2, windowheight - marginY, windowheight - 1, barColor);
            context.drawText(textRenderer, String.valueOf(marginY), (x + 8 + (marginY <= 9 ? 13 : 8)) - cstuff / 2, center - 3, textColor, false);
        }
        
        roundRectangle(context, inventoryX + (!isBar && !flipSide ? 22 : 0), inventoryY - (isBar ? 0 : 15), 123 + 130 - (isBar ? 0 : 22), 95 + (isBar ? 0 : 15), 4, hexToHexa(0xFFFFFFFF, (int) (40 * transit) + (int) (45 * anim)), 0, 0);
        if (isBar) {
            roundRectangle(context, x, y, 58, 30, 4, hexToHexa(0xFFFFFFFF, (int) (60 * transit) + (int) (40 * anim)), 0, 0);
        } else {
            roundRectangle(context, x + (flipSide ? 0 : 22), y - 15, 36, 45, 4, hexToHexa(0xFFFFFFFF, (int) (60 * transit) + (int) (40 * anim)), 0, 0);
        }
    }
    
    public static void closeScreen(boolean doAnim) {
        if (shouldanim) {
            ModConfig.get().update();
            shouldanim = false;
            forceAnim(false);
            if (doAnim) {
                animationFade(false);
            } else {
                transit = 0f;
                Button.setProgress(0f);
            }
            scheduler.schedule(() -> {
                final boolean[] open = {false};
                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                    if (!open[0]) {
                        setHudEditor(false);
                        open[0] = true;
                        client.setScreen(parent);
                        parent = null;
                    }
                });
            }, 290L, TimeUnit.MILLISECONDS);
        }
    }
    
    @Override
    public void close() {
        closeScreen(true);
    }
}
