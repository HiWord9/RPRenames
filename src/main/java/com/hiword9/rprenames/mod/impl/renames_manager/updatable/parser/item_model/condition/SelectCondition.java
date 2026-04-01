package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.properties.select.Charge;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.item.properties.select.CustomModelDataProperty;
import net.minecraft.client.renderer.item.properties.select.ItemBlockState;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.select.TrimMaterialProperty;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RegistryContextSwapper;
import net.minecraft.world.item.CrossbowItem.ChargeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import java.util.*;

import static com.hiword9.rprenames.mod.util.Util.*;

public non-sealed class SelectCondition<P extends SelectItemModelProperty<V>, V> extends AbstractPropertyValueCondition<P, List<V>> {
    private SelectCondition(P property, List<V> values) {
        super(property, values);
    }

    public static <P extends SelectItemModelProperty<V>, V> SelectCondition<P, V> of(
            P property, List<V> values, ClientItem asset
    ) {
        return isPropertyApplicable(property)
                ? new ApplicableSelectCondition<>(property, values, asset)
                : new SelectCondition<>(property, values);
    }

    private static <V> boolean isPropertyApplicable(SelectItemModelProperty<V> property) {
        for (var clazz : ApplicableSelectCondition.APPLICABLE_PROPERTIES) {
            if (clazz.isInstance(property)) return true;
        }
        return false;
    }

    public static class ApplicableSelectCondition<P extends SelectItemModelProperty<V>, V>
            extends SelectCondition<P, V>
            implements Applicable
    {
        private static final List<Class<?>> APPLICABLE_PROPERTIES = List.of(
                Charge.class,
                ComponentContents.class,
                CustomModelDataProperty.class,
                ItemBlockState.class,
                TrimMaterialProperty.class
        );

        private final ClientItem asset;

        private ApplicableSelectCondition(P property, List<V> values, ClientItem asset) {
            super(property, values);
            this.asset = asset;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void apply(ItemStack stack) {
            final var swapper = asset.registrySwapper();
            final var val = value.getFirst();
            switch (property) {
                case Charge ignored -> applyChargeType(stack, (ChargeType) val);
                case ComponentContents<?> prop -> applyComponent(
                        stack, (ComponentContents<V>) prop, val, swapper
                );
                case CustomModelDataProperty prop -> applyCustomModelData(stack, prop, (String) val);
                case ItemBlockState prop -> applyItemBlockState(stack, prop, (String) val);
                case TrimMaterialProperty prop -> applyTrimMaterial(
                        stack, prop, (ResourceKey<TrimMaterial>) val, swapper
                );
                case null, default -> {}
            }
        }

        private static void applyChargeType(ItemStack stack, ChargeType type) {
            switch (type) {
                case ChargeType.NONE -> {}
                case ChargeType.ROCKET -> stack.set(
                        DataComponents.CHARGED_PROJECTILES,
                        ChargedProjectiles.ofNonEmpty(List.of(new ItemStack(Items.FIREWORK_ROCKET)))
                );
                case ChargeType.ARROW -> stack.set(
                        DataComponents.CHARGED_PROJECTILES,
                        ChargedProjectiles.ofNonEmpty(List.of(new ItemStack(Items.ARROW)))
                );
            }
        }

        private static <V> void applyComponent(
                ItemStack stack,
                ComponentContents<V> property,
                V value,
                RegistryContextSwapper contextSwapper
        ) {
            if (client().level == null) return;

            var componentType = property.componentType();

            if (contextSwapper != null) {
                contextSwapper.swapTo(
                        property.valueCodec(),
                        value,
                        client().level.registryAccess()
                ).ifSuccess(swappedValue ->
                        stack.set(componentType, swappedValue)
                );
            } else {
                stack.set(componentType, value);
            }
        }

        private static void applyCustomModelData(ItemStack stack, CustomModelDataProperty prop, String value) {
            var exists = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (exists == null) {
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                        new ArrayList<>(),
                        new ArrayList<>(),
                        setAndFillMissing(new ArrayList<>(), prop.index(), value, ""),
                        new ArrayList<>()
                ));
            } else {
                setAndFillMissing(exists.strings(), prop.index(), value, "");
            }
        }

        private static void applyItemBlockState(ItemStack stack, ItemBlockState prop, String value) {
            var exists = stack.get(DataComponents.BLOCK_STATE);
            if (exists == null) {
                stack.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(
                        new HashMap<>(Map.of(prop.property(), value))
                ));
            } else {
                exists.properties().put(prop.property(), value);
            }
        }

        private static void applyTrimMaterial(
                ItemStack stack,
                TrimMaterialProperty prop,
                ResourceKey<TrimMaterial> value,
                RegistryContextSwapper contextSwapper
        ) {
            if (client().level == null || contextSwapper == null) return;

            contextSwapper.swapTo(
                    prop.valueCodec(),
                    value,
                    client().level.registryAccess()
            ).ifSuccess(swappedValue -> {
                var optMaterial = client().level.registryAccess().get(swappedValue);
                if (optMaterial.isEmpty()) return;

                var material = optMaterial.get();

                var exists = stack.get(DataComponents.TRIM);
                Holder<TrimPattern> pattern;

                if (exists == null) {
                    var optPattern = client()
                            .level.registryAccess()
                            .get(TrimPatterns.SENTRY); // todo optional ?
                    if (optPattern.isEmpty()) return;
                    pattern = optPattern.get();
                } else {
                    pattern = exists.pattern();
                }

                stack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
            });
        }
    }
}
