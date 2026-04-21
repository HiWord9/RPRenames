package com.hiword9.rprenames.loader.fabric;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.Settings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;

import static com.hiword9.rprenames.mod.RPRenames.*;
import static com.hiword9.rprenames.mod.util.Util.config;

public class RPRenamesFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Settings.setConfigDir(FabricLoader.getInstance().getConfigDir());

        LOGGER.info("Initializing RPRenames Fabric");

        ClientCommandRegistrationCallback.EVENT.register(RPRenames::registerCommand);
        if (config().loadModBuiltinResources) {
            FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
                for (String pack : new String[]{"vanillish", "default_dark_mode", "high_contrasted"}) {
                    ResourceLoader.registerBuiltinPack(
                            asId(pack),
                            container,
                            Component.translatable("rprenames.builtinResourcePack." + pack),
                            PackActivationType.NORMAL
                    );
                }
            });
        }
        registerItemGroup((id, tab)
                -> Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab)
        );

        RPRenames.onInit();

        var resourceLoader = ResourceLoader.get(PackType.CLIENT_RESOURCES);

        resourceLoader.registerReloadListener(RENAMES_RELOADER_ID, updatableRenamesManager);
        resourceLoader.addListenerOrdering(ResourceReloaderKeys.AFTER_VANILLA, RENAMES_RELOADER_ID);
    }
}
