package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    private static final ModConfig config = ModConfig.INSTANCE;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"), method = "disconnect*")
    private void serverResourcePackConfigDeleter(CallbackInfo ci) {
        if (config.updateConfig) {
            RPRenames.renamesServer.clear();
            if (RPRenames.serverResourcePackURL != null) {
                RPRenames.serverResourcePackURL = null;
                RPRenames.leavingServer = true;
            }
        }
    }
}
