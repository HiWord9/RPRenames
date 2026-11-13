package com.hiword9.rprenames.mod.mixin;

import com.hiword9.rprenames.mod.RPRenames;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ModelManager.class)
public class BakedModelManagerMixin {
    // Must not block: reload() runs on the render thread while listeners are still being started,
    // and the item assets future may depend on listeners that start later (e.g. Fabric model loading
    // plugins waiting on another listener's shared state) - joining here deadlocks the game on startup.
    @Inject(at = @At("RETURN"), method = "reload")
    private static void afterReloadModels(
            CallbackInfoReturnable<CompletableFuture<Void>> cir,
            @Local(ordinal = 4) CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> itemAssetsLoaderResult
    ) {
        RPRenames.itemModelParser.setItemAssetsFuture(
                itemAssetsLoaderResult.thenAccept(result -> RPRenames.itemModelParser.updateItemAssets(result.contents()))
        );
    }
}
