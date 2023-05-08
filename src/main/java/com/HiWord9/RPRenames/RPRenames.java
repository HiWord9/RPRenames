package com.HiWord9.RPRenames;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.File;

public class RPRenames implements ClientModInitializer {
	public static final String MOD_ID = "rprenames";

	public static String configPath = "config/renames/all/";
	public static String configPathFavorite = "config/renames/favorite/";
	public static String configPathModels = "config/renames/models/";
	public static String settingsPath = "config/rprenames.properties";
	public static File configFolder = new File(configPath);
	public static File configFolderModels = new File(configPathModels);

	@Override
	public void onInitializeClient() {
		FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
			ResourceManagerHelper.registerBuiltinResourcePack(asId("vanillish"), container, ResourcePackActivationType.NORMAL);
			ResourceManagerHelper.registerBuiltinResourcePack(asId("default_dark_mode"), container, ResourcePackActivationType.NORMAL);
		});
	}

	public static Identifier asId(String path) {
		return new Identifier(MOD_ID, path);
	}
}
