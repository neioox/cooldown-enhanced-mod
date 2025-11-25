package dev.neiox.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.neiox.enums.settings.SettingOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static ModConfig instance;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "cooldown_enhanced_settings.json");

    private SettingOptions.CooldownDisplayMode cooldownDisplayMode = SettingOptions.CooldownDisplayMode.NUMERIC;
    private SettingOptions.CooldownNumericMode cooldownNumericMode = SettingOptions.CooldownNumericMode.PERCENTAGE;
    private boolean audioNotification = false;
    private SoundEvent notificationSound = SoundEvents.NOTE_BLOCK_BELL.value();

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
        return notificationSound;
    }

    public void setNotificationSound(SoundEvent sound) {
        this.notificationSound = sound;
        save();
    }

    public void readSettings() {
        instance = load();
    }
}
