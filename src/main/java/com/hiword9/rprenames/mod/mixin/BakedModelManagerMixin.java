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
    @Inject(at = @At("RETURN"), method = "reload")
    private static void afterReloadModels(
            CallbackInfoReturnable<CompletableFuture<Void>> cir,
            @Local(ordinal = 4) CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> itemAssetsLoaderResult
    ) {
        var itemAssets = itemAssetsLoaderResult.join().contents();
        RPRenames.itemModelParser.updateItemAssets(itemAssets);
    }
}
