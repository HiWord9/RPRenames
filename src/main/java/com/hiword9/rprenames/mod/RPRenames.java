package com.hiword9.rprenames.mod;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.ext.renames_manager.CompositeRenamesProvider;
import com.hiword9.rprenames.mod.impl.renames_manager.favorite.FavoritesFileManager;
import com.hiword9.rprenames.mod.impl.renames_manager.favorite.FavoritesManager;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.cem.CEMParser;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.CITParser;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.ItemModelParser;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.UpdatableRenamesManager;
import com.hiword9.rprenames.mod.item_group.RPRenamesItemGroup;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

import static com.hiword9.rprenames.mod.util.Util.*;

public class RPRenames implements ClientModInitializer {
    public static final String MOD_ID = "rprenames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    public static final Path configPathFavorite = Path.of(configPath + "/favorite");

    public static final File MOD_CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "rprenames.json");

    public static final CompositeRenamesProvider<Rename> renamesProvider = new CompositeRenamesProvider<>();

    public static final UpdatableRenamesManager updatableRenamesManager = new UpdatableRenamesManager();
    public static final ItemModelParser itemModelParser = new ItemModelParser(updatableRenamesManager);
    public static final CITParser citParser = new CITParser(updatableRenamesManager);
    public static final CEMParser cemParser = new CEMParser(updatableRenamesManager);

    public static final FavoritesManager favoritesManager = new FavoritesManager(new FavoritesFileManager(RPRenames.configPathFavorite));

    @Override
    public void onInitializeClient() {
        LOGGER.info("RPRenames author like coca-cola zero, but don't tell anyone");
        ClientCommandRegistrationCallback.EVENT.register(RPRenames::registerCommand);
        if (config().loadModBuiltinResources) {
            LOGGER.info("Loading RPRenames built-in resource packs");
            FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
                for (String pack : new String[]{"vanillish", "default_dark_mode", "high_contrasted"}) {
                    ResourceManagerHelper.registerBuiltinResourcePack(
                            asId(pack),
                            container,
                            Text.translatable("rprenames.builtinResourcePack." + pack),
                            ResourcePackActivationType.NORMAL
                    );
                }
            });
        }

        registerItemGroup();

        renamesProvider.providers.add(updatableRenamesManager);

        updatableRenamesManager.parsers().add(itemModelParser);
        updatableRenamesManager.parsers().add(citParser);
        updatableRenamesManager.parsers().add(cemParser);

        favoritesManager.loadSavedFavorites();
    }

    public static Identifier asId(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        RPRenamesCommand.register(dispatcher, registryAccess);
    }

    public static void registerItemGroup() {
        RPRenamesItemGroup.register();
    }
}
