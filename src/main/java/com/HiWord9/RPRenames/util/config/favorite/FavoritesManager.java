package com.HiWord9.RPRenames.util.config.favorite;

import com.HiWord9.RPRenames.util.rename.RenamesManagerImpl;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.item.Item;

import java.util.ArrayList;

public class FavoritesManager extends RenamesManagerImpl {
    private final TaskQueueThread taskQueue = new TaskQueueThread();
    private final FavoritesFileManager favoritesFileManager;

    public FavoritesManager(FavoritesFileManager favoritesFileManager) {
        this.favoritesFileManager = favoritesFileManager;
        taskQueue.start();
    }

    public void loadSavedFavorites() {
        renames.putAll(favoritesFileManager.getAllSavedFavorites());
    }

    public void addRename(Item item, AbstractRename rename) {
        super.addRename(item, rename);
        taskQueue.addTask(() -> favoritesFileManager.setFavorites(getRenames(item), item));
    }

    public void removeRename(Item item, AbstractRename rename) {
        super.removeRename(item, rename);
        taskQueue.addTask(() -> favoritesFileManager.setFavorites(getRenames(item), item));
    }

    public void overrideRenames(Item item, ArrayList<AbstractRename> newRenames) {
        super.overrideRenames(item, newRenames);
        taskQueue.addTask(() -> favoritesFileManager.setFavorites(getRenames(item), item));
    }

    public void clearRenames() {
        super.clearRenames();
        taskQueue.addTask(() -> favoritesFileManager.doomConfigs(renamedItems()));
    }

    public boolean isFavorite(Item item, String name) {
        ArrayList<AbstractRename> favoriteList = getRenames(item);
        for (AbstractRename r : favoriteList) {
            if (r.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
