package com.HiWord9.RPRenames.util.config.favorite;

import com.HiWord9.RPRenames.util.rename.RenamesManagerImpl;
import com.HiWord9.RPRenames.util.rename.type.Rename;
import net.minecraft.item.Item;

import java.util.Set;

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

    public void addRename(Item item, String name) {
        addRename(item, new Rename(name, item));
    }

    public void addRename(Item item, Rename rename) {
        super.addRename(item, rename);
        taskQueue.addTask(() -> favoritesFileManager.setFavorites(getRenames(item), item));
    }

    public void removeRename(Item item, String name) {
        removeRename(item, new Rename(name, item));
    }

    public void removeRename(Item item, Rename rename) {
        super.removeRename(item, rename);
        taskQueue.addTask(() -> favoritesFileManager.setFavorites(getRenames(item), item));
    }

    public void clearRenames() {
        var items = Set.copyOf(renames.keySet());
        super.clearRenames();
        taskQueue.addTask(() -> favoritesFileManager.doomConfigs(items));
    }

    public boolean isFavorite(Item item, String name) {
        return getRenames(item).stream().anyMatch(r -> r.getName().equals(name));
    }

    public boolean isFavorite(Item item, Rename rename) {
        return getRenames(item).contains(rename);
    }
}
