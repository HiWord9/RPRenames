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
        var renameDataMap = getIdToRenameInfoMap(itemAssets);
        renameDataMap.forEach((id, renameDataList) -> {
            renameDataList.forEach(renameData -> {
                var rename = bakeRename(renameData, Util.itemFromId(id));
                renamesManager.addRename(rename);
            });
        });
        // todo: merge similar renames for different items
    }

    private static ItemModelRename bakeRename(RenameData renameData, Item... items) {
        return new ItemModelRename(
                renameData.applicableConditions,
                renameData.renameCondition.values().getFirst().getString(),
                items
        );
    }

    private static Map<Identifier, List<RenameData>> getIdToRenameInfoMap(Map<Identifier, ItemAsset> itemAssets) {
        var renameDataMap = new HashMap<Identifier, List<RenameData>>();
        itemAssets.forEach((id, asset) -> {
            var renameDataList = new ArrayList<RenameData>();
            fillRenamesConditionsMap(renameDataList, List.of(), asset.model());
            if (renameDataList.isEmpty()) return;
            renameDataMap.put(id, renameDataList);
        });
        return renameDataMap;
    }

    private static void fillRenamesConditionsMap(
            List<RenameData> renameDataList,
            List<ItemModelCondition> conditions,
            ItemModel.Unbaked unbakedModel
    ) {
        var cases = Case.getCases(unbakedModel);
        if (!cases.isEmpty()) {
            for (var modelCase : cases) {
                var newConditions = new ArrayList<>(conditions);
                newConditions.add(modelCase.condition());
                fillRenamesConditionsMap(renameDataList, newConditions, modelCase.result());
            }
        } else {
            var renameData = RenameData.of(conditions);
            if (renameData != null) renameDataList.add(renameData);
        }
    }
}
