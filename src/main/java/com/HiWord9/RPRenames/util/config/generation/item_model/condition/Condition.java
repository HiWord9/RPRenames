package com.HiWord9.RPRenames.util.config.generation.item_model.condition;

public interface Condition {
    FallbackCondition FALLBACK = new FallbackCondition();
    CompositeCondition COMPOSITE = new CompositeCondition();

    class FallbackCondition implements Condition {
        private FallbackCondition() {}
    }

    class CompositeCondition implements Condition {
        private CompositeCondition() {}
    }
}
