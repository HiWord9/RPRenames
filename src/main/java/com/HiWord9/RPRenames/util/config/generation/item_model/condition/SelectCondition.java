package com.HiWord9.RPRenames.util.config.generation.item_model.condition;

import net.minecraft.client.render.item.property.select.SelectProperty;

import java.util.List;

public record SelectCondition<P extends SelectProperty<V>, V>(
        P property,
        List<V> values
) implements Condition {}
