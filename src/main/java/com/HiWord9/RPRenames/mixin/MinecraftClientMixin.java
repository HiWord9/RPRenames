package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Final private ReloadableResourceManagerImpl resourceManager;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resource/ReloadableResourceManagerImpl;reload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/resource/ResourceReload;",
                    ordinal = 0
            ),
            method = "<init>"
    )
    private void onInit(RunArgs args, CallbackInfo ci) {
        // onInitializeClient() executed before resourceManager is initialized, so we register reloader in mixin
        resourceManager.registerReloader(RPRenames.renamesManager);
    }
}
