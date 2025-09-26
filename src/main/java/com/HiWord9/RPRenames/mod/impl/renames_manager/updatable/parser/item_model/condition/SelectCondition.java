package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.item.ItemStack;

import java.util.List;

public record SelectCondition<P extends SelectProperty<V>, V>(
        P property,
        List<V> values
) implements Condition {
    @Override
    @SuppressWarnings("unchecked") // todo wrap with try
    public void apply(ItemStack stack) {
        if (property instanceof ComponentSelectProperty<?>) {
            var componentSelectProperty = (ComponentSelectProperty<V>) property;
            stack.set(componentSelectProperty.componentType(), values.getFirst());
        } // todo fill for other cases
    }
}
