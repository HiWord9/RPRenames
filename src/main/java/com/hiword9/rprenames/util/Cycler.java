package com.hiword9.rprenames.util;

import java.util.List;

public class Cycler<T> {
    protected final List<T> values;
    protected int index = 0;

    public Cycler(List<T> values) {
        if (values.isEmpty()) throw new IllegalArgumentException("Cycler must have at least one value");
        this.values = List.copyOf(values);
    }

    public T current() {
        return values.get(index);
    }

    public int index() {
        return index;
    }

    public int size() {
        return values.size();
    }

    public T next() {
        return moveTo(index + 1);
    }

    public T previous() {
        return moveTo(index - 1);
    }

    public T first() {
        return moveTo(0);
    }

    public T last() {
        return moveTo(values.size() - 1);
    }

    public boolean select(T value) {
        int i = values.indexOf(value);
        if (i == -1) return false;
        index = i;
        return true;
    }

    protected T moveTo(int index) {
        this.index = Math.floorMod(index, values.size());
        return current();
    }
}
