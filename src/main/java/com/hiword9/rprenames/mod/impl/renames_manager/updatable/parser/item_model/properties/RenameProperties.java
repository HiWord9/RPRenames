package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RenameProperties {
    public static final RenameProperties EMPTY = new RenameProperties(Map.of());
    public static final Codec<RenameProperties> CODEC = new PropertiesCodec();

    protected final Map<RenameProperty<?>, Object> values;

    protected RenameProperties(Map<RenameProperty<?>, Object> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(RenameProperty<T> property) {
        return (T) values.getOrDefault(property, property.getDefaultValue());
    }

    public RenameProperties overlay(RenameProperties top) {
        if (top.isEmpty()) return this;

        var values = new LinkedHashMap<>(this.values);
        values.putAll(top.values);
        return new RenameProperties(values);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RenameProperties properties && values.equals(properties.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }

    protected static class PropertiesCodec implements Codec<RenameProperties> {
        @Override
        public <T> DataResult<Pair<RenameProperties, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input)
                    .flatMap(map -> decode(ops, map))
                    .map(properties -> Pair.of(properties, input));
        }

        protected <T> DataResult<RenameProperties> decode(DynamicOps<T> ops, MapLike<T> input) {
            var values = new LinkedHashMap<RenameProperty<?>, Object>();
            var errors = new ArrayList<String>();

            input.entries().forEach(entry -> {
                var key = ops.getStringValue(entry.getFirst()).result().orElse("");
                var id = RenameProperty.parseId(key);
                var property = id == null ? null : RenameProperty.get(id);

                if (property == null) {
                    errors.add("Unknown property " + key);
                } else if (values.containsKey(property)) {
                    errors.add("Duplicate property " + property);
                } else {
                    property.getCodec().parse(ops, entry.getSecond())
                            .ifSuccess(value -> values.put(property, value))
                            .ifError(error -> errors.add("Invalid property " + property + ": " + error.message()));
                }
            });

            var properties = new RenameProperties(values);
            return errors.isEmpty()
                    ? DataResult.success(properties)
                    : DataResult.error(() -> String.join("; ", errors), properties);
        }

        @Override
        public <T> DataResult<T> encode(RenameProperties input, DynamicOps<T> ops, T prefix) {
            var builder = ops.mapBuilder();
            input.values.forEach((property, value) ->
                    builder.add(property.getId().toString(), encodeValue(property, value, ops))
            );
            return builder.build(prefix);
        }

        @SuppressWarnings("unchecked")
        protected static <T, V> DataResult<T> encodeValue(RenameProperty<V> property, Object value, DynamicOps<T> ops) {
            return property.getCodec().encodeStart(ops, (V) value);
        }
    }
}
