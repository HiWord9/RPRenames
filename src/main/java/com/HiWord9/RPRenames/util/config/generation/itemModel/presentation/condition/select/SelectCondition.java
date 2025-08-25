package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.select;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.Condition;
import net.minecraft.client.render.item.property.select.SelectProperty;

import java.util.List;

public abstract class SelectCondition<P extends SelectProperty<V>, V> extends Condition {
    P property;
    List<V> values;

    public static <P extends SelectProperty<V>, V> SelectCondition<P, V> of(P property, List<V> values) {
        return null; // todo implement
    }
}


