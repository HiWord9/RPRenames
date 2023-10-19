package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin {
    private static final ModConfig config = ModConfig.INSTANCE;

    @Inject(method = "reload", at = @At("RETURN"))
    public void onReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs, CallbackInfoReturnable<ResourceReload> cir) {
        if (config.updateConfig) {
            if (RPRenames.joiningServer) {
                RPRenames.LOGGER.info("Starting recreating RPR Config for Server");
                new Thread(ConfigManager::configUpdateServer).start();
                RPRenames.joiningServer = false;
            } else if (RPRenames.leavingServer) {
                RPRenames.leavingServer = false;
            } else {
                RPRenames.LOGGER.info("Starting recreating RPR Config");
                new Thread(ConfigManager::configUpdate).start();
            }
        }
    }
}
