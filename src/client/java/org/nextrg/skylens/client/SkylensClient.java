package org.nextrg.skylens.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.nextrg.skylens.client.HudEditor.SkylensScreen;
import org.nextrg.skylens.client.Main.*;
import static org.nextrg.skylens.client.Helpers.Errors.errorMessage;

public class SkylensClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfig.init();
        MissingEnchantments.init();
        PetOverlay.init();
        SkylensScreen.initialize();
        errorMessage();
        ItemTooltipCallback.EVENT.register((stack,cont,type, lines) -> {
            MissingEnchantments.getMissingEnchantments(stack, lines);
            PotatoBooks.showMissingPotatoBooks(stack, lines);
            PetLevelAbbreviation.shortenPetLevel(stack, lines);
        });
    }
}