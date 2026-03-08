package com.HiWord9.RPRenames.mod.impl.renames_manager;

import com.HiWord9.RPRenames.api.RenamesProvider;
import com.HiWord9.RPRenames.api.rename.Rename;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompositeRenamesProvider<R extends Rename> implements RenamesProvider<R> {
    public final List<RenamesProvider<R>> providers = new ArrayList<>();

    @Override
    public List<R> getAllRenames() {
        return collectFromAllProviders(RenamesProvider::getAllRenames);
    }

    @Override
    public List<R> getRenames(Item item) {
        return collectFromAllProviders(p -> p.getRenames(item));
    }

    private List<R> collectFromAllProviders(Function<RenamesProvider<R>, List<R>> function) {
        return providers.stream()
                .map(function)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
