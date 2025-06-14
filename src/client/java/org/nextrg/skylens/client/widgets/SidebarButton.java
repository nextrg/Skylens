package org.nextrg.skylens.client.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.nextrg.skylens.client.main.CustomPetScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.nextrg.skylens.client.main.CustomPetScreen.interactWithSlot;
import static org.nextrg.skylens.client.rendering.Renderer.*;
import static org.nextrg.skylens.client.utils.Text.getColorCode;
import static org.nextrg.skylens.client.utils.Tooltips.getLore;

public class SidebarButton extends ClickableWidget {
    public int buttonType;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Object lock = new Object();
    
    public SidebarButton(int x, int y, int width, int height, int type) {
        super(x, y, width, height, Text.empty());
        buttonType = type;
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
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (buttonType == 1) {
            CustomPetScreen.miniMode = !CustomPetScreen.miniMode;
            var client = MinecraftClient.getInstance();
            client.setScreen(client.currentScreen);
        }
        if (buttonType == 2) {
            interactWithSlot(53, button);
        }
        if (buttonType == 3) {
            interactWithSlot(45, button);
        }
        if (buttonType == 4) {
            CustomPetScreen.showProgressBars = !CustomPetScreen.showProgressBars;
        }
        if (buttonType == 5) {
            interactWithSlot(50, button);
        }
        if (buttonType == 6) {
            interactWithSlot(49, button);
        }
        if (buttonType == 7) {
            interactWithSlot(48, button);
        }
        if (buttonType == 8) {
            interactWithSlot(52, button);
        }
        if (buttonType == 9) {
            interactWithSlot(51, button);
        }
        if (buttonType == 10) {
            interactWithSlot(7, button);
        }
        
        if (button == 1 || button == 2) {
            playDownSound(MinecraftClient.getInstance().getSoundManager());
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (hovered != hoveredLastFrame) {
            animateHover(hovered);
            hoveredLastFrame = hovered;
        }
        var color1 = 0xFF0F0F0F;
        var color2 = 0xFF393939;
        var bool = buttonType == 5;
        if (buttonType == 1) {
            bool = !CustomPetScreen.miniMode;
        }
        if (buttonType == 4) {
            bool = CustomPetScreen.showProgressBars;
        }
        roundRectangle(context, getX(), getY(), this.width, this.height, 4, bool ? ColorHelper.lerp(transit, color1, color2) : ColorHelper.lerp(transit, color2, color1), 1, 0);
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        var text = switch (buttonType) {
            case 10 -> "✧";
            case 9 -> "\uD83E\uDEE5";
            case 8 -> "\uD83E\uDDF9";
            case 7 -> "←";
            case 6 -> "\u2715";
            case 5 -> "\uD83D\uDD0D";
            case 4 -> "\uD83D\uDCCA";
            case 3 -> "<";
            case 2 -> ">";
            default -> "\uD83D\uDD33";
        };
        drawText(context, text, getX() + this.width / 2, getY() + this.height / 2 - textRenderer.fontHeight * 1f / 2 + 1, 0xFFFFFFFF, 1f, true, false);
        if (hovered) {
            var tooltip = switch (buttonType) {
                case 10 -> 7;
                case 9 -> 51;
                case 8 -> 52;
                case 7 -> 48;
                case 6 -> 49;
                case 5 -> 50;
                case 3 -> 45;
                case 2 -> 53;
                default -> 0;
            };
            
            var tooltipContent = Text.of(getColorCode("lightpurple") + (buttonType == 1 ? "Compact Mode" : "Hide Progress Bars"));
            List<Text> content = new ArrayList<>();
            var multipleLines = false;
            var tooltipWidth = textRenderer.getWidth(tooltipContent);
            var tooltipHeight = 0;
            if (buttonType != 4 && buttonType != 1) {
                tooltipWidth = 0;
                var item = CustomPetScreen.publicHandler.slots.get(tooltip).getStack();
                content = getLore(item);
                multipleLines = true;
                List<Text> finalContent = new ArrayList<>();
                finalContent.add(item.getCustomName());
                finalContent.addAll(content);
                content = finalContent;
                for (var lines : finalContent) {
                    var length = textRenderer.getWidth(lines);
                    if (length > tooltipWidth) {
                        tooltipWidth = length;
                    }
                }
                tooltipHeight = textRenderer.fontHeight * (content.size());
            }
            var x = getX() - tooltipWidth / 2 - 1;
            var y = getY() - 1 - tooltipHeight;
            if (multipleLines) {
                context.drawTooltip(textRenderer, content, x, y);
            } else {
                context.drawTooltip(textRenderer, tooltipContent, x, y);
            }
        }
    }
    
    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    
    }
}
