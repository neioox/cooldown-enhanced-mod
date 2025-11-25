package dev.neiox.utils;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.neiox.enums.settings.SettingOptions;
import dev.neiox.utils.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

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
                    .setDefaultValue(SoundEvent.createVariableRangeEvent(ResourceLocation.parse("minecraft:note_block.bell")))
                    .setNameProvider(sound -> Component.literal(
                            BuiltInRegistries.SOUND_EVENT.getKey(sound).toString()))
                    .setSaveConsumer(config::setNotificationSound)
                    .build());

            builder.setSavingRunnable(config::save);
            return builder.build();
        };
    }
}
