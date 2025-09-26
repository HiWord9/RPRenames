package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.item.ItemStack;

public interface Condition {
    FallbackCondition FALLBACK = new FallbackCondition();
    CompositeCondition COMPOSITE = new CompositeCondition();

    default void apply(ItemStack stack) {}

    class FallbackCondition implements Condition {
        private FallbackCondition() {}
    }

    class CompositeCondition implements Condition {
        private CompositeCondition() {}
    }
}
