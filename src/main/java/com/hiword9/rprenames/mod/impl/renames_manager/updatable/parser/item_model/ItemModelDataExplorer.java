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
    private static final Map<Class<? extends ItemModel.Unbaked>, CaseFiller<?>> UNBAKED_TO_FILLER = new HashMap<>();

    static {
        registerCaseFiller(CompositeItemModel.Unbaked.class, ItemModelDataExplorer::fillCases);
        registerCaseFiller(ConditionItemModel.Unbaked.class, ItemModelDataExplorer::fillCases);
        registerCaseFiller(SelectItemModel.Unbaked.class, ItemModelDataExplorer::fillCases);
        registerCaseFiller(RangeDispatchItemModel.Unbaked.class, ItemModelDataExplorer::fillCases);
    }

    public static <U extends ItemModel.Unbaked> void registerCaseFiller(Class<U> clazz, CaseFiller<U> caseFiller) {
        UNBAKED_TO_FILLER.put(clazz, caseFiller);
    }

    @SuppressWarnings("unchecked")
    public static <U extends ItemModel.Unbaked> CaseFiller<U> getCaseFiller(Class<U> clazz) {
        return (CaseFiller<U>) UNBAKED_TO_FILLER.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <U extends ItemModel.Unbaked> CaseFiller<U> getCaseFiller(U unbakedModel) {
        return (CaseFiller<U>) getCaseFiller(unbakedModel.getClass());
    }

    public interface CaseFiller<U extends ItemModel.Unbaked> {
        void fillCases(List<Case> cases, U unbakedModel, ItemAsset asset);
    }

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

    public static <U extends ItemModel.Unbaked> List<Case> getCases(U unbakedModel, ItemAsset asset) {
        var cases = new ArrayList<Case>();
        var caseFiller = getCaseFiller(unbakedModel);
        if (caseFiller != null)
            caseFiller.fillCases(cases, unbakedModel, asset);
        return cases;
    }

    private static void fillCases(
            List<Case> cases, CompositeItemModel.Unbaked unbakedCompositeModel, ItemAsset asset
    ) {
        for (ItemModel.Unbaked model : unbakedCompositeModel.models()) {
            cases.add(new Case(ItemModelCondition.COMPOSITE, model));
        }
    }

    private static void fillCases(
            List<Case> cases, ConditionItemModel.Unbaked unbakedConditionModel, ItemAsset asset
    ) {
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

    private static void fillCases(
            List<Case> cases, RangeDispatchItemModel.Unbaked unbakedRangeDispatchModel, ItemAsset asset
    ) {
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
