package com.hiword9.rprenames.loader.fabric.config;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.config.ModConfigScreenFactory;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.util.NullScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return ModConfigScreenFactory::create;
        }
        RPRenames.LOGGER.info("YetAnotherConfigLib is not installed! No configuration Screen for RPRenames available");
        return new NullScreenFactory<>();
    }
}
