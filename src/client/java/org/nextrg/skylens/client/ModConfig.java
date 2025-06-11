package org.nextrg.skylens.client;

import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.awt.*;
import java.time.LocalDate;

import static org.nextrg.skylens.client.main.HudEditor.openScreen;
import static org.nextrg.skylens.client.utils.Text.getColorCode;
import static org.nextrg.skylens.client.utils.Text.rgbToHexa;

public class ModConfig implements ModMenuApi {
    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.of("skylens"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("skylens.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();
    
    @SerialEntry
    public static boolean compactLevel = true;
    @SerialEntry
    public static boolean onlySkyblock = true;
    @SerialEntry
    public static boolean showErrors = false;
    
    @SerialEntry
    public static boolean enhancedNoteblockSounds = true;
    @SerialEntry
    public static float noteblockGeneralVolume = 1.0F;
    @SerialEntry
    public static float noteblockBasedrumVolume = 1.0F;
    @SerialEntry
    public static float noteblockHatVolume = 1.0F;
    @SerialEntry
    public static float noteblockSnareVolume = 1.0F;
    @SerialEntry
    public static float noteblockBassVolume = 0.333F;
    
    @SerialEntry
    public static boolean missingEnchants = true;
    @SerialEntry
    public static Color missingEnchantsEnabled = new Color(85, 255, 255);
    @SerialEntry
    public static Color missingEnchantsDisabled = new Color(0, 170, 170);
    
    @SerialEntry
    public static boolean slayerIntros = true;
    @SerialEntry
    public static String slayerIntrosBackground = "Opaque";
    
    @SerialEntry
    public static boolean missingPotatoBooks = true;
    @SerialEntry
    public static Color missingPotatoBooksColor = new Color(252, 168, 0);
    @SerialEntry
    public static String missingPotatoBooksStyle = "Style1";
    
    @SerialEntry
    public static boolean petOverlay = true;
    @SerialEntry
    public static String petOverlayStyle = "Style1";
    @SerialEntry
    public static String petOverlayTheme = "Custom";
    @SerialEntry
    public static boolean petOverlayPetRarity = true;
    @SerialEntry
    public static Color petOverlayColor1 = Color.WHITE;
    @SerialEntry
    public static Color petOverlayColor2 = Color.GRAY;
    @SerialEntry
    public static Color petOverlayColor3 = Color.DARK_GRAY;
    @SerialEntry
    public static int petOverlayX = 0;
    @SerialEntry
    public static int petOverlayY = 0;
    @SerialEntry
    public static boolean petOverlayInvert = false;
    @SerialEntry
    public static String petOverlayPosition = "Inventory_Right";
    @SerialEntry
    public static boolean petOverlayAnimFade = true;
    @SerialEntry
    public static boolean petOverlayAnimIdle = true;
    @SerialEntry
    public static boolean petOverlayAnimXP = true;
    @SerialEntry
    public static boolean petOverlayShowLvl = true;
    @SerialEntry
    public static boolean petOverlayIconAlign = true;
    @SerialEntry
    public static boolean petOverlayHideLvlFull = false;
    @SerialEntry
    public static boolean customPetMenu = true;
    
    private static Text volumeFormattedValue(Float val) {
        Color start = new Color(197, 242, 184);
        Color end = new Color(242, 184, 184);
        return Text.literal(Math.floor(val * 1000) / 10 + "%")
                .withColor(ColorHelper.lerp(val, rgbToHexa(start), rgbToHexa(end)));
    }
    
    private static Text instrumentFormattedValue(Float val) {
        if (val > 0f) {
            return volumeFormattedValue(val);
        } else {
            return Text.literal("Vanilla");
        }
    }
    
    private static ControllerBuilder<Float> volumeController(Option<Float> opt) {
        return FloatSliderControllerBuilder.create(opt)
                .range(0F, 1F)
                .step(0.005F)
                .formatValue(ModConfig::instrumentFormattedValue);
    }
    
    public enum PotatoBookStyles implements NameableEnum {
        Style1,
        Style2,
        Style3;
        
        @Override
        public Text getDisplayName() {
            int style = Integer.parseInt(name().substring(name().length() - 1));
            var a = style <= 2 ? "§7+" : "";
            var b = style != 1 ? "⊰" : "";
            var c = style != 1 ? "⊱" : "";
            return Text.literal("§5" + b + (char) ('\u2793' - 5) + a + "§e\u2793" + c);
        }
    }
    
    public enum Themes implements NameableEnum {
        Custom,
        Special,
        Divine,
        Mythic,
        Legendary,
        Epic,
        Rare,
        Uncommon,
        Common;
        
        @Override
        public Text getDisplayName() {
            return Text.literal((name().equals("Custom")) ? "§nCustom" : getColorCode(name().toLowerCase()) + name());
        }
    }
    
    public enum BackgroundStyle implements NameableEnum {
        Opaque,
        Half_Opaque,
        Transparent;
        
        @Override
        public Text getDisplayName() {
            return Text.literal(name().replace("_", " "));
        }
    }
    
    public Screen config(Screen parent) {
        var randomLevel = Math.round(15 + Math.random() * 75);
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Skylens"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal(((LocalDate.now().getMonthValue() == 4 && LocalDate.now().getDayOfMonth() == 1) ? "Skibidi" : "Sky") + "lens"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Compact Level Display"))
                                .description(OptionDescription.of(Text.literal("Shortens pet level display on tooltip.\nExamples:\n§7[Lvl " + randomLevel + "] §6Pet §f→ §8[§7" + randomLevel + "§8] §6Pet\n§7[Lvl 100] §6Pet §f→ §8[§6100§8] §6Pet")))
                                .binding(
                                        true,
                                        () -> compactLevel,
                                        newValue -> compactLevel = newValue
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                        .coloured(true))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Custom Pet Menu"))
                                .description(OptionDescription.of(Text.literal("Modern style pet menu, that show pet's progress to next level and their rarity.")))
                                .binding(
                                        true,
                                        () -> customPetMenu,
                                        newValue -> customPetMenu = newValue
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                        .coloured(true))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Only in Skyblock"))
                                .binding(
                                        true,
                                        () -> onlySkyblock,
                                        newValue -> onlySkyblock = newValue
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                        .coloured(true))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Show Errors"))
                                .description(OptionDescription.of(Text.literal("Will show thrown exceptions, helps to determine the cause of problems.")))
                                .binding(
                                        false,
                                        () -> showErrors,
                                        newValue -> showErrors = newValue
                                )
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                        .coloured(true))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Pet Overlay"))
                                .description(OptionDescription.of(Text.literal("Displays the progress to max level and next level of current pet.")))
                                .collapsed(true)
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable"))
                                        .binding(
                                                true,
                                                () -> petOverlay,
                                                newValue -> petOverlay = newValue
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                                .coloured(true))
                                        .build())
                                .option(ButtonOption.createBuilder()
                                        .name(Text.literal("Open HUD Editor"))
                                        .text(Text.literal("→"))
                                        .action((yaclScreen, thisOption) -> openScreen(MinecraftClient.getInstance().currentScreen, true))
                                        .build())
                                .option(LabelOption.create(Text.literal("Themes")))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Level Progress Color"))
                                        .binding(Color.WHITE,
                                                () -> petOverlayColor1,
                                                newValue -> petOverlayColor1 = newValue)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("XP Progress Color"))
                                        .binding(Color.GRAY,
                                                () -> petOverlayColor2,
                                                newValue -> petOverlayColor2 = newValue)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Background Color"))
                                        .binding(Color.DARK_GRAY,
                                                () -> petOverlayColor3,
                                                newValue -> petOverlayColor3 = newValue)
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(LabelOption.create(Text.literal("Animation")))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("XP/Level Change"))
                                        .binding(
                                                true,
                                                () -> petOverlayAnimXP,
                                                newValue -> petOverlayAnimXP = newValue
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                                .coloured(true))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Fade"))
                                        .binding(
                                                true,
                                                () -> petOverlayAnimFade,
                                                newValue -> petOverlayAnimFade = newValue
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                                .coloured(true))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Idle"))
                                        .binding(
                                                true,
                                                () -> petOverlayAnimIdle,
                                                newValue -> petOverlayAnimIdle = newValue
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                                .coloured(true))
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Enhanced Noteblock Sounds"))
                                .description(OptionDescription.of(Text.literal("Replaces the instrument sounds to sound more refined. Setting an instrument volume to §e0%§f will §cdisable§f it and the vanilla counterpart will be played instead.")))
                                .collapsed(true)
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable"))
                                        .binding(
                                                true,
                                                () -> enhancedNoteblockSounds,
                                                newValue -> enhancedNoteblockSounds = newValue
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                                .coloured(true))
                                        .build())
                                .option(LabelOption.create(Text.literal("Channel Volumes")))
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Master"))
                                        .binding(
                                                1F,
                                                () -> noteblockGeneralVolume,
                                                newValue -> noteblockGeneralVolume = newValue
                                        )
                                        .controller((opt) -> FloatSliderControllerBuilder.create(opt)
                                                .range(0F, 1F)
                                                .step(0.005F)
                                                .formatValue(ModConfig::volumeFormattedValue))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Instrument: Bass"))
                                        .binding(
                                                0.333F,
                                                () -> noteblockBassVolume,
                                                newValue -> noteblockBassVolume = newValue
                                        )
                                        .controller(ModConfig::volumeController)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Instrument: Kick"))
                                        .binding(
                                                1F,
                                                () -> noteblockBasedrumVolume,
                                                newValue -> noteblockBasedrumVolume = newValue
                                        )
                                        .controller(ModConfig::volumeController)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Instrument: Hit-hat"))
                                        .binding(
                                                1F,
                                                () -> noteblockHatVolume,
                                                newValue -> noteblockHatVolume = newValue
                                        )
                                        .controller(ModConfig::volumeController)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Instrument: Snare"))
                                        .binding(
                                                1F,
                                                () -> noteblockSnareVolume,
                                                newValue -> noteblockSnareVolume = newValue
                                        )
                                        .controller(ModConfig::volumeController)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Missing Enchants"))
                                .description(OptionDescription.of(Text.literal("Shows a list of missing enchantments on items.")))
                                .collapsed(true)
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable"))
                                        .binding(
                                                true,
                                                () -> missingEnchants,
                                                newValue -> missingEnchants = newValue
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                                .coloured(true))
                                        .build())
                                .option(LabelOption.create(Text.literal("Appearance")))
                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Color when active"))
                                        .binding(new Color(85, 255, 255),
                                                () -> missingEnchantsEnabled,
                                                newValue -> missingEnchantsEnabled = newValue)
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Color when inactive"))
                                        .binding(new Color(0, 170, 170),
                                                () -> missingEnchantsDisabled,
                                                newValue -> missingEnchantsDisabled = newValue)
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Potato Books"))
                                .description(OptionDescription.of(Text.literal("Displays how many potato books the hovered item has missing.")))
                                .collapsed(true)
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable"))
                                        .binding(
                                                true,
                                                () -> missingPotatoBooks,
                                                newValue -> missingPotatoBooks = newValue
                                        )
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                                                .coloured(true))
                                        .build())
                                .option(LabelOption.create(Text.literal("Appearance")))
                                .option(Option.<PotatoBookStyles>createBuilder()
                                        .name(Text.literal("Style"))
                                        .binding(PotatoBookStyles.Style1,
                                                () -> PotatoBookStyles.valueOf(missingPotatoBooksStyle),
                                                newValue -> missingPotatoBooksStyle = String.valueOf(newValue))
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(PotatoBookStyles.class))
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.literal("Color"))
                                        .binding(new Color(252, 168, 0),
                                                () -> missingPotatoBooksColor,
                                                newValue -> missingPotatoBooksColor = newValue)
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .save(this::update)
                .build()
                .generateScreen(parent);
    }
    
    public void update() {
        ModConfig.HANDLER.save();
    }
    
    public static ModConfig get() {
        return HANDLER.instance();
    }
    
    public static void openConfig() {
        final boolean[] open = {false};
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!open[0]) {
                open[0] = true;
                client.setScreen(new ModConfig().config(null));
            }
        });
    }
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::config;
    }
    
    public void init() {
        ModConfig.HANDLER.load();
    }
}
