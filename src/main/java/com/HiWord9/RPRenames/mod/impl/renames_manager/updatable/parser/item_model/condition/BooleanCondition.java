package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.bool.BooleanProperty;
import net.minecraft.item.ItemStack;

public non-sealed class BooleanCondition<P extends BooleanProperty> extends AbstractPropertyValueCondition<P, Boolean> {
    private BooleanCondition(P property, boolean value) {
        super(property, value);
    }

    public static <P extends BooleanProperty> BooleanCondition<P> of(
            P property, boolean value
    ) {
        return isPropertyApplicable(property)
                ? new ApplicableBooleanCondition<>(property, value)
                : new BooleanCondition<>(property, value);
    }

    private static boolean isPropertyApplicable(BooleanProperty property) {
        // todo add cases
        return false;
    }

    public static class ApplicableBooleanCondition<P extends BooleanProperty>
            extends BooleanCondition<P>
            implements ItemModelCondition.Applicable
    {
        private ApplicableBooleanCondition(P property, boolean value) {
            super(property, value);
        }

        @Override
        public void apply(ItemStack stack) {
            // todo fill for all apply cases
            Applicable.super.apply(stack);
        }
    }
}
