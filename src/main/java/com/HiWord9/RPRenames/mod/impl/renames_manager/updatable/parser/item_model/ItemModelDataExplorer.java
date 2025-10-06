package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.HiWord9.RPRenames.mod.util.Util;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.*;

public class ItemModelDataExplorer {

    public static Map<Item, List<ItemModelData>> getMapUnmerged(Map<Identifier, ItemAsset> itemAssets) {
        var map = new HashMap<Item, List<ItemModelData>>();
        itemAssets.forEach((id, asset) -> {
            var list = new ArrayList<ItemModelData>();
            var item = Util.itemFromId(id);
            fillItemModelDataList(list, List.of(), asset.model(), asset, item);
            map.put(item, list);
        });
        return map;
    }

    public static List<ItemModelData> getListMerged(Map<Identifier, ItemAsset> itemAssets) {
        return ItemModelData.mergeAllPossible(getListUnmerged(itemAssets));
    }

    public static List<ItemModelData> getListUnmerged(Map<Identifier, ItemAsset> itemAssets) {
        return getMapUnmerged(itemAssets)
                .values().stream()
                .flatMap(Collection::stream)
                .toList();
    }

    private static void fillItemModelDataList(
            List<ItemModelData> itemModelDataList,
            List<ItemModelCondition> conditions,
            ItemModel.Unbaked unbakedModel,
            ItemAsset asset,
            Item item
    ) {
        var cases = Case.getCases(unbakedModel, asset);
        if (!cases.isEmpty()) {
            for (var modelCase : cases) {
                var newConditions = new ArrayList<>(conditions);
                newConditions.add(modelCase.condition());
                fillItemModelDataList(itemModelDataList, newConditions, modelCase.result(), asset, item);
            }
        } else {
            itemModelDataList.add(ItemModelData.of(conditions, List.of(item)));
        }
    }
}
