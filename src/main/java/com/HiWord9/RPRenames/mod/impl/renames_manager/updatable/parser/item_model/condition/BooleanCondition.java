package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.client.render.item.property.bool.BooleanProperty;

public class BooleanCondition<P extends BooleanProperty> implements ItemModelCondition {
    public final P property;
    public final boolean value;

    private BooleanCondition(P property, boolean value) {
        this.property = property;
        this.value = value;
    }

    public static <P extends BooleanProperty> BooleanCondition<P> of(
            P property, boolean value
    ) {
        return new BooleanCondition<>(property, value);
    }
}
