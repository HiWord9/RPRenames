package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RenamesManagerImpl implements RenamesManager {
    protected final Map<Item, ArrayList<AbstractRename>> renames = new HashMap<>();

    public ArrayList<AbstractRename> getAllRenames() {
        return (ArrayList<AbstractRename>) renames
                .entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public ArrayList<AbstractRename> getRenames(Item item) {
        if (renames.containsKey(item)) {
            return renames.get(item);
        } else {
            return new ArrayList<>();
        }
    }

    public void addRename(Item item, AbstractRename rename) {
        if (renames.containsKey(item)) {
            renames.get(item).add(rename);
        } else {
            ArrayList<AbstractRename> arrayList = new ArrayList<>();
            arrayList.add(rename);
            renames.put(item, arrayList);
        }
    }

    public void removeRename(Item item, AbstractRename rename) {
        if (renames.containsKey(item)) {
            renames.get(item).remove(rename);
        }
    }

    public void clearRenames() {
        renames.clear();
    }

    public void overrideRenames(Item item, ArrayList<AbstractRename> newRenames) {
        renames.put(item, newRenames);
    }

    public ArrayList<Item> renamedItems() {
        return new ArrayList<>(renames.keySet());
    }
}
