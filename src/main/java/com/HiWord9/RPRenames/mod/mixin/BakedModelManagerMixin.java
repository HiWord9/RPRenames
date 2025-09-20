package com.HiWord9.RPRenames.mod.mixin;

import com.HiWord9.RPRenames.mod.RPRenames;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.item.ItemAssetsLoader;
import net.minecraft.client.render.model.BakedModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
    @Inject(at = @At("RETURN"), method = "reload")
    private static void afterReloadModels(
            CallbackInfoReturnable<CompletableFuture<Void>> cir,
            @Local(ordinal = 4) CompletableFuture<ItemAssetsLoader.Result> itemAssetsLoaderResult
    ) {
        var itemAssets = itemAssetsLoaderResult.join().contents();
        RPRenames.itemModelParser.updateItemAssets(itemAssets);
    }
}
