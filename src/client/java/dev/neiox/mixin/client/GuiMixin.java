package dev.neiox.mixin.client;

import dev.neiox.utils.Settings;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Unique
    boolean wasOnCooldown = false;

    @Unique
    private void renderCooldownNumericMode(GuiGraphics guiGraphics, Minecraft minecaft, String text){
        int screenWidth = minecaft.getWindow().getGuiScaledWidth();
        int screenHeight = minecaft.getWindow().getGuiScaledHeight() + 40;

        int textWidth = minecaft.font.width(text);
        int textHeight = minecaft.font.lineHeight;

        int x = (screenWidth - textWidth) / 2;
        int y = (screenHeight - textHeight) / 2;
        guiGraphics.drawString(minecaft.font, text, x, y, 0xFFFFFFFF, true);
    }

    @Unique
    Settings settings = Settings.getInstance();

    @Inject(at = @At("HEAD"), method = "render")
    private  void onRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci){

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;
        float attackStrengthScale = player.getAttackStrengthScale(0.0F);
        if(attackStrengthScale < 1.0F){
            wasOnCooldown = true;

            String text = "";

            switch (Settings.getInstance().getCooldownNumericMode()){

                case DECIMAL -> {
                    text = String.format("%.2f", attackStrengthScale);
                }
                case PERCENTAGE -> {
                    int cooldownPercentage = (int) (attackStrengthScale * 100);
                    text = String.format("%d%%", cooldownPercentage);
                }
            }
            renderCooldownNumericMode(guiGraphics, minecraft, text);
        }else{
            if(wasOnCooldown){
                renderCooldownNumericMode(guiGraphics, minecraft, "100%");

                if(settings.isAudioNotificationEnabled()){
                    player.playSound(settings.getNotificationSound(), 0.5F, 1.0F);
                }
                wasOnCooldown = false;
            }
        }
    }
}
