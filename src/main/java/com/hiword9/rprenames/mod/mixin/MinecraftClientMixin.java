package com.hiword9.rprenames.mod.mixin;

import com.hiword9.rprenames.mod.RPRenames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Shadow @Final private ReloadableResourceManager resourceManager;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;",
                    ordinal = 0
            ),
            method = "<init>"
    )
    private void onInit(GameConfig args, CallbackInfo ci) {
        // onInitializeClient() executed before resourceManager is initialized, so we register reloader in mixin
        resourceManager.registerReloadListener(RPRenames.updatableRenamesManager);
    }
}
