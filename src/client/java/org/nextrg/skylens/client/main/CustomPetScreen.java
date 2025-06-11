package org.nextrg.skylens.client.main;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.nextrg.skylens.client.widgets.Pet;
import org.nextrg.skylens.client.widgets.SidebarButton;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.nextrg.skylens.client.rendering.Renderer.drawText;
import static org.nextrg.skylens.client.rendering.Renderer.roundRectangle;

public class CustomPetScreen extends HandledScreen<ScreenHandler> {
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public static ScreenHandler publicHandler;
    public static int width = 204;
    public static boolean miniMode = false;
    public static boolean showProgressBars = true;
    public static boolean refresh = true;
    public static String displayTitle = "Pets";
    
    public CustomPetScreen(ScreenHandler handler, PlayerInventory inventory, String title) {
        super(handler, inventory, Text.empty());
        publicHandler = handler;
        displayTitle = title;
    }
    
    @Override
    protected void init() {
        super.init();
        
        width = miniMode ? 204 : 306;
        
        var screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        var screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        var height = width / 7 * 4;
        var x = screenWidth / 2 - width / 2;
        var y = screenHeight / 2 - height / 2 + 9;
        
        var i = 0;
        var petX = (miniMode ? 1 : 3) + x;
        for (Slot slot : publicHandler.slots) {
            if ((slot.id >= 10 && slot.id <= 16)
                    || (slot.id >= 19 && slot.id <= 25)
                    || (slot.id >= 28 && slot.id <= 34)
                    || (slot.id >= 37 && slot.id <= 43)) {
                var size = width / 7;
                var line = i / 7;
                var column = i % 7;
                Pet pet = new Pet(petX + (column * size), y + (line * size), size, size, slot.getStack(), slot.id);
                this.addDrawableChild(pet);
                i++;
            }
            if (slot.id == 53) {
                SidebarButton left = new SidebarButton(x + width - 30 / 2 + 3, y + height + 4, 16, 16, 2);
                this.addDrawableChild(left);
            }
            if (slot.id == 45) {
                SidebarButton right = new SidebarButton(x - 3, y + height + 4, 16, 16, 3);
                this.addDrawableChild(right);
            }
        }
        
        SidebarButton miniMode = new SidebarButton(x + width - 30 / 2 - 1, y - 18, 16, 16, 1);
        this.addDrawableChild(miniMode);
        
        SidebarButton showLevel = new SidebarButton(x + width - 30 / 2 - 1 - 16 - 2, y - 18, 16, 16, 4);
        this.addDrawableChild(showLevel);
        
        SidebarButton search = new SidebarButton(x + width - 30 / 2 - 1 - 16 - 2 - 16 - 2, y - 18, 16, 16, 5);
        this.addDrawableChild(search);
        
        SidebarButton petVisibility = new SidebarButton(x + width / 2 - 55, y + height + 4, 22, 22, 9);
        this.addDrawableChild(petVisibility);
        
        var marginX = 0;
        if (displayTitle.contains("Exp Sharing") || displayTitle.contains("Choose Pet")) {
            marginX = 12;
        }
        
        SidebarButton close = new SidebarButton(x + width / 2 - 11 + marginX, y + height + 4, 22, 22, 6);
        this.addDrawableChild(close);
        
        SidebarButton goBack = new SidebarButton(x + width / 2 - 33 + marginX, y + height + 4, 22, 22, 7);
        this.addDrawableChild(goBack);
        
        SidebarButton sort = new SidebarButton(x + width / 2 - 12 + 23, y + height + 4, 22, 22, 8);
        this.addDrawableChild(sort);
        
        SidebarButton xpShare = new SidebarButton(x + width / 2 - 12 + 45, y + height + 4, 22, 22, 10);
        this.addDrawableChild(xpShare);
    }
    
    public static boolean isNotAPane(ItemStack item) {
        return !item.getItemName().toString().contains("glass_pane");
    }
    
    public static boolean isNotABarrier(ItemStack item) {
        return !item.getItemName().toString().contains("barrier");
    }
    
    public static void interactWithSlot(int id, int type) {
        var interactionManager = MinecraftClient.getInstance().interactionManager;
        var player = MinecraftClient.getInstance().player;
        SlotActionType action = HandledScreen.hasShiftDown() ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP;
        if (type == 2) type = 0;
        assert interactionManager != null;
        publicHandler.onSlotClick(id, type, action, player);
        interactionManager.clickSlot(publicHandler.syncId, id, type, action, player);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var screenWidth = context.getScaledWindowWidth();
        var screenHeight = context.getScaledWindowHeight();
        var padding = 6;
        var frameWidth = width + padding;
        var frameHeight = frameWidth / 7 * 4 + padding + 16;
        var x = screenWidth / 2 - frameWidth / 2;
        var y = screenHeight / 2 - frameHeight / 2;
        roundRectangle(context, x, y, frameWidth + 1, frameHeight, 6.5f, 0xFF232323, 0, 0);
        drawText(context, displayTitle, x + 8, y + 9 - textRenderer.fontHeight / 2 / 1.5f, 0x55FFFFFF, 1.5F, false, false);
        
        try {
            for (var element : this.children()) {
                if (element instanceof Drawable drawable) {
                    if (element instanceof SidebarButton) {
                        var type = ((SidebarButton) element).buttonType;
                        if (type != 2 && type != 3 && type <= 7) {
                            drawable.render(context, mouseX, mouseY, delta);
                        } else {
                            if ((type == 2 && isNotAPane(publicHandler.slots.get(53).getStack())) ||
                                    (type == 3 && isNotAPane(publicHandler.slots.get(45).getStack()))) {
                                drawable.render(context, mouseX, mouseY, delta);
                            }
                            if ((type == 8 || type == 9 || type == 10) && (!displayTitle.contains("Exp Sharing") && !displayTitle.contains("Choose Pet"))) {
                                drawable.render(context, mouseX, mouseY, delta);
                            }
                        }
                    } else {
                        drawable.render(context, mouseX, mouseY, delta);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        
        if (refresh) {
            scheduler.schedule(() -> this.init(), 25L, TimeUnit.MILLISECONDS);
            refresh = false;
        }
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    }
}
