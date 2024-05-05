package com.HiWord9.RPRenames.util.config;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.Rename;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesManager {

    public static Map<String, ArrayList<Rename>> getAllFavorites() {
        Map<String, ArrayList<Rename>> favoriteRenames = new HashMap<>();
        File[] files = RPRenames.configPathFavorite.toFile().listFiles();
        if (files == null) return favoriteRenames;
        for (File file : files) {
            String fileName = file.getName();
            String item = fileName.substring(0, fileName.length() - 5);
            favoriteRenames.put(item.replace(".", ":"), getFavorites(item));
        }
        return favoriteRenames;
    }

    public static ArrayList<Rename> getFavorites(String item) {
        ArrayList<Rename> renames = new ArrayList<>();
        File favoritesFile = new File(RPRenames.configPathFavorite + File.separator + item.replace(":", ".") + ".json");
        if (favoritesFile.exists()) {
            try {
                FileReader fileReader = new FileReader(favoritesFile);
                Type type = new TypeToken<ArrayList<Rename>>() {
                }.getType();
                Gson gson = new Gson();
                renames = gson.fromJson(fileReader, type);
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fixRenameItemsIfNeeded(renames, item);

        return renames;
    }

    private static void fixRenameItemsIfNeeded(ArrayList<Rename> renames, String item) {
        boolean fix = false;
        for (Rename rename : renames) {
            if (rename.getItems() != null) continue;
            RPRenames.LOGGER.error("Fixing items list for favorite Rename \"" + rename.getName() + "\". Looks like it was created in ver <0.8.0");
            rename.setItems(new ArrayList<>(List.of(item)));
            fix = true;
        }
        if (!fix) return;
        RPRenames.LOGGER.warn("Recreating Favorite Renames List File for \"" + item + "\" with fixed Items.");
        deleteFavoriteConfigFile(item);
        for (Rename rename : renames) {
            addToFavorites(rename.getName(), item);
        }
    }

    public static void addToFavorites(String favoriteName, String item) {
        ArrayList<Rename> listNames = new ArrayList<>();
        Rename rename = new Rename(favoriteName, item);
        ArrayList<Rename> alreadyExist = getFavorites(item);
        if (!alreadyExist.isEmpty()) {
            ArrayList<Rename> newConfig = new ArrayList<>(alreadyExist);
            newConfig.add(rename);
            listNames = newConfig;
        } else {
            if (RPRenames.configPathFavorite.toFile().mkdirs()) {
                RPRenames.LOGGER.info("Created folder for favorites config: {}", RPRenames.configPathFavorite);
            }
            RPRenames.LOGGER.info("Created new file for favorites config: {}{}{}.json", RPRenames.configPathFavorite, File.separator, item.replaceAll(":", "."));
            listNames.add(rename);
        }


        writeFavoriteFile(listNames, item);
    }

    public static void removeFromFavorites(String favoriteName, String item) {
        ArrayList<Rename> renamesList = getFavorites(item);
        int indexInRenamesList = new Rename(favoriteName).indexIn(renamesList, true);
        if (indexInRenamesList >= 0) {
            renamesList.remove(indexInRenamesList);
        }

        if (!renamesList.isEmpty()) {
            writeFavoriteFile(renamesList, item);
        } else {
            deleteFavoriteConfigFile(item);
        }
    }

    private static void writeFavoriteFile(ArrayList<Rename> renames, String item) {
        try {
            FileWriter fileWriter = new FileWriter(RPRenames.configPathFavorite + File.separator + item.replaceAll(":", ".") + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(renames, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteFavoriteConfigFile(String item) {
        try {
            Files.deleteIfExists(Path.of(RPRenames.configPathFavorite + File.separator + item.replaceAll(":", ".") + ".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFavorite(String item, String name) {
        ArrayList<Rename> favoriteList = getFavorites(item);
        for (Rename r : favoriteList) {
            if (r.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
