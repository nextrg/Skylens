package org.nextrg.skylens.client.hudeditor;

import earth.terrarium.olympus.client.shader.builtin.RoundedRectShader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.nextrg.skylens.client.ModConfig;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static org.nextrg.skylens.client.utils.Renderer.*;
import static org.nextrg.skylens.client.utils.Text.getColorCode;
import static org.nextrg.skylens.client.utils.Text.hexToHexa;
import static org.nextrg.skylens.client.hudeditor.HudEditor.*;

public class Button extends ClickableWidget {
    public String displayText;
    public int btype; // button type
    public String desc;
    private boolean bool;
    public static float progress; // variable for anim when opening/closing the screen
    public Button(int x, int y, int width, int height, String text, String description, int type) {
        super(x, y, width, height, Text.literal(text));
        btype = type;
        displayText = text;
        desc = description;
        if (btype == 3) {
            bool = ModConfig.petOverlayPetRarity;
        }
        if (btype == 5) {
            bool = ModConfig.petOverlayShowLvl;
        }
        if (btype == 6) {
            bool = ModConfig.petOverlayInvert;
        }
        if (btype == 11) {
            bool = ModConfig.petOverlayHideLvlFull;
        }
        button = (bool ? 1f : 0f);
    }
    
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Object lock = new Object();
    
    public static void setProgress(float prog) {
        progress = prog;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        onClick(mouseX, mouseY, button);
        if (button == 1 || button == 2) {
            playDownSound(MinecraftClient.getInstance().getSoundManager());
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void onClick(double mouseX, double mouseY, int type) {
        try {
            if (btype == 1) {
                var position = ModConfig.petOverlayPosition;
                var value = position.replace("Inventory_Left", "3")
                        .replace("Inventory_Right", "4")
                        .replace("Left", "1")
                        .replace("Right", "2");
                int option;
                try {option = Integer.parseInt(value);} catch (NumberFormatException ignored) {option = 4;}
                if (type == 0) {
                    option += (option < 4 ? 1 : -3);
                } else if (type == 1) {
                    option -= (option > 1 ? 1 : -3);
                } else {
                    option = 4;
                }
                var text = String.valueOf(option)
                        .replace("3", "Inventory_Left")
                        .replace("4", "Inventory_Right")
                        .replace("1", "Left")
                        .replace("2", "Right");
                ModConfig.petOverlayPosition = text;
                displayText = text.replace("_", " ");
            }
            if (btype == 2) {
                var name = ModConfig.petOverlayStyle;
                var value = name.substring(name.length() - 1);
                int style = 1;
                try {style = Integer.parseInt(value);} catch (NumberFormatException ignored) {}
                if (type == 0) {
                    style += (style < 3 ? 1 : -2);
                } else if (type == 1) {
                    style -= (style < 1 ? 1 : -2);
                } else {
                    style = 1;
                }
                var text = "Style" + style;
                ModConfig.petOverlayStyle = text;
                displayText = text.replace("Style1", "Bar")
                        .replace("Style2", "Circular")
                        .replace("Style3", "Circular" + getColorCode("gray") + " (alt)");
            }
            if (btype == 3) {
                ModConfig.petOverlayPetRarity = type == 2 || !ModConfig.petOverlayPetRarity;
                animateSwitch(ModConfig.petOverlayPetRarity);
            }
            if (btype == 5) {
                ModConfig.petOverlayShowLvl = type == 2 || !ModConfig.petOverlayShowLvl;
                animateSwitch(ModConfig.petOverlayShowLvl);
            }
            if (btype == 6) {
                ModConfig.petOverlayInvert = type != 2 && !ModConfig.petOverlayInvert;
                displayText = String.valueOf(ModConfig.petOverlayInvert).replace("true", "Inverted").replace("false", "Default");
            }
            var targetPage = currentPage;
            if (btype == 7) {
                if (targetPage - 1 < 1) {
                    targetPage = pages;
                } else {
                    targetPage -= 1;
                }
            }
            if (btype == 8) {
                if (targetPage + 1 > pages) {
                    targetPage = 1;
                } else {
                    targetPage += 1;
                }
            }
            if (btype == 7 || btype == 8) {
                HudEditor.setCurrentPage(targetPage);
            }
            if (btype == 9) {
                ModConfig.petOverlayIconAlign = type != 2 && !ModConfig.petOverlayIconAlign;
                displayText = String.valueOf(ModConfig.petOverlayIconAlign).replace("true", "Left").replace("false", "Right");
            }
            if (btype == 10) {
                var text = "Special";
                if (type != 2) {
                    var currentTheme = ModConfig.Themes.valueOf(ModConfig.petOverlayTheme);
                    var next = (currentTheme.ordinal() + (type == 0 ? -1 : 1) + ModConfig.Themes.values().length) % ModConfig.Themes.values().length;
                    text = String.valueOf(ModConfig.Themes.values()[next]);
                }
                ModConfig.petOverlayTheme = text;
                displayText = getColorCode(text.toLowerCase()) + (text.equals("Custom") ? "§n" : "") + text;
            }
            if (btype == 11) {
                ModConfig.petOverlayHideLvlFull = type != 2 && !ModConfig.petOverlayHideLvlFull;
                animateSwitch(ModConfig.petOverlayHideLvlFull);
            }
        } catch (Exception ignored) {}
        if (btype == 4) {
            closeScreen(true);
        }
        animateClick();
        
        super.onClick(mouseX, mouseY);
    }
    
    private float transit = 0f;
    private boolean hoveredLastFrame = false;
    private boolean animationRunning = false;
    private boolean animatingToVisible = false;
    private void animateHover(boolean show) {
        if (animationRunning && show == animatingToVisible) return;
        animationRunning = true;
        animatingToVisible = show;
        for (int i = 0; i < 60; i++) {
            final float progress = i / 59f;
            final int step = i;
            scheduler.schedule(() -> {
                synchronized (lock) {
                    if (animatingToVisible != show) return;
                    if (animatingToVisible) {
                        transit = Math.min(easeInOutCubic(progress), 1f);
                    } else {
                        transit = Math.max(0f, 1 - easeInOutCubic(progress));
                    }
                    if (step == 59) {
                        animationRunning = false;
                    }
                }
            }, i * 3L, TimeUnit.MILLISECONDS);
        }
    }
    
    private float button;
    private Boolean lastAnimatedValue = null;
    private void animateSwitch(boolean show) {
        if (lastAnimatedValue == null) { lastAnimatedValue = bool; }
        if (lastAnimatedValue == show) {
            return;
        }
        lastAnimatedValue = show;
        for (int i = 0; i < 60; i++) {
            final float progress = i / 59f;
            final boolean animTrue = show;
            scheduler.schedule(() -> {
                synchronized (lock) {
                    if (animTrue) {
                        button = Math.min(easeInOutCubic(progress), 1f);
                    } else {
                        button = Math.max(0f, 1 - easeInOutCubic(progress));
                    }
                }
            }, i * 3L, TimeUnit.MILLISECONDS);
        }
    }
    
    private float press = 0f;
    private void animateClick() {
        scheduler.schedule(() -> {
            final int steps = 60;
            final long stepDelay = 6L;
            for (int i = 0; i < steps; i++) {
                final float progress = (float) i / (steps - 1);
                final long delay = i * stepDelay;
                scheduler.schedule(() -> {
                    synchronized (lock) {
                        press = 1f - easeInOutCubic(progress);
                    }
                }, delay, TimeUnit.MILLISECONDS);
            }
        }, 0, TimeUnit.SECONDS);
    }
    
    int color1 = 0xFF626262;
    int color2 = 0xFF5e7edf;
    int color3 = 0xFFFFFFFF;
    int color4 = 0xFFc4cfff;
    
    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (isHovered() != hoveredLastFrame) {
            animateHover(isHovered());
            hoveredLastFrame = isHovered();
        }
        boolean isPages = btype == 7 || btype == 8;
        boolean isTheme = btype == 10;
        String part = switch (btype) {
            case 11 -> "Hide Level If Maxed";
            case 10 -> "Theme:";
            case 9 -> "Icon Alignment:";
            case 6 -> "Progress Color:";
            case 5 -> "Show Level";
            case 4 -> "";
            case 3 -> "Pet Rarity Theme";
            case 2 -> "Style:";
            case 1 -> "Position:";
            default -> (btype == 7) ? "<" : ">";
        };
        boolean isSwitch = !part.contains(":") && part.length() > 1;
        var x = (int) (getX() - 150 + 150 * progress) + (int) ((btype == 4 || isPages ? 0 : 4) * transit);
        RoundedRectShader.fill(context, x - 1, getY() - 1, this.width + 2, this.height + 2, hexToHexa(0xFF252525, (int)(192 * progress)), 0x00000000, 5, 1);
        var center = getY() + this.height / 2 - MinecraftClient.getInstance().textRenderer.fontHeight / 2;
        RoundedRectShader.fill(context, x + (this.width + 2) / 2 - (int) ((float) (this.width + 2) / 2 * transit) - 1, getY() - 1, (int) ((this.width + 2) * transit), this.height + 2, hexToHexa(ColorHelper.lerp(isSwitch ? 0f : press, 0xFF353535, 0xFF454545), (int)(255 * progress)), 0x00000000, 5, 1);
        var hasDesc = (isTheme ? 7 : !(Objects.equals(desc, "")) ? 5 : 0);
        context.drawText(MinecraftClient.getInstance().textRenderer, part + " " + displayText, x + (btype == 4 ? 7 : 0) + (isPages ? (btype == 7 ? 7 : 8) : 10), center - hasDesc + (int) (hasDesc - hasDesc * transit), hexToHexa(0xFFFFFFFF, (int) (progress * 245 + 10)), false);
        context.drawText(MinecraftClient.getInstance().textRenderer, isTheme ? "Custom can be changed" : desc, x + 10, center + (int) (hasDesc * transit) - (isTheme ? 4 : 0), hexToHexa(0xFF999999, (int) (transit * 245 + 10)), false);
        if (isTheme) {
            context.drawText(MinecraftClient.getInstance().textRenderer, "in mod's config.", x + 10, center + (int) (hasDesc * transit) + 4, hexToHexa(0xFF999999, (int) (transit * 245 + 10)), false);
        }
        if (isSwitch) {
            var buttonX = x + this.width - 28;
            var buttonY = getY() + this.height / 2 - 6;
            RoundedRectShader.fill(context, buttonX + 1, buttonY + 1, 25 - 4, 14 - 4, hexToHexa(ColorHelper.lerp(button, color1, color2), (int) (255 * progress)), 0x00000000, 4, 1);
            RoundedRectShader.fill(context, buttonX + (int) (11 * button), buttonY, 12, 12, hexToHexa(ColorHelper.lerp(button, color3, color4), (int) (255 * progress)), 0x00000000, 5, 1);
        }
    }
    
    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    
    }
}
