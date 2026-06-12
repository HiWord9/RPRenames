package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.renderer.item.properties.numeric.Count;
import net.minecraft.client.renderer.item.properties.numeric.CustomModelDataProperty;
import net.minecraft.client.renderer.item.properties.numeric.Damage;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import java.util.ArrayList;
import java.util.List;

import static com.hiword9.rprenames.util.Util.*;

public non-sealed class NumericCondition<P extends RangeSelectItemModelProperty> extends AbstractPropertyValueCondition<P, Float> {
    private NumericCondition(P property, float threshold) {
        super(property, threshold);
    }

    public static <P extends RangeSelectItemModelProperty> NumericCondition<P> of(
            P property, float scale, float threshold
    ) {
        threshold = threshold / scale;
        return isPropertyApplicable(property)
                ? new ApplicableNumericCondition<>(property, threshold)
                : new NumericCondition<>(property, threshold);
    }

    private static boolean isPropertyApplicable(RangeSelectItemModelProperty property) {
        for (var clazz : ApplicableNumericCondition.APPLICABLE_PROPERTIES) {
            if (clazz.isInstance(property)) return true;
        }
        return false;
    }

    public static class ApplicableNumericCondition<P extends RangeSelectItemModelProperty>
            extends NumericCondition<P>
            implements Applicable
    {
        private static final List<Class<?>> APPLICABLE_PROPERTIES = List.of(
                Count.class,
                CustomModelDataProperty.class,
                Damage.class
        );

        private ApplicableNumericCondition(P property, float threshold) {
            super(property, threshold);
        }

        @Override
        public void apply(ItemStack stack) {
            switch (property) {
                case Count prop -> applyCount(stack, prop, value);
                case CustomModelDataProperty prop -> applyCustomModelData(stack, prop, value);
                case Damage prop -> applyDamage(stack, prop, value);
                case null, default -> {}
            }
        }

        private static void applyCount(ItemStack stack, Count property, float value) {
            stack.setCount((int) Math.ceil(value * (property.normalize() ? stack.getMaxStackSize() : 1)));
        }

        private static void applyCustomModelData(ItemStack stack, CustomModelDataProperty property, float value) {
            var exists = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (exists == null) {
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                        setAndFillMissing(new ArrayList<>(), property.index(), value, 0f),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                ));
            } else {
                setAndFillMissing(exists.floats(), property.index(), value, 0f);
            }
        }

        private static void applyDamage(ItemStack stack, Damage property, float value) {
            stack.setDamageValue((int) Math.ceil(value * (property.normalize() ? stack.getMaxDamage() : 1)));
        }
    }
}
