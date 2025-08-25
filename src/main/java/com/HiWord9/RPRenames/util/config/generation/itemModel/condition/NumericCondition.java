package com.HiWord9.RPRenames.util.config.generation.itemModel.condition;

import net.minecraft.client.render.item.property.numeric.NumericProperty;

public record NumericCondition<P extends NumericProperty>(
        P property,
        float threshold
) implements Condition {}
