package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.HiWord9.RPRenames.mod.impl.rename.ItemModelRename;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.Parser;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.HiWord9.RPRenames.api.RenamesManager;
import com.HiWord9.RPRenames.mod.util.Util;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemModelParser implements Parser {
    private final Map<Identifier, ItemAsset> itemAssets = new HashMap<>();

    public RenamesManager<? super ItemModelRename> renamesManager;

    public ItemModelParser(RenamesManager<? super ItemModelRename> renamesManager) {
        this.renamesManager = renamesManager;
    }

    public void updateItemAssets(Map<Identifier, ItemAsset> itemAssets) {
        this.itemAssets.clear();
        this.itemAssets.putAll(itemAssets);
    }

    @Override
    public void parse(ResourceManager resourceManager, Profiler profiler) {
        var renameDataList = getRenameDataList(itemAssets);

        renameDataList.forEach(data -> {
            var rename = bakeRename(data);
            renamesManager.addRename(rename);
        });
    }

    private static ItemModelRename bakeRename(RenameData renameData) {
        return new ItemModelRename(
                renameData.applicableConditions,
                renameData.renameCondition.value.getFirst().getString(),
                renameData.items.toArray(new Item[]{})
        );
    }

    private static List<RenameData> getRenameDataList(Map<Identifier, ItemAsset> itemAssets) {
        var resultList = new ArrayList<RenameData>();
        itemAssets.forEach((id, asset) -> {
            var list = new ArrayList<RenameData>();
            fillRenameDataList(list, List.of(), asset.model(), Util.itemFromId(id));
            resultList.addAll(list);
        });
        return RenameData.mergeAllPossible(resultList);
    }

    private static void fillRenameDataList(
            List<RenameData> renameDataList,
            List<ItemModelCondition> conditions,
            ItemModel.Unbaked unbakedModel,
            Item item
    ) {
        var cases = Case.getCases(unbakedModel);
        if (!cases.isEmpty()) {
            for (var modelCase : cases) {
                var newConditions = new ArrayList<>(conditions);
                newConditions.add(modelCase.condition());
                fillRenameDataList(renameDataList, newConditions, modelCase.result(), item);
            }
        } else {
            var renameData = RenameData.of(conditions, List.of(item));
            if (renameData != null) renameDataList.add(renameData);
        }
    }
}
