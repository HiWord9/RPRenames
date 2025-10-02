package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.client.render.item.property.select.SelectProperty;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class SelectCondition<P extends SelectProperty<V>, V> implements ItemModelCondition {
    public final P property;
    public final List<V> values;

    private SelectCondition(P property, List<V> values) {
        this.property = property;
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SelectCondition<?, ?> that
                && Objects.equals(property, that.property)
                && Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, values);
    }

    public static <P extends SelectProperty<V>, V> SelectCondition<P, V> of(
            P property, List<V> values
    ) {
        return isPropertyApplicable(property)
                ? new ApplicableSelectCondition<>(property, values)
                : new SelectCondition<>(property, values);
    }

    private static <V> boolean isPropertyApplicable(SelectProperty<V> property) {
        return property instanceof ComponentSelectProperty<?>;
        // todo add cases
    }

    public static class ApplicableSelectCondition<P extends SelectProperty<V>, V>
            extends SelectCondition<P, V>
            implements ItemModelCondition.Applicable
    {
        private ApplicableSelectCondition(P property, List<V> values) {
            super(property, values);
        }

        @Override
        @SuppressWarnings("unchecked") // todo wrap with try
        public void apply(ItemStack stack) {
            if (property instanceof ComponentSelectProperty<?>) {
                var componentSelectProperty = (ComponentSelectProperty<V>) property;
                stack.set(componentSelectProperty.componentType(), values.getFirst());
            } // todo fill for all apply cases
        }
    }
}
