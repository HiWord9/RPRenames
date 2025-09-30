package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.Condition;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class RenameData {
    protected final List<Condition> conditions;
    protected final SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition;

    private RenameData(
            List<Condition> conditions,
            SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition
    ) {
        this.conditions = conditions;
        this.renameCondition = renameCondition;
    }

    protected static RenameData of(List<Condition> conditions) {
        var renameCondition = getRenameCondition(conditions);
        if (renameCondition == null) return null;
        return new RenameData(conditions, renameCondition);
    }

    private static @Nullable SelectCondition<ComponentSelectProperty<Text>, Text> getRenameCondition(
            List<Condition> conditions
    ) {
        SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition = null;
        for (Condition condition : conditions) {
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
            Condition condition
    ) {
        if (condition instanceof SelectCondition<?, ?> select
                && select.property() instanceof ComponentSelectProperty<?>(ComponentType<?> componentType)
                && componentType.equals(DataComponentTypes.CUSTOM_NAME)
        ) return (SelectCondition<ComponentSelectProperty<Text>, Text>) select;

        return null;
    }
}
