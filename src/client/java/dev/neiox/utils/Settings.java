package dev.neiox.utils;

import dev.neiox.enums.settings.SettingOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class Settings {

    private static Settings instance;

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    private SettingOptions.CooldownDisplayMode cooldownDisplayMode = SettingOptions.CooldownDisplayMode.NUMERIC;
    private SettingOptions.CooldownNumericMode cooldownNumericMode = SettingOptions.CooldownNumericMode.PERCENTAGE;
    private boolean audioNotification = false;
    private SoundEvent notificationSound = SoundEvents.NOTE_BLOCK_BELL.value();


   public SettingOptions.CooldownDisplayMode getCooldownDisplayMode() {
        return cooldownDisplayMode;
    }

    public void setCooldownDisplayMode(SettingOptions.CooldownDisplayMode mode) {
        this.cooldownDisplayMode = mode;
    }

    public SettingOptions.CooldownNumericMode getCooldownNumericMode() {
        return cooldownNumericMode;
    }

    public void setCooldownNumericMode(SettingOptions.CooldownNumericMode mode) {
        this.cooldownNumericMode = mode;
    }

    public boolean isAudioNotificationEnabled() {
        return audioNotification;
    }
    public void setAudioNotification(boolean enabled) {
        this.audioNotification = enabled;
    }

    public SoundEvent getNotificationSound() {
        return notificationSound;
    }
    public void setNotificationSound(SoundEvent sound) {
        this.notificationSound = sound;
    }
}
