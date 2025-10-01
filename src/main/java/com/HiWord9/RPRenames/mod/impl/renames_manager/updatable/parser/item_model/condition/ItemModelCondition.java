package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import net.minecraft.item.ItemStack;

public interface ItemModelCondition {
    FallbackCondition FALLBACK = new FallbackCondition();
    CompositeCondition COMPOSITE = new CompositeCondition();

    class FallbackCondition implements ItemModelCondition {
        private FallbackCondition() {}
    }

    class CompositeCondition implements ItemModelCondition {
        private CompositeCondition() {}
    }
    
    interface Applicable extends ItemModelCondition {
        default void apply(ItemStack stack) {}
    }
}
