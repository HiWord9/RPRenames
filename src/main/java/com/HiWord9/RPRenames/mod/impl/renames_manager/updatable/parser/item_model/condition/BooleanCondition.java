package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.bool.*;
import net.minecraft.client.render.item.property.numeric.CountProperty;
import net.minecraft.client.render.item.property.numeric.CustomModelDataFloatProperty;
import net.minecraft.client.render.item.property.numeric.DamageProperty;
import net.minecraft.item.ItemStack;

import java.util.List;

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
        for (var clazz : ApplicableBooleanCondition.APPLICABLE_PROPERTIES) {
            if (clazz.isInstance(property)) return true;
        }
        return false;
    }

    public static class ApplicableBooleanCondition<P extends BooleanProperty>
            extends BooleanCondition<P>
            implements ItemModelCondition.Applicable
    {
        private static final List<Class<?>> APPLICABLE_PROPERTIES = List.of(
                BrokenProperty.class,
                ComponentBooleanProperty.class,
                CustomModelDataFlagProperty.class,
                DamagedProperty.class,
                HasComponentProperty.class
        );

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
