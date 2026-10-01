package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

import com.hiword9.rprenames.mod.RPRenames;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class RenameProperty<T> {
    private static final Map<Identifier, RenameProperty<?>> REGISTRY = new HashMap<>();

    protected final Identifier id;
    protected final Codec<T> codec;
    protected final T defaultValue;

    protected RenameProperty(Identifier id, Codec<T> codec, T defaultValue) {
        this.id = id;
        this.codec = codec;
        this.defaultValue = defaultValue;
    }

    public static <T> RenameProperty<T> register(Identifier id, Codec<T> codec, T defaultValue) {
        var property = new RenameProperty<>(id, codec, defaultValue);
        if (REGISTRY.putIfAbsent(id, property) != null) {
            throw new IllegalArgumentException("Rename property " + id + " is already registered");
        }
        return property;
    }

    public static @Nullable RenameProperty<?> get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static @Nullable Identifier parseId(String id) {
        return id.indexOf(Identifier.NAMESPACE_SEPARATOR) < 0
                ? Identifier.tryBuild(RPRenames.MOD_ID, id)
                : Identifier.tryParse(id);
    }

    public Identifier getId() {
        return id;
    }

    public Codec<T> getCodec() {
        return codec;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
