package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.rename.type.Rename;
import net.minecraft.item.Item;

import java.util.List;

public interface RenamesManager<R extends Rename> {

    List<R> getAllRenames();

    List<R> getRenames(Item item);

    default void addRename(R rename) {
        addRename(rename.getItem(), rename);
    }

    void addRename(Item item, R rename);

    default void removeRename(R rename) {
        removeRename(rename.getItem(), rename);
    }

    void removeRename(Item item, R rename);

    void clearRenames();
}