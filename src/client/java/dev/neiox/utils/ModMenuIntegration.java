package dev.neiox.utils;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.neiox.enums.settings.SettingOptions;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;

import java.util.List;
import java.util.stream.Collectors;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> {
            ModConfig config = ModConfig.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Cooldown Enhanced Settings"));

            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General Settings"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // 1. Cooldown Display Mode
            general.addEntry(entryBuilder.startEnumSelector(
                            Component.literal("Cooldown Display Mode"),
                            SettingOptions.CooldownDisplayMode.class,
                            config.getCooldownDisplayMode()
                    )
                    .setDefaultValue(SettingOptions.CooldownDisplayMode.NUMERIC)
                    .setSaveConsumer(config::setCooldownDisplayMode)
                    .build());

            // 2. Numeric Mode
            general.addEntry(entryBuilder.startEnumSelector(
                            Component.literal("Numeric Mode"),
                            SettingOptions.CooldownNumericMode.class,
                            config.getCooldownNumericMode()
                    )
                    .setDefaultValue(SettingOptions.CooldownNumericMode.PERCENTAGE)
                    .setSaveConsumer(config::setCooldownNumericMode)
                    .build());

            // 3. Audio Notification Toggle
            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Enable Audio Notification"),
                            config.isAudioNotificationEnabled()
                    )
                    .setDefaultValue(false)
                    .setSaveConsumer(config::setAudioNotification)
                    .build());

            // 4. Notification Sound (SoundEvent dropdown)
            List<SoundEvent> sounds = BuiltInRegistries.SOUND_EVENT.stream()
                    .sorted((a, b) -> BuiltInRegistries.SOUND_EVENT.getKey(a)
                            .compareTo(BuiltInRegistries.SOUND_EVENT.getKey(b)))
                    .collect(Collectors.toList());

            SoundEvent current = config.getNotificationSound();

            general.addEntry(entryBuilder.startSelector(
                            Component.literal("Notification Sound"),
                            sounds.toArray(new SoundEvent[0]),
                            current
                    )
                    .setDefaultValue(SoundEvent.createVariableRangeEvent(Identifier.parse("minecraft:note_block.bell")))
                    .setNameProvider(sound -> Component.literal(
                            BuiltInRegistries.SOUND_EVENT.getKey(sound).toString()))
                    .setSaveConsumer(config::setNotificationSound)
                    .build());

            // 5. Bar Color (ARGB sliders)
            ConfigCategory colorCat = builder.getOrCreateCategory(Component.literal("Bar Color"));

            int color = config.getBarColor();
            int a = ARGB.alpha(color);
            int r = ARGB.red(color);
            int g = ARGB.green(color);
            int b = ARGB.blue(color);

            final int[] newA = {a};
            final int[] newR = {r};
            final int[] newG = {g};
            final int[] newB = {b};

            colorCat.addEntry(entryBuilder
                    .startIntSlider(Component.literal("Alpha (A)"), a, 0, 255)
                    .setDefaultValue(255)
                    .setSaveConsumer(val -> newA[0] = val)
                    .build());

            colorCat.addEntry(entryBuilder
                    .startIntSlider(Component.literal("Red (R)"), r, 0, 255)
                    .setDefaultValue(0)
                    .setSaveConsumer(val -> newR[0] = val)
                    .build());

            colorCat.addEntry(entryBuilder
                    .startIntSlider(Component.literal("Green (G)"), g, 0, 255)
                    .setDefaultValue(255)
                    .setSaveConsumer(val -> newG[0] = val)
                    .build());

            colorCat.addEntry(entryBuilder
                    .startIntSlider(Component.literal("Blue (B)"), b, 0, 255)
                    .setDefaultValue(0)
                    .setSaveConsumer(val -> newB[0] = val)
                    .build());

            builder.setSavingRunnable(() -> {
                config.setBarColor(newA[0], newR[0], newG[0], newB[0]);
                config.save();
            });

            return builder.build();
        };
    }
}
