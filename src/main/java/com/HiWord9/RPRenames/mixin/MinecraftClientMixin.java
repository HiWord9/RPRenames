package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.configGeneration.ConfigManager;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    private static final ModConfig config = ModConfig.INSTANCE;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"), method = "disconnect*")
    private void serverResourcePackConfigDeleter(CallbackInfo ci){
        if (config.updateConfig) {
            if (new File(RPRenames.configPathServer).exists()) {
                ConfigManager.configDelete(RPRenames.configPathServer);
            }
            RPRenames.serverResourcePackURL = null;
        }
    }

    @Inject(method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private void onReloadResources(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (config.updateConfig) {
            RPRenames.LOGGER.info("Starting recreating RPR Config");
            ConfigManager.configUpdate();
        }
    }
}
