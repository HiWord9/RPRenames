package com.HiWord9.RPRenames.modConfig;

import com.HiWord9.RPRenames.RPRenames;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return ModConfigScreenFactory::create;
        }
        RPRenames.LOGGER.info("Cloth-config is not installed! No configuration Screen for RPRenames available");
        return screen -> null;
    }
}
