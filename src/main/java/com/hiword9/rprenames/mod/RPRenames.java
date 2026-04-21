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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiConsumer;

public class RPRenames {
    public static final String MOD_ID = "rprenames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Path CONFIG_PATH = Settings.getConfigDir().resolve(MOD_ID);
    public static final Path CONFIG_PATH_FAVORITE = Path.of(CONFIG_PATH + "/favorite");

    public static final File MOD_CONFIG_FILE = new File(Settings.getConfigDir().toFile(), "rprenames.json");

    public static final Identifier RENAMES_RELOADER_ID = Identifier.fromNamespaceAndPath(MOD_ID, "renames_reloader");

    public static final CompositeRenamesProvider<Rename> renamesProvider = new CompositeRenamesProvider<>();

    public static final UpdatableRenamesManager updatableRenamesManager = new UpdatableRenamesManager();
    public static final ItemModelParser itemModelParser = new ItemModelParser(updatableRenamesManager);
    public static final CITParser citParser = new CITParser(updatableRenamesManager);
    public static final CEMParser cemParser = new CEMParser(updatableRenamesManager);

    public static final FavoritesManager favoritesManager = new FavoritesManager(new FavoritesFileManager(RPRenames.CONFIG_PATH_FAVORITE));

    public static void onInit() {
        LOGGER.info("RPRenames author like coca-cola zero, but don't tell anyone");

        renamesProvider.providers.add(updatableRenamesManager);

        updatableRenamesManager.parsers().add(itemModelParser);
        updatableRenamesManager.parsers().add(citParser);
        updatableRenamesManager.parsers().add(cemParser);

        favoritesManager.loadSavedFavorites();
    }

    public static Identifier asId(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @SuppressWarnings("unchecked")
    public static <T extends SharedSuggestionProvider> void registerCommand(CommandDispatcher<T> dispatcher, CommandBuildContext registryAccess) {
        RPRenamesCommand.register((CommandDispatcher<SharedSuggestionProvider>) dispatcher, registryAccess);
    }

    public static void registerItemGroup(BiConsumer<Identifier, CreativeModeTab> registry) {
        RPRenamesItemGroup.register(registry);
    }
}
