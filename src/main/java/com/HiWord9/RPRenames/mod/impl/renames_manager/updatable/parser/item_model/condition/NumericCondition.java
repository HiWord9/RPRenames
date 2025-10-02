package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.item.ItemStack;

public non-sealed class NumericCondition<P extends NumericProperty> extends AbstractPropertyValueCondition<P, Float> {
    private NumericCondition(P property, float threshold) {
        super(property, threshold);
    }

    public static <P extends NumericProperty> NumericCondition<P> of(
            P property, float threshold
    ) {
        return isPropertyApplicable(property)
                ? new ApplicableNumericCondition<>(property, threshold)
                : new NumericCondition<>(property, threshold);
    }

    private static boolean isPropertyApplicable(NumericProperty property) {
        // todo add cases
        return false;
    }

    public static class ApplicableNumericCondition<P extends NumericProperty>
            extends NumericCondition<P>
            implements ItemModelCondition.Applicable
    {
        private ApplicableNumericCondition(P property, float threshold) {
            super(property, threshold);
        }

        @Override
        public void apply(ItemStack stack) {
            // todo fill for all apply cases
            Applicable.super.apply(stack);
        }
    }
}
