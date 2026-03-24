package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import net.minecraft.world.item.Item;
import java.util.*;

public class ItemModelData {
    protected final List<Item> items;
    protected final List<ItemModelCondition.Applicable> applicableConditions;
    protected final List<List<ItemModelCondition>> contexts;

    public ItemModelData(
            List<Item> items,
            List<ItemModelCondition.Applicable> conditions,
            List<List<ItemModelCondition>> contexts
    ) {
        this.items = items;
        this.applicableConditions = conditions;
        this.contexts = contexts;
    }

    public ItemModelData tryMerge(ItemModelData other) {
        var mergedConditions = tryMergeConditions(applicableConditions, other.applicableConditions);
        if (mergedConditions == null) return null;

        var contexts = new ArrayList<>(this.contexts);
        contexts.addAll(other.contexts);

        var items = new ArrayList<>(this.items);
        for (Item item : other.items)
            if (!items.contains(item))
                items.add(item);

        return new ItemModelData(items, mergedConditions, contexts);
    }

    // todo this should be improved for edge cases
    protected static List<ItemModelCondition.Applicable> tryMergeConditions(
            List<ItemModelCondition.Applicable> first,
            List<ItemModelCondition.Applicable> second
    ) {
        if (new HashSet<>(first).equals(new HashSet<>(second))) {
            return new ArrayList<>(first);
        }
        return null;
    }

    public static List<ItemModelData> mergeAllPossible(List<ItemModelData> itemModelDataList) {
        var list = new ArrayList<>(itemModelDataList);
        for (int i = 0; i < list.size(); i++) {
            var currentData = list.get(i);
            if (currentData == null) continue;

            for (int j = i+1; j < list.size(); j++) {
                var other = list.get(j);
                if (other == null) continue;

                var merged = currentData.tryMerge(other);
                if (merged == null) continue;

                currentData = merged;
                list.set(i, currentData);
                list.set(j, null);
            }
        }
        list.removeIf(Objects::isNull);
        return list;
    }

    public static ItemModelData of(List<ItemModelCondition> conditions, List<Item> items) {
        var applicableConditions = pullApplicableConditions(conditions);
        var contexts = getContexts(conditions, applicableConditions);

        return new ItemModelData(items, applicableConditions, contexts);
    }

    protected static List<List<ItemModelCondition>> getContexts(
            List<ItemModelCondition> allConditions,
            List<ItemModelCondition.Applicable> applicableConditions
    ) {
        var context = new ArrayList<>(allConditions);
        context.removeAll(applicableConditions);
        var contexts = new ArrayList<List<ItemModelCondition>>();
        if (!context.isEmpty()) contexts.add(context);
        return contexts;
    }

    protected static List<ItemModelCondition.Applicable> pullApplicableConditions(List<ItemModelCondition> conditions) {
        var applicableConditions = new ArrayList<ItemModelCondition.Applicable>();
        for (var condition : conditions) {
            if (condition instanceof ItemModelCondition.Applicable applicable) {
                applicableConditions.add(applicable);
            }
        }
        return applicableConditions;
    }
}
