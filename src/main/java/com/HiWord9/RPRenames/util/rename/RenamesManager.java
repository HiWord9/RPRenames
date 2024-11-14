package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.item.Item;

import java.util.ArrayList;

public interface RenamesManager {

    ArrayList<AbstractRename> getAllRenames();

    ArrayList<AbstractRename> getRenames(Item item);

    void addRename(Item item, AbstractRename rename);

    void removeRename(Item item, AbstractRename rename);

    void clearRenames();

    void overrideRenames(Item item, ArrayList<AbstractRename> newRenames);

    ArrayList<Item> renamedItems();
}