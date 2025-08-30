package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.item.Item;

import java.util.List;

public interface RenamesManager {

    List<AbstractRename> getAllRenames();

    List<AbstractRename> getRenames(Item item);

    default void addRename(AbstractRename rename) {
        addRename(rename.getItem(), rename);
    }

    void addRename(Item item, AbstractRename rename);

    default void removeRename(AbstractRename rename) {
        removeRename(rename.getItem(), rename);
    }

    void removeRename(Item item, AbstractRename rename);

    void clearRenames();

    void overrideRenames(Item item, List<AbstractRename> newRenames);

    List<Item> renamedItems();
}