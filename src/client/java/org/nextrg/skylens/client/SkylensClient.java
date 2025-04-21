package org.nextrg.skylens.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.nextrg.skylens.client.hudeditor.HudEditor;
import org.nextrg.skylens.client.main.*;
import org.nextrg.skylens.client.utils.CustomSounds;

import static org.nextrg.skylens.client.utils.Errors.errorMessage;
import static org.nextrg.skylens.client.utils.Tooltips.tooltipMiddleCache;

public class SkylensClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfig.init();
        MissingEnchantments.init();
        PetOverlay.init();
        SlayerIntros.init();
        HudEditor.initialize();
        CustomSounds.initialize();
        errorMessage();
        tooltipMiddleCache();
        ItemTooltipCallback.EVENT.register((stack,cont,type, lines) -> {
            MissingEnchantments.getMissingEnchantments(stack, lines);
            PotatoBooks.showMissingPotatoBooks(stack, lines);
            PetLevelAbbreviation.shortenPetLevel(stack, lines);
        });
    }
}