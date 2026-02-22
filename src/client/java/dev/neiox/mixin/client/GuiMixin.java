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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE;
    @Shadow
    @Final
    private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE;
    @Shadow
    @Final
    private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE;
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
        int barHeight = 4 * scale;;

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
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ResourceLocation.fromNamespaceAndPath("cooldown-enhanced", "hud/cooldown_background"), x1, y1, width, textureHeight);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ResourceLocation.fromNamespaceAndPath("cooldown-enhanced", "hud/cooldown_progress"), x1, y1, progressWidth, textureHeight);
            return;
        }

        guiGraphics.fill(x1, y1, x2, y2, ARGB.color(100, 0, 0, 0));
        guiGraphics.fill(x1, y1, x1 + progressWidth, y2, settings.getBarColor());
    }
    @ModifyArgs(
            method = "renderCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V"
            )
    )
    private void scaleAttackIndicatorFixed(Args args) {
        net.minecraft.resources.ResourceLocation sprite = (net.minecraft.resources.ResourceLocation) args.get(1);

        if (!sprite.equals(CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE)
                && !sprite.equals(CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE)) {
            return;
        }

        int scale = Math.max(1, settings.getAttackIndicatorScale());
        if (scale == 1) return;

        int x = (int) args.get(2);
        int y = (int) args.get(3);
        int w = (int) args.get(4);
        int h = (int) args.get(5);

        int nw = w * scale;
        int nh = h * scale;

        args.set(2, x - (nw - w) / 2);
        args.set(3, y - (nh - h) / 2);
        args.set(4, nw);
        args.set(5, nh);
    }

    @ModifyArgs(
            method = "renderCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIIIIII)V"
            )
    )
    private void scaleAttackIndicatorProgress(Args args) {
        net.minecraft.resources.ResourceLocation sprite = (net.minecraft.resources.ResourceLocation) args.get(1);
        if (!sprite.equals(CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE)) return;

        int scale = Math.max(1, settings.getAttackIndicatorScale());
        if (scale == 1) return;

        int baseW = (int) args.get(2);
        int baseH = (int) args.get(3);

        int u = (int) args.get(4);
        int v = (int) args.get(5);

        int x = (int) args.get(6);
        int y = (int) args.get(7);

        int w = (int) args.get(8);
        int h = (int) args.get(9);

        int nx = x - (baseW * (scale - 1)) / 2;
        int ny = y - (baseH * (scale - 1)) / 2;

        args.set(2, baseW * scale);
        args.set(3, baseH * scale);

        args.set(4, u * scale);
        args.set(5, v * scale);

        args.set(6, nx);
        args.set(7, ny);
        args.set(8, w * scale);
        args.set(9, h * scale);
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
