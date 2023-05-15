package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.configGeneration.ConfigManager;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.resource.ResourcePackProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ResourcePackOrganizer.class)
public class ResourcePackOrganizerMixin {

    @Shadow @Final
    List<ResourcePackProfile> enabledPacks;

    ModConfig config = ModConfig.INSTANCE;

    @Inject(at = @At("HEAD"), method = "apply")
    private void abc(CallbackInfo ci) {
        if (config.createConfig) {
            ConfigManager.configUpdate(enabledPacks);
        }
    }
}
