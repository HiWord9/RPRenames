package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.client.renderer.item.properties.conditional.Broken;
import net.minecraft.client.renderer.item.properties.conditional.ComponentMatches;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.conditional.CustomModelDataProperty;
import net.minecraft.client.renderer.item.properties.conditional.Damaged;
import net.minecraft.client.renderer.item.properties.conditional.HasComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.CustomDataPredicate;
import net.minecraft.core.component.predicates.DamagePredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import java.util.ArrayList;
import java.util.List;

import static com.hiword9.rprenames.util.Util.setAndFillMissing;

public non-sealed class BooleanCondition<P extends ConditionalItemModelProperty> extends AbstractPropertyValueCondition<P, Boolean> {
    private BooleanCondition(P property, boolean value) {
        super(property, value);
    }

    public static <P extends ConditionalItemModelProperty> BooleanCondition<P> of(
            P property, boolean value
    ) {
        return isPropertyApplicable(property)
                ? new ApplicableBooleanCondition<>(property, value)
                : new BooleanCondition<>(property, value);
    }

    private static boolean isPropertyApplicable(ConditionalItemModelProperty property) {
        for (var clazz : ApplicableBooleanCondition.APPLICABLE_PROPERTIES) {
            if (clazz.isInstance(property)) return true;
        }
        return false;
    }

    public static class ApplicableBooleanCondition<P extends ConditionalItemModelProperty>
            extends BooleanCondition<P>
            implements Applicable
    {
        private static final List<Class<?>> APPLICABLE_PROPERTIES = List.of(
                Broken.class,
                ComponentMatches.class,
                CustomModelDataProperty.class,
                Damaged.class,
                HasComponent.class
        );

        private ApplicableBooleanCondition(P property, boolean value) {
            super(property, value);
        }

        @Override
        public void apply(ItemStack stack) {
            switch (property) {
                case Broken prop -> applyBroken(stack, value);
                case ComponentMatches prop -> applyComponentBoolean(stack, prop, value);
                case CustomModelDataProperty prop -> applyCustomModelData(stack, prop, value);
                case Damaged prop -> applyDamaged(stack, value);
                case HasComponent prop -> applyHasComponent(stack, prop, value);
                case null, default -> {}
            }
        }

        private static void applyBroken(ItemStack stack, boolean value) {
            if (!stack.isDamageableItem()) return;
            stack.setDamageValue(value ? stack.getMaxDamage() - 1 : 0);
        }

        private static void applyComponentBoolean(ItemStack stack, ComponentMatches property, boolean value) {
            switch (property.predicate().predicate()) {
                case CustomDataPredicate predicate -> applyCustomDataPredicate(stack, predicate, value);
                case DamagePredicate predicate -> applyDamagePredicate(stack, predicate, value);
                case SingleComponentItemPredicate<?> predicate -> {} // todo implement for ComponentSubPredicate
                case null, default -> {}
            }
        }

        private static void applyCustomDataPredicate(ItemStack stack, CustomDataPredicate predicate, boolean value) {
            var exists = stack.get(DataComponents.CUSTOM_DATA);
            if (value && predicate.value().tag() != null) {
                if (exists == null) {
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    exists = stack.get(DataComponents.CUSTOM_DATA);
                    assert exists != null;
                }
                stack.set(
                        DataComponents.CUSTOM_DATA,
                        CustomData.of(exists.copyTag().merge(predicate.value().tag()))
                );
            } else if (predicate.matches(stack)) {
                // todo need to brake equality here somehow
            }
        }

        private static void applyDamagePredicate(ItemStack stack, DamagePredicate predicate, boolean value) {
            var maxDamageComponent = stack.getMaxDamage();

            var damageRangeFirst = predicate.damage();
            var damageRangeSecond = MinMaxBounds.Ints.between(
                    maxDamageComponent - predicate.durability().bounds().max().orElse(maxDamageComponent),
                    maxDamageComponent - predicate.durability().bounds().min().orElse(0)
            );

            var passRange = MinMaxBounds.Ints.between(
                    Math.max(
                            damageRangeFirst.bounds().min().orElse(0),
                            damageRangeSecond.bounds().min().orElse(0)
                    ),
                    Math.min(
                            damageRangeFirst.bounds().max().orElse(maxDamageComponent),
                            damageRangeSecond.bounds().max().orElse(maxDamageComponent)
                    )
            );

            if (value) {
                stack.setDamageValue(passRange.bounds().min().orElse(0));
            } else if (predicate.matches(stack)) {
                stack.setDamageValue(passRange.bounds().min().orElse(0) != 0
                        ? 0
                        : passRange.bounds().max().orElse(maxDamageComponent) + 1
                );
            }
        }

        private static void applyCustomModelData(ItemStack stack, CustomModelDataProperty property, boolean value) {
            var exists = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (exists == null) {
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
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
            if (!stack.isDamageableItem()) return;
            stack.setDamageValue(value ? 1 : 0);
        }

        private static void applyHasComponent(ItemStack stack, HasComponent property, boolean value) {
            // todo account for ignoreDefault
            if (stack.has(property.componentType()) == value) return;

            if (value) {
                // todo set empty component
            } else {
                stack.remove(property.componentType());
            }
        }
    }
}
