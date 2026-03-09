package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.bool.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.component.ComponentSubPredicate;
import net.minecraft.predicate.component.CustomDataPredicate;
import net.minecraft.predicate.item.DamagePredicate;

import java.util.ArrayList;
import java.util.List;

import static com.hiword9.rprenames.mod.util.Util.setAndFillMissing;

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
            switch (property) {
                case BrokenProperty prop -> applyBroken(stack, value);
                case ComponentBooleanProperty prop -> applyComponentBoolean(stack, prop, value);
                case CustomModelDataFlagProperty prop -> applyCustomModelData(stack, prop, value);
                case DamagedProperty prop -> applyDamaged(stack, value);
                case HasComponentProperty prop -> applyHasComponent(stack, prop, value);
                case null, default -> {}
            }
        }

        private static void applyBroken(ItemStack stack, boolean value) {
            if (!stack.isDamageable()) return;
            stack.setDamage(value ? stack.getMaxDamage() - 1 : 0);
        }

        private static void applyComponentBoolean(ItemStack stack, ComponentBooleanProperty property, boolean value) {
            switch (property.predicate().predicate()) {
                case CustomDataPredicate predicate -> applyCustomDataPredicate(stack, predicate, value);
                case DamagePredicate predicate -> applyDamagePredicate(stack, predicate, value);
                case ComponentSubPredicate<?> predicate -> {} // todo implement for ComponentSubPredicate
                case null, default -> {}
            }
        }

        private static void applyCustomDataPredicate(ItemStack stack, CustomDataPredicate predicate, boolean value) {
            var exists = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (value && predicate.value().nbt() != null) {
                if (exists == null) {
                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                    exists = stack.get(DataComponentTypes.CUSTOM_DATA);
                    assert exists != null;
                }
                stack.set(
                        DataComponentTypes.CUSTOM_DATA,
                        NbtComponent.of(exists.copyNbt().copyFrom(predicate.value().nbt()))
                );
            } else if (predicate.test(stack)) {
                // todo need to brake equality here somehow
            }
        }

        private static void applyDamagePredicate(ItemStack stack, DamagePredicate predicate, boolean value) {
            var maxDamageComponent = stack.getMaxDamage();

            var damageRangeFirst = predicate.damage();
            var damageRangeSecond = NumberRange.IntRange.between(
                    maxDamageComponent - predicate.durability().max().orElse(maxDamageComponent),
                    maxDamageComponent - predicate.durability().min().orElse(0)
            );

            var passRange = NumberRange.IntRange.between(
                    Math.max(
                            damageRangeFirst.min().orElse(0),
                            damageRangeSecond.min().orElse(0)
                    ),
                    Math.min(
                            damageRangeFirst.max().orElse(maxDamageComponent),
                            damageRangeSecond.max().orElse(maxDamageComponent)
                    )
            );

            if (value) {
                stack.setDamage(passRange.min().orElse(0));
            } else if (predicate.test(stack)) {
                stack.setDamage(passRange.min().orElse(0) != 0
                        ? 0
                        : passRange.max().orElse(maxDamageComponent) + 1
                );
            }
        }

        private static void applyCustomModelData(ItemStack stack, CustomModelDataFlagProperty property, boolean value) {
            var exists = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (exists == null) {
                stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                        new ArrayList<>(),
                        setAndFillMissing(new ArrayList<>(), property.index(), value, false),
                        new ArrayList<>(),
                        new ArrayList<>()
                ));
            } else {
                setAndFillMissing(exists.flags(), property.index(), value, false);
            }
        }

        private static void applyDamaged(ItemStack stack, boolean value) {
            if (!stack.isDamageable()) return;
            stack.setDamage(value ? 1 : 0);
        }

        private static void applyHasComponent(ItemStack stack, HasComponentProperty property, boolean value) {
            // todo account for ignoreDefault
            if (stack.contains(property.componentType()) == value) return;

            if (value) {
                // todo set empty component
            } else {
                stack.remove(property.componentType());
            }
        }
    }
}
