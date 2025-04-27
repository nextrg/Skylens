package org.nextrg.skylens.client.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.nextrg.skylens.client.main.CustomPetScreen;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.nextrg.skylens.client.main.CustomPetScreen.*;
import static org.nextrg.skylens.client.main.PetOverlay.themeColors;
import static org.nextrg.skylens.client.utils.Errors.logErr;
import static org.nextrg.skylens.client.utils.Other.getPetRarity;
import static org.nextrg.skylens.client.rendering.Renderer.*;
import static org.nextrg.skylens.client.utils.Text.getLiteral;
import static org.nextrg.skylens.client.utils.Text.hexToHexa;
import static org.nextrg.skylens.client.utils.Tooltips.getLore;

public class Pet extends ClickableWidget {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Object lock = new Object();
    public int id;
    public ItemStack pet;
    public int level = 1;
    public float xp = 0f;
    public int maxLevel = 100;
    public int tooltipWidth;
    public int tooltipHeight;
    public boolean petExists = true;
    public boolean isFavorited = false;
    public String rarity;
    public int[] colors = new int[]{0xFF191919,0xFF202020,0x0};
    
    public Pet(int x, int y, int width, int height, ItemStack petStack, Integer petId) {
        super(x, y, width, height, Text.empty());
        id = petId;
        pet = petStack;
        if (!pet.isEmpty()) {
            rarity = getPetRarity(pet);
            colors = themeColors.getOrDefault(rarity, new int[]{0xFF9A9A9A, 0xFFFFFFFF, 0xFF636363});
        }
        var lines = getLore(pet);
        if (pet.getCustomName() != null) {
            isFavorited = pet.getCustomName().toString().contains("⭐");
        }
        petExists = !pet.isEmpty();
        try {
            for (var line : lines) {
                if (line.toString().contains("Progress to")) {
                    String levelProgress = getLiteral(line.getSiblings().getFirst().getContent().toString()).replace("Progress to Level ", "").replace(":", "").trim();
                    String xpProgress = getLiteral(line.getSiblings().getLast().getContent().toString().replace("%", ""));
                    maxLevel = (lines.toString().contains("Golden Dragon")) ? 200 : 100;
                    level = Integer.parseInt(levelProgress.trim()) - 1;
                    xp = Float.parseFloat(xpProgress.trim()) / 100;
                } else if (line.toString().contains("MAX LEVEL")) {
                    level = 100;
                    xp = 1f;
                }
                var txtRd = MinecraftClient.getInstance().textRenderer;
                int lineWidth = txtRd.getWidth(line);
                if (lineWidth > tooltipWidth) {
                    tooltipWidth = lineWidth; // the widest line defines the size of its tooltip
                }
                tooltipHeight = getLore(pet).size() * txtRd.fontHeight;
            }
        } catch (Exception e) {
            logErr(e, "Caught an error processing data for pet");
        }
    }
    
    public void onClick(double mouseX, double mouseY, int type) {
        interactWithSlot(id, type);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        onClick(mouseX, mouseY, button);
        if (button == 1 || button == 2) {
            playDownSound(MinecraftClient.getInstance().getSoundManager());
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
    
    Identifier favoritedIcon = Identifier.of("skylens", "textures/gui/star.png");
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        var deadZone = 1;
        boolean isHovered =
                mouseX >= getX() + deadZone &&
                mouseX <= getX() - deadZone + this.width &&
                mouseY >= getY() + deadZone &&
                mouseY <= getY() - deadZone + this.height;
        this.hovered = isHovered;
        if (isHovered != hoveredLastFrame) {
            animateHover(isHovered);
            hoveredLastFrame = isHovered;
        }
        
        // 1x -> 0, 2x -> 1
        int menuScale = (CustomPetScreen.width - 204) / 102;
        var equipped = petExists && getLore(pet).toString().contains("Click to despawn!");
        var actualTransitValue = 1f;
        
        if (!equipped) {
            actualTransitValue = transit;
        }
        
        if (hovered && petExists) {
            var client = MinecraftClient.getInstance();
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            
            boolean facingLeft = width / 2 + 100 + 12 + tooltipWidth > width;
            
            context.drawItemTooltip(client.textRenderer, pet, width / 2 + (facingLeft ? -tooltipWidth - CustomPetScreen.width * 3/5 + 10 : 99 + menuScale * 50), height / 2 - tooltipHeight / 2);
        }
        
        if (isPane(pet)) {
            roundRectangleHiddenBorder(context, getX(), getY(), this.width, this.height, ColorHelper.lerp(actualTransitValue, colors[0], colors[1]), 5);
        }
        
        if (petExists && level == maxLevel && isPane(pet)) {
            roundGradientBorderless(context, getX(), getY(), this.width, this.height, colors[2], colors[0], 5, 0);
        }
        
        if (equipped) {
            roundRectangleHiddenBorder(context, getX(), getY(), this.width, this.height, hexToHexa(colors[1], 185), 5);
        }
        
        var itemScale = 1.25f + menuScale * 0.75f * 0.5f * (1f + actualTransitValue);
        var center = 6.5 + menuScale * 7;
        drawItem(context, pet, (float) (getX() + center), (float) (getY() + center - (showProgressBars ? 2 : 0)), (float) Math.round(itemScale * 15) / 15);
        
        if (petExists && isPane(pet)) {
            if (showProgressBars && !displayTitle.contains("Choose Pet")) {
                var progressBarHeight = 10 + 2 * menuScale;
                roundRectangle(context, getX(), getY() + this.height - progressBarHeight, this.width, progressBarHeight, ColorHelper.lerp(actualTransitValue, colors[2], colors[0]), 0x00000000, 2f + 0.6f * menuScale, 3);
                roundRectangle(context, getX(), getY() + this.height - progressBarHeight, Math.max((this.width * level / maxLevel), 8), progressBarHeight, ColorHelper.lerp(actualTransitValue, colors[1], colors[2]), 0x00000000, 2f + 0.6f * menuScale, 3);
                roundRectangle(context, getX() + 1, getY() + 1 + this.height - progressBarHeight, Math.max((int) (this.width * xp), 4) - 2, progressBarHeight - 2, ColorHelper.lerp(actualTransitValue, colors[0], colors[1]), 0x00000000, 0.6f + 1.2f * menuScale, 3);
            }
            drawText(context, String.valueOf(level == maxLevel ? "MAX" : level), getX() + 2.8f, getY() + 2.8f, ColorHelper.lerp(actualTransitValue, colors[1], 0xFF000000), 0.5F + 0.5F * menuScale, false, false);
            if (isFavorited) {
                context.drawTexture(RenderLayer::getGuiTextured, favoritedIcon, getX() + this.width - 11 - 2, getY() + 2, 0, 0, 11, 11, 11, 11);
            }
        }
    }
    
    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    
    }
}
