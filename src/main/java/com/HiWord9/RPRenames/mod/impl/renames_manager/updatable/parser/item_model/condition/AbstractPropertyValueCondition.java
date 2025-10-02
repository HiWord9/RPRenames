package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition;

import java.util.Objects;

public sealed abstract class AbstractPropertyValueCondition<P, V>
        implements ItemModelCondition
        permits BooleanCondition, NumericCondition, SelectCondition
{
    public final P property;
    public final V value;

    public AbstractPropertyValueCondition(P property, V value) {
        this.property = property;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        // no need to check instanceof in each subclass,
        // cause subclasses themselves divide properties in separate logical groups
        return o instanceof AbstractPropertyValueCondition<?, ?> that
                && Objects.equals(property, that.property)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, value);
    }
}
