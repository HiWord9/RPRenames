package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.numeric;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.Condition;
import net.minecraft.client.render.item.property.numeric.NumericProperty;

public abstract class NumericCondition<P extends NumericProperty> extends Condition {
    P property;
    float threshold;

    public static <P extends NumericProperty> NumericCondition<P> of(P property, float threshold) {
        return null; // todo implement
    }
}
