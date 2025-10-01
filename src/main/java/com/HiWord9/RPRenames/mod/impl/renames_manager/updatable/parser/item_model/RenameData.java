package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class RenameData {
    protected final List<ItemModelCondition> allConditions;
    protected final List<ItemModelCondition.Applicable> applicableConditions;
    protected final SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition;

    private RenameData(
            List<ItemModelCondition> conditions,
            SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition
    ) {
        this.allConditions = conditions;
        this.applicableConditions = pullApplicableConditions(this.allConditions);
        this.renameCondition = renameCondition;
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

    protected static RenameData of(List<ItemModelCondition> conditions) {
        var renameCondition = getRenameCondition(conditions);
        if (renameCondition == null) return null;
        return new RenameData(conditions, renameCondition);
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
                && select.property() instanceof ComponentSelectProperty<?>(ComponentType<?> componentType)
                && componentType.equals(DataComponentTypes.CUSTOM_NAME)
        ) return (SelectCondition<ComponentSelectProperty<Text>, Text>) select;

        return null;
    }
}
