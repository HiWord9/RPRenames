package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.util.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @Inject(method = "<init>", at =
    @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"))
    private void rpr$loadRenames(BlockColors blockColors, Profiler profiler, Map jsonUnbakedModels, Map blockStates, CallbackInfo ci) {
        profiler.push("rprenames:reloading_renames");
        ConfigManager.parseRenames(MinecraftClient.getInstance().getResourceManager(), profiler);
        profiler.pop();
    }
}