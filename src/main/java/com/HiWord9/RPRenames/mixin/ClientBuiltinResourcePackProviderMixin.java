package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;
import java.util.concurrent.CompletableFuture;

@Mixin(ClientBuiltinResourcePackProvider.class)
public abstract class ClientBuiltinResourcePackProviderMixin {

    @Inject(at = @At("RETURN"), method = "download")
    private void serverResourcePackConfigCreator(URL url, String packSha1, boolean closeAfterDownload, CallbackInfoReturnable<CompletableFuture<?>> cir){
        RPRenames.serverResourcePackURL = url;
    }
}