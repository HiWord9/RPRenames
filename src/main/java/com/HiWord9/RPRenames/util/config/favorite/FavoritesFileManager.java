package com.HiWord9.RPRenames.util.config.favorite;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesFileManager {
    private final Path configPathFavorite;

    public FavoritesFileManager(Path configPathFavorite) {
        this.configPathFavorite = configPathFavorite;
    }

    public Map<Item, List<AbstractRename>> getAllSavedFavorites() {
        Map<Item, List<AbstractRename>> favoriteRenames = new HashMap<>();
        File[] files = configPathFavorite.toFile().listFiles();
        if (files == null) return favoriteRenames;
        for (File file : files) {
            Item item = itemFromFavoriteFileName(file.getName());
            favoriteRenames.put(item, savedFavorites(item));
        }
        return favoriteRenames;
    }

    public void setFavorites(List<AbstractRename> renames, Item item) {
        if (!renames.isEmpty()) {
            writeFavoriteFile(renames, item);
        } else {
            deleteFavoriteConfigFile(item);
        }
    }

    public List<AbstractRename> savedFavorites(Item item) {
        List<AbstractRename> renames = null;
        File favoritesFile = new File(itemToFavoriteFile(item));
        if (favoritesFile.exists()) {
            renames = readFavoriteFile(favoritesFile);
            for (AbstractRename r : renames) {
                if (r.getItems().isEmpty()) r.getItems().add(item);
            }
        }
        return renames == null ? new ArrayList<>() : renames;
    }

    private List<AbstractRename> readFavoriteFile(File file) {
        ArrayList<AbstractRename> renames = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(file);
            Type type = new TypeToken<ArrayList<AbstractRename>>(){}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(AbstractRename.class, new FavoriteRenameSerializer())
                    .create();
            renames = gson.fromJson(fileReader, type);
            fileReader.close();
        } catch (Exception e) {
            RPRenames.LOGGER.error("Could not read Favorites from file {}", file, e);
        }
        return renames;
    }

    private void writeFavoriteFile(List<AbstractRename> renames, Item item) {
        try {
            if (configPathFavorite.toFile().mkdirs()) {
                RPRenames.LOGGER.info("Created folder for favorites config: {}", configPathFavorite);
            }
            File file = new File(itemToFavoriteFile(item));
            if (!file.exists()) {
                RPRenames.LOGGER.info("Creating new file for favorites config: {}", itemToFavoriteFile(item));
            }
            FileWriter fileWriter = new FileWriter(file);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(AbstractRename.class, new FavoriteRenameSerializer())
                    .create();
            gson.toJson(renames, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            RPRenames.LOGGER.error("Could not write Favorites for {}", item, e);
        }
    }

    private void deleteFavoriteConfigFile(Item item) {
        Path path = Path.of(itemToFavoriteFile(item));
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            RPRenames.LOGGER.error("Could not delete file for Favorites {}", path, e);
        }
    }

    protected void doomConfigs(List<Item> items) {
        items.forEach(this::deleteFavoriteConfigFile);
    }

    private static Item itemFromFavoriteFileName(String fileName) {
        if (!fileName.endsWith(".json")) return Items.AIR;
        String itemFromFileName = fileName.substring(0, fileName.length() - 5).replace(".", ":");
        return ParserHelper.itemFromName(itemFromFileName);
    }

    private String itemToFavoriteFile(Item item) {
        return configPathFavorite + File.separator + fileNameFromItem(item);
    }

    private static String fileNameFromItem(Item item) {
        return ParserHelper.idFromItem(item).replace(":", ".") + ".json";
    }
}
