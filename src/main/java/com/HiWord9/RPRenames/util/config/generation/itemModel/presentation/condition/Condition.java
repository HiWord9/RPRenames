package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition;

public abstract class Condition {
    public static final FallbackCondition FALLBACK = new FallbackCondition();
    public static final CompositeCondition COMPOSITE = new CompositeCondition();

    public static class FallbackCondition extends Condition {
        private FallbackCondition() {}
    }

    public static class CompositeCondition extends Condition {
        private CompositeCondition() {}
    }
}
