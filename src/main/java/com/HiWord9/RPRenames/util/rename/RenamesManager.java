package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.rename.type.Rename;
import net.minecraft.item.Item;

import java.util.List;

public interface RenamesManager {

    List<Rename> getAllRenames();

    List<Rename> getRenames(Item item);

    default void addRename(Rename rename) {
        addRename(rename.getItem(), rename);
    }

    void addRename(Item item, Rename rename);

    default void removeRename(Rename rename) {
        removeRename(rename.getItem(), rename);
    }

    void removeRename(Item item, Rename rename);

    void clearRenames();

    void overrideRenames(Item item, List<Rename> newRenames);

    List<Item> renamedItems();
}