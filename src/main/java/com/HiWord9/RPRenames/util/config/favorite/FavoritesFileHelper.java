package com.HiWord9.RPRenames.util.config.favorite;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.rename.RenameSerializer;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoritesFileHelper {
    static Path configPathFavorite = RPRenames.configPathFavorite;

    public static Map<Item, ArrayList<AbstractRename>> getAllSavedFavorites() {
        Map<Item, ArrayList<AbstractRename>> favoriteRenames = new HashMap<>();
        File[] files = configPathFavorite.toFile().listFiles();
        if (files == null) return favoriteRenames;
        for (File file : files) {
            Item item = itemFromFavoriteFileName(file.getName());
            favoriteRenames.put(item, savedFavorites(item));
        }
        return favoriteRenames;
    }

    public static void setFavorites(ArrayList<AbstractRename> renames, Item item) {
        if (!renames.isEmpty()) {
            writeFavoriteFile(renames, item);
        } else {
            deleteFavoriteConfigFile(item);
        }
    }

    public static ArrayList<AbstractRename> savedFavorites(Item item) {
        ArrayList<AbstractRename> renames = new ArrayList<>();
        File favoritesFile = new File(pathToFavoriteFile(item));
        if (favoritesFile.exists()) {
            renames = readFavoriteFile(favoritesFile);
            for (AbstractRename r : renames) {
                if (r.getItem() == null) r.setItem(item);
            }
        }
        return renames;
    }

    private static ArrayList<AbstractRename> readFavoriteFile(File file) {
        ArrayList<AbstractRename> renames = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(file);
            Type type = new TypeToken<ArrayList<AbstractRename>>(){}.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(AbstractRename.class, new RenameSerializer())
                    .create();
            renames = gson.fromJson(fileReader, type);
            fileReader.close();
        } catch (Exception e) {
            RPRenames.LOGGER.error("Could not read Favorites from file {}", file, e);
        }
        return renames;
    }

    private static void writeFavoriteFile(ArrayList<AbstractRename> renames, Item item) {
        try {
            if (configPathFavorite.toFile().mkdirs()) {
                RPRenames.LOGGER.info("Created folder for favorites config: {}", configPathFavorite);
            }
            File file = new File(pathToFavoriteFile(item));
            if (!file.exists()) {
                RPRenames.LOGGER.info("Creating new file for favorites config: {}", pathToFavoriteFile(item));
            }
            FileWriter fileWriter = new FileWriter(file);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(AbstractRename.class, new RenameSerializer())
                    .create();
            gson.toJson(renames, fileWriter);
            fileWriter.close();
        } catch (Exception e) {
            RPRenames.LOGGER.error("Could not write Favorites for {}", item, e);
        }
    }

    private static void deleteFavoriteConfigFile(Item item) {
        Path path = Path.of(pathToFavoriteFile(item));
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            RPRenames.LOGGER.error("Could not delete file for Favorites {}", path, e);
        }
    }

    private static Item itemFromFavoriteFileName(String fileName) {
        if (!fileName.endsWith(".json")) return Items.AIR;
        String itemFromFileName = fileName.substring(0, fileName.length() - 5).replace(".", ":");
        return ParserHelper.itemFromName(itemFromFileName);
    }

    private static String pathToFavoriteFile(Item item) {
        return configPathFavorite + File.separator + fileNameFromItem(item);
    }

    private static String fileNameFromItem(Item item) {
        return ParserHelper.idFromItem(item).replace(":", ".") + ".json";
    }
}
