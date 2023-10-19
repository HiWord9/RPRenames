package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import net.minecraft.client.resource.ServerResourcePackProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerResourcePackProvider.class)
public abstract class ServerResourcePackProviderMixin {
    private static final ModConfig config = ModConfig.INSTANCE;

    @Inject(at = @At("RETURN"), method = "download")
    private void serverResourcePackConfigCreator(URL url, String packSha1, boolean closeAfterDownload, CallbackInfoReturnable<CompletableFuture<?>> cir) {
        if (config.updateConfig) {
            RPRenames.serverResourcePackURL = url;
            RPRenames.joiningServer = true;
        }
    }
}