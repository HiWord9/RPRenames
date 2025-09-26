package com.HiWord9.RPRenames.mod.impl.renames_manager.favorite;

import com.HiWord9.RPRenames.mod.impl.renames_manager.RenamesManagerImpl;
import com.HiWord9.RPRenames.mod.util.TaskQueueThread;
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

    public boolean addRenames(Collection<Item> items, String name) {
        boolean bl = false;
        for (Item item : items) bl |= addRename(item, name);
        return bl;
    }

    public boolean addRename(Item item, String name) {
        return addRename(item, new FavoriteRename(name, item));
    }

    public boolean addRename(Item item, FavoriteRename rename) {
        boolean bl = super.addRename(item, rename);
        updateFile(item);
        return bl;
    }

    public boolean removeRenames(Collection<Item> items, String name) {
        boolean bl = false;
        for (Item item : items) bl |= removeRename(item, name);
        return bl;
    }

    public boolean removeRename(Item item, String name) {
        return removeRename(item, new FavoriteRename(name, item));
    }

    public boolean removeRename(Item item, FavoriteRename rename) {
        if (!isFavorite(item, rename)) return false;
        boolean bl = super.removeRename(item, rename);
        updateFile(item);
        return bl;
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
