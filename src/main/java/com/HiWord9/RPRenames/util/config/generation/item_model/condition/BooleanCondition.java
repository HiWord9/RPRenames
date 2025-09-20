package com.HiWord9.RPRenames.util.config.generation.item_model.condition;

import net.minecraft.client.render.item.property.bool.BooleanProperty;

public record BooleanCondition<P extends BooleanProperty>(
        P property,
        boolean value
) implements Condition {}
