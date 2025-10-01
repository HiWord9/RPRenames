package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.numeric.NumericProperty;

public class NumericCondition<P extends NumericProperty> implements ItemModelCondition {
    public final P property;
    public final float threshold;

    private NumericCondition(P property, float threshold) {
        this.property = property;
        this.threshold = threshold;
    }

    public static <P extends NumericProperty> NumericCondition<P> of(
            P property, float threshold
    ) {
        return new NumericCondition<>(property, threshold);
    }
}
