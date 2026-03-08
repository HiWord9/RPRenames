package com.HiWord9.RPRenames.api;

import com.HiWord9.RPRenames.api.rename.Rename;
import net.minecraft.item.Item;

import java.util.List;

public interface RenamesManager<R extends Rename> extends RenamesProvider<R> {
    default boolean addRename(R rename) {
        return addRename(rename.getItem(), rename);
    }

    boolean addRename(Item item, R rename);

    default boolean removeRename(R rename) {
        return removeRename(rename.getItem(), rename);
    }

    boolean removeRename(Item item, R rename);

    void clearRenames();
}