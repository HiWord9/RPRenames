package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.numeric.CountProperty;
import net.minecraft.client.render.item.property.numeric.CustomModelDataFloatProperty;
import net.minecraft.client.render.item.property.numeric.DamageProperty;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
        for (var clazz : ApplicableNumericCondition.APPLICABLE_PROPERTIES) {
            if (clazz.isInstance(property)) return true;
        }
        return false;
    }

    public static class ApplicableNumericCondition<P extends NumericProperty>
            extends NumericCondition<P>
            implements ItemModelCondition.Applicable
    {
        private static final List<Class<?>> APPLICABLE_PROPERTIES = List.of(
                CountProperty.class,
                CustomModelDataFloatProperty.class,
                DamageProperty.class
        );

        private ApplicableNumericCondition(P property, float threshold) {
            super(property, threshold);
        }

        @Override
        public void apply(ItemStack stack) {
            switch (property) {
                case CountProperty prop -> applyCount(stack, prop, value);
                case CustomModelDataFloatProperty prop -> applyCustomModelData(stack, prop, value);
                case DamageProperty prop -> applyDamage(stack, prop, value);
                case null, default -> {}
            }
        }

        private static void applyCount(ItemStack stack, CountProperty property, float value) {
            stack.setCount(Math.round(value * (property.normalize() ? stack.getMaxCount() : 1)));
        }

        private static void applyCustomModelData(ItemStack stack, CustomModelDataFloatProperty property, float value) {
            var exists = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (exists == null) {
                stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                        new ArrayList<>(List.of(value)),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                ));
            } else {
                exists.floats().set(property.index(), value);
            }
        }

        private static void applyDamage(ItemStack stack, DamageProperty property, float value) {
            stack.setDamage(Math.round(value * (property.normalize() ? stack.getMaxDamage() : 1)));
        }
    }
}
