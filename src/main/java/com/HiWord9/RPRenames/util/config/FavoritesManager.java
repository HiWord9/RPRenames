package com.HiWord9.RPRenames.util.config;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.rename.RenameSerializer;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FavoritesManager {

    public static Map<Item, ArrayList<AbstractRename>> getAllFavorites() {
        Map<Item, ArrayList<AbstractRename>> favoriteRenames = new HashMap<>();
        File[] files = RPRenames.configPathFavorite.toFile().listFiles();
        if (files == null) return favoriteRenames;
        for (File file : files) {
            Item item = itemFromFavoriteFileName(file.getName());
            favoriteRenames.put(item, getFavorites(item));
        }
        return favoriteRenames;
    }

    private static Item itemFromFavoriteFileName(String fileName) {
        if (!fileName.endsWith(".json")) return Items.AIR;
        String itemFromFileName = fileName.substring(0, fileName.length() - 5).replace(".", ":");
        return ParserHelper.itemFromName(itemFromFileName);
    }

    public static ArrayList<AbstractRename> getFavorites(Item item) {
        ArrayList<AbstractRename> renames = savedFavorites(item);

        fixRenameItemsIfNeeded(renames, item);

        return renames;
    }

    private static void fixRenameItemsIfNeeded(ArrayList<AbstractRename> renames, Item item) {
        boolean fix = false;
        for (AbstractRename rename : renames) {
            if (rename instanceof CITRename citRename) {
                RPRenames.LOGGER.error("Fixing items list for favorite Rename \"{}\". Looks like it was created in ver <0.8.0", rename.getName());
                citRename.setItems(new ArrayList<>(List.of(item)));
                fix = true;
            }
        }
        if (!fix) return;
        RPRenames.LOGGER.warn("Recreating Favorite Renames List File for \"{}\" with fixed Items.", item);
        deleteFavoriteConfigFile(item);
        for (AbstractRename rename : renames) {
            addToFavorites(rename.getName(), item);
        }
    }

    public static void addToFavorites(String favoriteName, Item item) {
        ArrayList<AbstractRename> renames = new ArrayList<>();
        AbstractRename rename = new AbstractRename(favoriteName, item);
        ArrayList<AbstractRename> alreadyExist = getFavorites(item);
        if (!alreadyExist.isEmpty()) {
            ArrayList<AbstractRename> newConfig = new ArrayList<>(alreadyExist);
            newConfig.add(rename);
            renames = newConfig;
        } else {
            if (RPRenames.configPathFavorite.toFile().mkdirs()) {
                RPRenames.LOGGER.info("Created folder for favorites config: {}", RPRenames.configPathFavorite);
            }
            RPRenames.LOGGER.info("Created new file for favorites config: {}", pathToFavoriteFile(item));
            renames.add(rename);
        }

        writeFavoriteFile(renames, item);
    }

    public static void removeFromFavorites(String favoriteName, Item item) {
        ArrayList<AbstractRename> renamesList = getFavorites(item);
        int indexInRenamesList = new AbstractRename(favoriteName).indexIn(renamesList, true);
        if (indexInRenamesList >= 0) {
            renamesList.remove(indexInRenamesList);
        }

        if (!renamesList.isEmpty()) {
            writeFavoriteFile(renamesList, item);
        } else {
            deleteFavoriteConfigFile(item);
        }
    }

    private static ArrayList<AbstractRename> savedFavorites(Item item) {
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
            Type type = new TypeToken<ArrayList<AbstractRename>>() {
            }.getType();
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
            FileWriter fileWriter = new FileWriter(pathToFavoriteFile(item));
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

    public static boolean isFavorite(Item item, String name) {
        ArrayList<AbstractRename> favoriteList = getFavorites(item);
        for (AbstractRename r : favoriteList) {
            if (r.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static String pathToFavoriteFile(Item item) {
        return RPRenames.configPathFavorite + File.separator + fileNameFromItem(item);
    }

    private static String fileNameFromItem(Item item) {
        return ParserHelper.idFromItem(item).replace(":", ".") + ".json";
    }
}
