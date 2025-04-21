package org.nextrg.skylens.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.nextrg.skylens.client.main.*;
import org.nextrg.skylens.client.main.EnhancedNoteblockSounds;
import static org.nextrg.skylens.client.ModConfig.openConfig;
import static org.nextrg.skylens.client.hudeditor.HudEditor.openScreen;
import static org.nextrg.skylens.client.utils.Errors.errorMessage;
import static org.nextrg.skylens.client.utils.Tooltips.tooltipMiddleCache;

public class SkylensClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfig modConfig = new ModConfig();
        modConfig.init();
        MissingEnchantments.init();
        PetOverlay.init();
        SlayerIntros.init();
        EnhancedNoteblockSounds.initialize();
        errorMessage();
        tooltipMiddleCache();
        ItemTooltipCallback.EVENT.register((stack,cont,type, lines) -> {
            MissingEnchantments.getMissingEnchantments(stack, lines);
            PotatoBooks.showMissingPotatoBooks(stack, lines);
            PetLevelAbbreviation.shortenPetLevel(stack, lines);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("skylens")
                    .executes(context -> {
                        openConfig();
                        return 1;
                    })
                    .then(ClientCommandManager.literal("hudedit")
                            .executes(context -> {
                                openScreen(null, true);
                                return 1;
                            })
                    )
            );
        });
    }
}