package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.rename.type.Rename;
import net.minecraft.item.Item;

import java.util.*;
import java.util.stream.Collectors;

public class RenamesManagerImpl<R extends Rename> implements RenamesManager<R> {
    protected final Map<Item, List<R>> renames = new HashMap<>();

    public List<R> getAllRenames() {
        return renames
                .values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<R> getRenames(Item item) {
        var l = renames.get(item);
        return l == null ? List.of() : List.copyOf(l);
    }

    public void addRename(Item item, R rename) {
        renames.computeIfAbsent(item, i -> new ArrayList<>()).add(rename);
    }

    public void removeRename(Item item, R rename) {
        var l = renames.get(item);
        if (l != null) l.remove(rename);
    }

    public void clearRenames() {
        renames.clear();
    }
}
