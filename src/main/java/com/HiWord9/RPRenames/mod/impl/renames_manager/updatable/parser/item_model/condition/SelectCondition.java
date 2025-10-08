package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.property.select.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.CrossbowItem.ChargeType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.item.equipment.trim.ArmorTrimPatterns;
import net.minecraft.registry.ContextSwapper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.*;

import static com.HiWord9.RPRenames.mod.util.Util.*;

public non-sealed class SelectCondition<P extends SelectProperty<V>, V> extends AbstractPropertyValueCondition<P, List<V>> {
    private SelectCondition(P property, List<V> values) {
        super(property, values);
    }

    public static <P extends SelectProperty<V>, V> SelectCondition<P, V> of(
            P property, List<V> values, ItemAsset asset
    ) {
        return isPropertyApplicable(property)
                ? new ApplicableSelectCondition<>(property, values, asset)
                : new SelectCondition<>(property, values);
    }

    private static <V> boolean isPropertyApplicable(SelectProperty<V> property) {
        for (var clazz : ApplicableSelectCondition.APPLICABLE_PROPERTIES) {
            if (clazz.isInstance(property)) return true;
        }
        return false;
    }

    public static class ApplicableSelectCondition<P extends SelectProperty<V>, V>
            extends SelectCondition<P, V>
            implements Applicable
    {
        private static final List<Class<?>> APPLICABLE_PROPERTIES = List.of(
                ChargeTypeProperty.class,
                ComponentSelectProperty.class,
                CustomModelDataStringProperty.class,
                ItemBlockStateProperty.class,
                TrimMaterialProperty.class
        );

        private final ItemAsset asset;

        private ApplicableSelectCondition(P property, List<V> values, ItemAsset asset) {
            super(property, values);
            this.asset = asset;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void apply(ItemStack stack) {
            final var swapper = asset.registrySwapper();
            final var val = value.getFirst();
            switch (property) {
                case ChargeTypeProperty ignored -> applyChargeType(stack, (ChargeType) val);
                case ComponentSelectProperty<?> prop -> applyComponent(
                        stack, (ComponentSelectProperty<V>) prop, val, swapper
                );
                case CustomModelDataStringProperty prop -> applyCustomModelData(stack, prop, (String) val);
                case ItemBlockStateProperty prop -> applyItemBlockState(stack, prop, (String) val);
                case TrimMaterialProperty prop -> applyTrimMaterial(
                        stack, prop, (RegistryKey<ArmorTrimMaterial>) val, swapper
                );
                case null, default -> {}
            }
        }

        private static void applyChargeType(ItemStack stack, ChargeType type) {
            switch (type) {
                case ChargeType.NONE -> {}
                case ChargeType.ROCKET -> stack.set(
                        DataComponentTypes.CHARGED_PROJECTILES,
                        ChargedProjectilesComponent.of(new ItemStack(Items.FIREWORK_ROCKET))
                );
                case ChargeType.ARROW -> stack.set(
                        DataComponentTypes.CHARGED_PROJECTILES,
                        ChargedProjectilesComponent.of(new ItemStack(Items.ARROW))
                );
            }
        }

        private static <V> void applyComponent(
                ItemStack stack,
                ComponentSelectProperty<V> property,
                V value,
                ContextSwapper contextSwapper
        ) {
            if (client().world == null) return;

            var componentType = property.componentType();

            if (contextSwapper != null) {
                contextSwapper.swapContext(
                        property.valueCodec(),
                        value,
                        client().world.getRegistryManager()
                ).ifSuccess(swappedValue ->
                        stack.set(componentType, swappedValue)
                );
            } else {
                stack.set(componentType, value);
            }
        }

        private static void applyCustomModelData(ItemStack stack, CustomModelDataStringProperty prop, String value) {
            var exists = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (exists == null) {
                stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(
                        new ArrayList<>(),
                        new ArrayList<>(),
                        setAndNullMissing(new ArrayList<>(), prop.index(), value, ""),
                        new ArrayList<>()
                ));
            } else {
                setAndNullMissing(exists.strings(), prop.index(), value, "");
            }
        }

        private static void applyItemBlockState(ItemStack stack, ItemBlockStateProperty prop, String value) {
            var exists = stack.get(DataComponentTypes.BLOCK_STATE);
            if (exists == null) {
                stack.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(
                        new HashMap<>(Map.of(prop.property(), value))
                ));
            } else {
                exists.properties().put(prop.property(), value);
            }
        }

        private static void applyTrimMaterial(
                ItemStack stack,
                TrimMaterialProperty prop,
                RegistryKey<ArmorTrimMaterial> value,
                ContextSwapper contextSwapper
        ) {
            if (client().world == null || contextSwapper == null) return;

            contextSwapper.swapContext(
                    prop.valueCodec(),
                    value,
                    client().world.getRegistryManager()
            ).ifSuccess(swappedValue -> {
                var optMaterial = client().world.getRegistryManager().getOptionalEntry(swappedValue);
                if (optMaterial.isEmpty()) return;

                var material = optMaterial.get();

                var exists = stack.get(DataComponentTypes.TRIM);
                RegistryEntry<ArmorTrimPattern> pattern;

                if (exists == null) {
                    var optPattern = client()
                            .world.getRegistryManager()
                            .getOptionalEntry(ArmorTrimPatterns.SENTRY); // todo optional ?
                    if (optPattern.isEmpty()) return;
                    pattern = optPattern.get();
                } else {
                    pattern = exists.pattern();
                }

                stack.set(DataComponentTypes.TRIM, new ArmorTrim(material, pattern));
            });
        }
    }
}
