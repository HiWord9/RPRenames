package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.bool.BooleanProperty;

public record BooleanCondition<P extends BooleanProperty>(
        P property,
        boolean value
) implements Condition {}
