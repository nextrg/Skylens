package org.nextrg.skylens.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;
import org.nextrg.skylens.client.main.*;

import static org.nextrg.skylens.client.ModConfig.openConfig;
import static org.nextrg.skylens.client.main.HudEditor.openScreen;
import static org.nextrg.skylens.client.utils.Errors.errorMessage;
import static org.nextrg.skylens.client.utils.Tooltips.tooltipMiddleCache;

public class SkylensClient implements ClientModInitializer {

    private static ModContainer mod;

    @Override
    public void onInitializeClient() {
        mod = FabricLoader.getInstance().getModContainer("skylens").get();
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

    public static Identifier id(String path) {
        return Identifier.of(mod.getMetadata().getId(), path);
    }
}