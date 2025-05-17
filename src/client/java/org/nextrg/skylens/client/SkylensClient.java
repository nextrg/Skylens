package org.nextrg.skylens.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.nextrg.skylens.client.main.*;

import static org.nextrg.skylens.client.ModConfig.openConfig;
import static org.nextrg.skylens.client.main.HudEditor.openScreen;
import static org.nextrg.skylens.client.utils.Errors.errorMessage;
import static org.nextrg.skylens.client.utils.Tooltips.tooltipMiddleCache;

public class SkylensClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new ModConfig().init();
        MissingEnchants.init();
        PetOverlay.init();
        SlayerBossIntros.init();
        LowHpIndicator.init();
        EnhancedNoteblockSounds.init();
        errorMessage();
        tooltipMiddleCache();
        ItemTooltipCallback.EVENT.register((stack, cont, type, lines) -> {
            MissingEnchants.getMissingEnchantments(stack, lines);
            PotatoBooks.showMissingPotatoBooks(stack, lines);
            CompactLevelDisplay.shortenPetLevel(stack, lines);
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