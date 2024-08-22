package com.HiWord9.RPRenames.util.config.favorite;

import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoritesManager {
    static FavoritesManager instance;
    private final Map<Item, ArrayList<AbstractRename>> favoriteRenames = new HashMap<>();
    private final TaskQueueThread taskQueue = new TaskQueueThread();

    public static synchronized FavoritesManager getInstance() {
        if (instance == null) {
            instance = new FavoritesManager();
        }
        return instance;
    }

    protected FavoritesManager() {
        taskQueue.start();
    }

    public void loadSavedFavorites() {
        favoriteRenames.putAll(FavoritesFileHelper.getAllSavedFavorites());
    }

    public Map<Item, ArrayList<AbstractRename>> getAllFavorites() {
        return new HashMap<>(favoriteRenames);
    }

    public ArrayList<AbstractRename> getFavorites(Item item) {
        ArrayList<AbstractRename> renames = favoriteRenames.get(item);
        return renames == null ? new ArrayList<>() : new ArrayList<>(renames);
    }

    public void addToFavorites(String favoriteName, Item item) {
        ArrayList<AbstractRename> renames = new ArrayList<>();
        AbstractRename rename = new AbstractRename(favoriteName, item);
        ArrayList<AbstractRename> alreadyExist = getFavorites(item);
        if (!alreadyExist.isEmpty()) {
            ArrayList<AbstractRename> newConfig = new ArrayList<>(alreadyExist);
            newConfig.add(rename);
            renames.addAll(newConfig);
        } else {
            renames.add(rename);
        }

        favoriteRenames.put(item, renames);
        taskQueue.addTask(() -> FavoritesFileHelper.setFavorites(renames, item));
    }

    public void removeFromFavorites(String favoriteName, Item item) {
        ArrayList<AbstractRename> renames = getFavorites(item);
        int indexInRenamesList = new AbstractRename(favoriteName).indexIn(renames, true);
        if (indexInRenamesList >= 0) {
            renames.remove(indexInRenamesList);
        }

        favoriteRenames.put(item, renames);
        taskQueue.addTask(() -> FavoritesFileHelper.setFavorites(renames, item));
    }

    public boolean isFavorite(Item item, String name) {
        ArrayList<AbstractRename> favoriteList = getFavorites(item);
        for (AbstractRename r : favoriteList) {
            if (r.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
