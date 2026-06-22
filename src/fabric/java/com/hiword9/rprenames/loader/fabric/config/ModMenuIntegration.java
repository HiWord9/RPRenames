package com.hiword9.rprenames.loader.fabric.config;

import com.hiword9.rprenames.mod.config.ModConfigScreenFactory;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.util.NullScreenFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    // Own logger: ModMenu may query this factory before the client initializer runs,
    // so referencing RPRenames here would trigger its static init before the config
    // directory is set and crash. Keep this path free of RPRenames class loading.
    private static final Logger LOGGER = LoggerFactory.getLogger("rprenames");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return ModConfigScreenFactory::create;
        }
        LOGGER.info("YetAnotherConfigLib is not installed! No configuration Screen for RPRenames available");
        return new NullScreenFactory<>();
    }
}
