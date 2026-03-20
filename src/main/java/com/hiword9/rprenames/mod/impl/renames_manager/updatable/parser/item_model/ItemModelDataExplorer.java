package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.BooleanCondition;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.NumericCondition;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import com.hiword9.rprenames.mod.util.Util;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.*;
import net.minecraft.client.render.item.property.select.SelectProperty;
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
        var cases = getCases(unbakedModel, asset);
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

    public record Case(ItemModelCondition condition, ItemModel.Unbaked result) {}

    public static List<Case> getCases(ItemModel.Unbaked unbakedModel, ItemAsset asset) {
        var cases = new ArrayList<Case>();
        switch (unbakedModel) {
            case CompositeItemModel.Unbaked composite -> fillCases(cases, composite);
            case ConditionItemModel.Unbaked condition -> fillCases(cases, condition);
            case SelectItemModel.Unbaked select -> fillCases(cases, select, asset);
            case RangeDispatchItemModel.Unbaked rangeDispatch -> fillCases(cases, rangeDispatch);
            default -> {} // basic, empty, bundle/selected_item, special and unknown
        }
        return cases;
    }

    private static void fillCases(List<Case> cases, CompositeItemModel.Unbaked unbakedCompositeModel) {
        for (ItemModel.Unbaked model : unbakedCompositeModel.models()) {
            cases.add(new Case(ItemModelCondition.COMPOSITE, model));
        }
    }

    private static void fillCases(List<Case> cases, ConditionItemModel.Unbaked unbakedConditionModel) {
        cases.add(new Case(
                BooleanCondition.of(unbakedConditionModel.property(), true),
                unbakedConditionModel.onTrue()
        ));
        cases.add(new Case(
                BooleanCondition.of(unbakedConditionModel.property(), false),
                unbakedConditionModel.onFalse()
        ));
    }

    @SuppressWarnings("unchecked")
    private static <P extends SelectProperty<T>, T> void fillCases(
            List<Case> cases, SelectItemModel.Unbaked unbakedSelectModel, ItemAsset asset
    ) {
        var unbakedSwitch = (SelectItemModel.UnbakedSwitch<P, T>) unbakedSelectModel.unbakedSwitch();

        for (var switchCase : unbakedSwitch.cases()) {
            cases.add(new Case(
                    SelectCondition.of(unbakedSwitch.property(), switchCase.values(), asset),
                    switchCase.model()
            ));
        }

        unbakedSelectModel.fallback().ifPresent(fallback ->
                cases.add(new Case(ItemModelCondition.FALLBACK, fallback))
        );
    }

    private static void fillCases(List<Case> cases, RangeDispatchItemModel.Unbaked unbakedRangeDispatchModel) {
        for (RangeDispatchItemModel.Entry entry : unbakedRangeDispatchModel.entries()) {
            cases.add(new Case(
                    NumericCondition.of(
                            unbakedRangeDispatchModel.property(),
                            unbakedRangeDispatchModel.scale(),
                            entry.threshold()
                    ),
                    entry.model()
            ));
        }

        unbakedRangeDispatchModel.fallback().ifPresent(fallback ->
                cases.add(new Case(ItemModelCondition.FALLBACK, fallback))
        );
    }
}
