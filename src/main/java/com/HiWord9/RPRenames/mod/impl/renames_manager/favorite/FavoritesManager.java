package com.HiWord9.RPRenames.mod.impl.renames_manager.favorite;

import com.HiWord9.RPRenames.mod.impl.renames_manager.RenamesManagerImpl;
import net.minecraft.item.Item;

import java.util.Collection;
import java.util.Set;

public class FavoritesManager extends RenamesManagerImpl<FavoriteRename> {
    private final TaskQueueThread taskQueue = new TaskQueueThread();
    private final FavoritesFileManager favoritesFileManager;

    public FavoritesManager(FavoritesFileManager favoritesFileManager) {
        this.favoritesFileManager = favoritesFileManager;
        taskQueue.start();
    }

    public void loadSavedFavorites() {
        renames.putAll(favoritesFileManager.getAllSavedFavorites());
    }

    public void addRenames(Collection<Item> items, String name) {
        items.forEach(item -> addRename(item, name));
    }

    public void addRename(Item item, String name) {
        addRename(item, new FavoriteRename(name, item));
    }

    public void addRename(Item item, FavoriteRename rename) {
        super.addRename(item, rename);
        updateFile(item);
    }

    public void removeRenames(Collection<Item> items, String name) {
        items.forEach(item -> removeRename(item, name));
    }

    public void removeRename(Item item, String name) {
        removeRename(item, new FavoriteRename(name, item));
    }

    public void removeRename(Item item, FavoriteRename rename) {
        if (!isFavorite(item, rename)) return;
        super.removeRename(item, rename);
        updateFile(item);
    }

    public void clearRenames() {
        var items = Set.copyOf(renames.keySet());
        super.clearRenames();
        taskQueue.addTask(() -> favoritesFileManager.doomConfigs(items));
    }

    public boolean isFavorite(Item item, String name) {
        return getRenames(item).stream().anyMatch(r -> r.getName().equals(name));
    }

    public boolean isFavorite(Item item, FavoriteRename rename) {
        return getRenames(item).contains(rename);
    }

    public boolean isFavoriteAny(Collection<Item> items, String name) {
        return items.stream().anyMatch(item -> isFavorite(item, name));
    }

    private void updateFile(Item item) {
        taskQueue.addTask(() -> favoritesFileManager.setFavorites(getRenames(item), item));
    }
}
