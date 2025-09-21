package com.HiWord9.RPRenames.mod.impl.renames_manager;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.api.RenamesManager;
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

    public boolean addRename(Item item, R rename) {
        return renames.computeIfAbsent(item, i -> new ArrayList<>()).add(rename);
    }

    public boolean removeRename(Item item, R rename) {
        var l = renames.get(item);
        if (l != null) return l.remove(rename);
        else return false;
    }

    public void clearRenames() {
        renames.clear();
    }
}
