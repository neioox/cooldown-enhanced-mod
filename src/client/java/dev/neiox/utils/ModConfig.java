package dev.neiox.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.neiox.enums.settings.SettingOptions;
import me.shedaniel.math.Color;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static ModConfig instance;

    private static  Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static  Path CONFIG_PATH = Path.of("config", "cooldown_enhanced_settings.json");

    private SettingOptions.CooldownDisplayMode cooldownDisplayMode = SettingOptions.CooldownDisplayMode.DEFAULT;
    private SettingOptions.CooldownNumericMode cooldownNumericMode = SettingOptions.CooldownNumericMode.PERCENTAGE;
    private boolean audioNotification = false;
    private String notificationSoundId = "minecraft:block.note_block.bell";
    private int barColor =  ARGB.color(255, 0, 255, 0);
    private int attackIndicatorScale = 1;
    private boolean modernBarStyle = false;

    private ModConfig() {}

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static ModConfig load() {
        try {
            if (Files.notExists(CONFIG_PATH)) {
                ModConfig config = new ModConfig();
                config.save();
                return config;
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config == null) config = new ModConfig();
                config.save(); // Update with defaults if new fields were added
                return config;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }

    // Accessors

    public SettingOptions.CooldownDisplayMode getCooldownDisplayMode() {
        return cooldownDisplayMode;
    }

    public void setCooldownDisplayMode(SettingOptions.CooldownDisplayMode mode) {
        this.cooldownDisplayMode = mode;
        save();
    }

    public SettingOptions.CooldownNumericMode getCooldownNumericMode() {
        return cooldownNumericMode;
    }

    public void setCooldownNumericMode(SettingOptions.CooldownNumericMode mode) {
        this.cooldownNumericMode = mode;
        save();
    }

    public boolean isAudioNotificationEnabled() {
        return audioNotification;
    }

    public void setAudioNotification(boolean enabled) {
        this.audioNotification = enabled;
        save();
    }

    public SoundEvent getNotificationSound() {
        try {
            Identifier identifier = Identifier.parse(notificationSoundId);
            return BuiltInRegistries.SOUND_EVENT.getOptional(identifier)
                    .orElse(SoundEvents.NOTE_BLOCK_BELL.value());
        } catch (Exception e) {
            return SoundEvents.NOTE_BLOCK_BELL.value();
        }
    }

    public int getBarColor() {
        return barColor;
    }

    public void setBarColor(int a, int r, int g, int b) {
        this.barColor = ARGB.color(a, r, g, b);
        save();
    }

    public void setNotificationSound(SoundEvent sound) {
        Identifier identifier = BuiltInRegistries.SOUND_EVENT.getKey(sound);
        if (identifier != null) {
            this.notificationSoundId = identifier.toString();
        }
        save();
    }

    public int getAttackIndicatorScale() {
        return attackIndicatorScale;
    }

    public void setAttackIndicatorScale(int scale) {
        this.attackIndicatorScale = scale;
        save();
    }

    public boolean getModernBarStyle() {
        return modernBarStyle;
    }
    public void setModernBarStyle(boolean modernBarStyle) {
        this.modernBarStyle = modernBarStyle;
        save();
    }

    public void readSettings() {
        instance = load();
    }
}
