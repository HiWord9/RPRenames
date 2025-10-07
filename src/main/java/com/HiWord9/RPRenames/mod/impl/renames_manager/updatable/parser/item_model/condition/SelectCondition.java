package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.property.select.*;
import net.minecraft.item.ItemStack;

import java.util.List;

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
            implements ItemModelCondition.Applicable
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
        @SuppressWarnings("unchecked") // todo wrap with try
        public void apply(ItemStack stack) {
            if (property instanceof ComponentSelectProperty<?>) {
                var componentSelectProperty = (ComponentSelectProperty<V>) property;

                if (client().world == null) return;

                var componentType = componentSelectProperty.componentType();
                var componentValue = value.getFirst();

                var contextSwapper = asset.registrySwapper();
                if (contextSwapper != null) {
                    contextSwapper.swapContext(
                            componentSelectProperty.valueCodec(),
                            componentValue,
                            client().world.getRegistryManager()
                    ).ifSuccess(swappedValue ->
                            stack.set(componentType, swappedValue)
                    );
                } else {
                    stack.set(componentType, componentValue);
                }
            } // todo fill for all apply cases
        }
    }
}
