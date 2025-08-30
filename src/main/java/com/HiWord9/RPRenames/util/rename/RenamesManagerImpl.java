package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RenamesManagerImpl implements RenamesManager {
    protected final Map<Item, List<AbstractRename>> renames = new HashMap<>();

    public List<AbstractRename> getAllRenames() {
        return renames
                .entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<AbstractRename> getRenames(Item item) {
        if (renames.containsKey(item)) {
            return List.copyOf(renames.get(item));
        } else {
            return List.of();
        }
    }

    public void addRename(Item item, AbstractRename rename) {
        renames.computeIfAbsent(item, i -> new ArrayList<>()).add(rename);
    }

    public void removeRename(Item item, AbstractRename rename) {
        if (renames.containsKey(item)) {
            renames.get(item).remove(rename);
        }
    }

    public void clearRenames() {
        renames.clear();
    }

    public void overrideRenames(Item item, List<AbstractRename> newRenames) {
        renames.put(item, new ArrayList<>(newRenames));
    }

    public List<Item> renamedItems() {
        return List.copyOf(renames.keySet());
    }
}
