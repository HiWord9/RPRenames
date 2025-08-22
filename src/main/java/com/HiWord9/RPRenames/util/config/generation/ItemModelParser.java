package com.HiWord9.RPRenames.util.config.generation;

import com.HiWord9.RPRenames.util.rename.RenamesManager;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;

public class ItemModelParser implements Parser {
    private Map<Identifier, ItemAsset> itemAssets = new HashMap<>();

    public RenamesManager renamesManager;

    public ItemModelParser(RenamesManager renamesManager) {
        this.renamesManager = renamesManager;
    }

    @Override
    public void parse(ResourceManager resourceManager, Profiler profiler) {
        // here parse itemAssets as a tree of models to get renames
    }

    public void updateItemAssets(Map<Identifier, ItemAsset> itemAssets) {
        this.itemAssets.clear();
        this.itemAssets.putAll(itemAssets);
    }
}
