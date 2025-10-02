package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class RenameData {
    protected final List<Item> items;
    protected final List<ItemModelCondition.Applicable> applicableConditions;
    protected final List<List<ItemModelCondition>> contexts;
    protected final SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition;

    private RenameData(
            List<Item> items,
            List<ItemModelCondition.Applicable> conditions,
            List<List<ItemModelCondition>> contexts,
            SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition
    ) {
        this.items = items;
        this.applicableConditions = conditions;
        this.contexts = contexts;
        this.renameCondition = renameCondition;
    }

    protected RenameData tryMerge(RenameData other) {
        var mergedConditions = tryMergeConditions(applicableConditions, other.applicableConditions);
        if (mergedConditions == null) return null;

        var contexts = new ArrayList<>(this.contexts);
        contexts.addAll(other.contexts);

        var items = new ArrayList<>(this.items);
        for (Item item : other.items)
            if (!items.contains(item))
                items.add(item);

        return new RenameData(items, mergedConditions, contexts, renameCondition);
    }

    // todo this should be improved for edge cases
    private static List<ItemModelCondition.Applicable> tryMergeConditions(
            List<ItemModelCondition.Applicable> first,
            List<ItemModelCondition.Applicable> second
    ) {
        if (new HashSet<>(first).equals(new HashSet<>(second))) {
            return new ArrayList<>(first);
        }
        return null;
    }

    protected static List<RenameData> mergeAllPossible(List<RenameData> renameDataList) {
        var list = new ArrayList<>(renameDataList);
        for (int i = 0; i < list.size(); i++) {
            var currentData = list.get(i);
            for (int j = i+1; j < list.size();) {
                var merged = currentData.tryMerge(list.get(j));
                if (merged != null) {
                    currentData = merged;
                    list.set(i, currentData);
                    list.remove(j);
                } else {
                    j++;
                }
            }
        }
        return list;
    }

    protected static RenameData of(List<ItemModelCondition> conditions, List<Item> items) {
        var renameCondition = getRenameCondition(conditions);
        if (renameCondition == null) return null;

        var applicableConditions = pullApplicableConditions(conditions);
        var context = new ArrayList<>(conditions);
        context.removeAll(applicableConditions);
        var contexts = new ArrayList<List<ItemModelCondition>>();
        contexts.add(context);

        return new RenameData(items, applicableConditions, contexts, renameCondition);
    }

    private static List<ItemModelCondition.Applicable> pullApplicableConditions(List<ItemModelCondition> conditions) {
        var applicableConditions = new ArrayList<ItemModelCondition.Applicable>();
        for (var condition : conditions) {
            if (condition instanceof ItemModelCondition.Applicable applicable) {
                applicableConditions.add(applicable);
            }
        }
        return applicableConditions;
    }

    private static @Nullable SelectCondition<ComponentSelectProperty<Text>, Text> getRenameCondition(
            List<ItemModelCondition> conditions
    ) {
        SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition = null;
        for (ItemModelCondition condition : conditions) {
            var candidate = asCustomNameConditionOrNull(condition);
            if (candidate != null) {
                if (renameCondition == null) {
                    renameCondition = candidate;
                } else {
                    // todo handle multiple rename conditions; probably an error
                }
            }
        }
        return renameCondition;
    }

    @SuppressWarnings("unchecked")
    private static SelectCondition<ComponentSelectProperty<Text>, Text> asCustomNameConditionOrNull(
            ItemModelCondition condition
    ) {
        if (condition instanceof SelectCondition<?, ?> select
                && select.property instanceof ComponentSelectProperty<?>(ComponentType<?> componentType)
                && componentType.equals(DataComponentTypes.CUSTOM_NAME)
        ) return (SelectCondition<ComponentSelectProperty<Text>, Text>) select;

        return null;
    }
}
