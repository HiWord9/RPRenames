package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.item.ItemStack;

import java.util.List;

public class SelectCondition<P extends SelectProperty<V>, V> implements ItemModelCondition.Applicable {
    public final P property;
    public final List<V> values;

    private SelectCondition(P property, List<V> values) {
        this.property = property;
        this.values = values;
    }

    public static <P extends SelectProperty<V>, V> SelectCondition<P, V> of(
            P property, List<V> values
    ) {
        return new SelectCondition<>(property, values);
    }

    @Override
    @SuppressWarnings("unchecked") // todo wrap with try
    public void apply(ItemStack stack) {
        if (property instanceof ComponentSelectProperty<?>) {
            var componentSelectProperty = (ComponentSelectProperty<V>) property;
            stack.set(componentSelectProperty.componentType(), values.getFirst());
        } // todo fill for other cases
    }
}
