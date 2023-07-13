package com.HiWord9.RPRenames;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

public class RPRenames implements ClientModInitializer {
	public static final String MOD_ID = "rprenames";
	public static final Logger LOGGER = LoggerFactory.getLogger("rprenames");

	public static String configPath = "config/renames/";
	public static String configPathClient = configPath + "client/";
	public static String configPathServer = configPath + "server/";
	public static String configPathNameCIT = "all";
	public static String configPathNameCEM = "models";
	public static String configPathFavorite = configPath + "favorite/";
	public static File configClientCITFolder = new File(configPathClient + configPathNameCIT);
	public static File configClientCEMFolder = new File(configPathClient + configPathNameCEM);
	public static File configServerCITFolder = new File(configPathServer + configPathNameCIT);
	public static File configServerCEMFolder = new File(configPathServer + configPathNameCEM);
	public static File configFolderMain = new File(RPRenames.configPath);
	public static File configClientFolder = new File(RPRenames.configPathClient);
	public static File configServerFolder = new File(RPRenames.configPathServer);

	public static URL serverResourcePackURL = null;

	public static final File MOD_CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "rprenames.json");

	private static ConfigHolder<ModConfig> configHolder;

	@Override
	public void onInitializeClient() {
		LOGGER.info("RPRenames author like coca-cola zero, but don't tell anyone");
		configHolder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		configHolder.registerSaveListener((manager, data) -> ActionResult.SUCCESS);
		ModConfig config = ModConfig.INSTANCE;
		if (config.loadModBuiltinResources) {
			LOGGER.info("Loading RPRenames built-in resource packs");
			FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
				ResourceManagerHelper.registerBuiltinResourcePack(asId("vanillish"), container, ResourcePackActivationType.NORMAL);
				ResourceManagerHelper.registerBuiltinResourcePack(asId("default_dark_mode"), container, ResourcePackActivationType.NORMAL);
			});
		}
	}

	public static Identifier asId(String path) {
		return new Identifier(MOD_ID, path);
	}
}
