package dev.neiox.mixin.client;

import dev.neiox.enums.settings.SettingOptions;
import dev.neiox.utils.ModConfig;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Gui.class)
public class GuiMixin {
    private static final Identifier[] OVERLAY_PROGRESS_SPRITES = new Identifier[]{Identifier.withDefaultNamespace("boss_bar/notched_6_progress"), Identifier.withDefaultNamespace("boss_bar/notched_10_progress"), Identifier.withDefaultNamespace("boss_bar/notched_12_progress"), Identifier.withDefaultNamespace("boss_bar/notched_20_progress")};
    @Unique
    boolean wasOnCooldown = false;
    @Unique
    ModConfig settings = ModConfig.getInstance();

    @Unique
    private void renderCooldownNumericMode(GuiGraphics guiGraphics, Minecraft minecaft, String text) {
        int screenWidth = minecaft.getWindow().getGuiScaledWidth();
        int screenHeight = minecaft.getWindow().getGuiScaledHeight() + 40;

        int textWidth = minecaft.font.width(text);
        int textHeight = minecaft.font.lineHeight;

        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight - textHeight) / 2;
        guiGraphics.drawString(minecaft.font, text, x, y, 0xFFFFFFFF, true);
    }

    @Unique
    private void renderCooldownBarMode(GuiGraphics guiGraphics, Minecraft minecraft, float attackStrengthScale) {
        int scale = Math.max(1, settings.getAttackIndicatorScale());

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int baseBarWidth = screenWidth * 2 / 5 - screenWidth / 3;
        int barWidth = baseBarWidth * scale;
        int barHeight = 4 * scale;
        ;

        int x1 = (screenWidth - barWidth) / 2;
        int y1 = screenHeight / 2 + 20;

        int x2 = x1 + barWidth;
        int y2 = y1 + barHeight;

        drawBar(guiGraphics, x1, y1, x2, y2, attackStrengthScale, settings.getModernBarStyle());
    }


    @Unique
    private void drawBar(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, float progress, boolean modernBar) {
        int width = x2 - x1;
        progress = Mth.clamp(progress, 0.0F, 1.0F);
        int textureWidth = 182;
        int textureHeight = 5;
        int progressWidth = (int) (width * progress);

        if (modernBar) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath("cooldown-enhanced", "hud/cooldown_background"), x1, y1, width, textureHeight);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath("cooldown-enhanced", "hud/cooldown_progress"), x1, y1, progressWidth, textureHeight);
            return;
        }

        guiGraphics.fill(x1, y1, x2, y2, ARGB.color(100, 0, 0, 0));
        guiGraphics.fill(x1, y1, x1 + progressWidth, y2, settings.getBarColor());
    }


    @Redirect(
            method = "renderCrosshair",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;")
    )
    private Object cancelAttackIndicatorCheck(OptionInstance instance) {
        if (settings.getCooldownDisplayMode() != SettingOptions.CooldownDisplayMode.DEFAULT) {
            return AttackIndicatorStatus.OFF;
        }
        return instance.get();
    }
        @Inject(at = @At("HEAD"), method = "render")
    private void onRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        float attackStrengthScale = player.getAttackStrengthScale(0.0F);

        if (attackStrengthScale < 1.0F) {
            wasOnCooldown = true;

            if (settings.getCooldownDisplayMode() == dev.neiox.enums.settings.SettingOptions.CooldownDisplayMode.BAR) {
                renderCooldownBarMode(guiGraphics, minecraft, attackStrengthScale);
                return;
            }else if (settings.getCooldownDisplayMode() == SettingOptions.CooldownDisplayMode.DEFAULT) {
                return;
            }

            String text;
            switch (ModConfig.getInstance().getCooldownNumericMode()) {
                case DECIMAL -> text = String.format("%.2f", attackStrengthScale);
                case PERCENTAGE -> {
                    int cooldownPercentage = (int) (attackStrengthScale * 100);
                    text = String.format("%d%%", cooldownPercentage);
                }
                default -> text = "";
            }
            renderCooldownNumericMode(guiGraphics, minecraft, text);
        } else {
            if (wasOnCooldown) {

                if (settings.getCooldownDisplayMode() == dev.neiox.enums.settings.SettingOptions.CooldownDisplayMode.BAR) {
                    renderCooldownBarMode(guiGraphics, minecraft, 1.0F);
                }

                if(settings.getCooldownDisplayMode() == dev.neiox.enums.settings.SettingOptions.CooldownDisplayMode.NUMERIC) {

                    if (settings.getCooldownNumericMode() == dev.neiox.enums.settings.SettingOptions.CooldownNumericMode.DECIMAL) {
                        renderCooldownNumericMode(guiGraphics, minecraft, "1.00");
                    } else if (settings.getCooldownNumericMode() == dev.neiox.enums.settings.SettingOptions.CooldownNumericMode.PERCENTAGE) {
                        renderCooldownNumericMode(guiGraphics, minecraft, "100%");
                    }
                }
                if (settings.isAudioNotificationEnabled()) {
                    player.playSound(settings.getNotificationSound(), 0.5F, 1.0F);
                }

                wasOnCooldown = false;
            }
        }
    }
}
