package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.rename.type.Rename;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RenamesManagerImpl<R extends Rename> implements RenamesManager<R> {
    protected final Map<Item, List<R>> renames = new HashMap<>();

    public List<R> getAllRenames() {
        return renames
                .entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<R> getRenames(Item item) {
        if (renames.containsKey(item)) {
            return List.copyOf(renames.get(item));
        } else {
            return List.of();
        }
    }

    public void addRename(Item item, R rename) {
        renames.computeIfAbsent(item, i -> new ArrayList<>()).add(rename);
    }

    public void removeRename(Item item, R rename) {
        if (renames.containsKey(item)) {
            renames.get(item).remove(rename);
        }
    }

    public void clearRenames() {
        renames.clear();
    }
}
