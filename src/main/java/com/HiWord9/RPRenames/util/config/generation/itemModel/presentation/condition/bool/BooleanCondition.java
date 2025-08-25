package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.bool;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.Condition;
import net.minecraft.client.render.item.property.bool.BooleanProperty;

public abstract class BooleanCondition<P extends BooleanProperty> extends Condition {
    P property;
    boolean value;

    public static <P extends BooleanProperty> BooleanCondition<P> of(P property, boolean value) {
        return null; // todo implement
    }
}
