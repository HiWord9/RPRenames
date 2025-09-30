package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.numeric.NumericProperty;

public record NumericCondition<P extends NumericProperty>(
        P property,
        float threshold
) implements ItemModelCondition {}
