package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

import com.hiword9.rprenames.mod.RPRenames;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.renderer.item.SelectItemModel;

public class SwitchCasePropertiesCodec<T> implements Codec<SelectItemModel.SwitchCase<T>> {
    public static final String KEY = RPRenames.asId("properties").toString();

    protected final Codec<SelectItemModel.SwitchCase<T>> original;

    public SwitchCasePropertiesCodec(Codec<SelectItemModel.SwitchCase<T>> original) {
        this.original = original;
    }

    @Override
    public <I> DataResult<Pair<SelectItemModel.SwitchCase<T>, I>> decode(DynamicOps<I> ops, I input) {
        return original.decode(ops, input)
                .ifSuccess(result -> readProperties(ops, input, result.getFirst()));
    }

    @Override
    public <I> DataResult<I> encode(SelectItemModel.SwitchCase<T> input, DynamicOps<I> ops, I prefix) {
        var properties = RenamePropertiesHolder.of(input).rprenames$getProperties();
        var encoded = original.encode(input, ops, prefix);
        if (properties.isEmpty()) return encoded;

        return encoded.flatMap(map -> RenameProperties.CODEC.encodeStart(ops, properties)
                .flatMap(value -> ops.mergeToMap(map, ops.createString(KEY), value))
        );
    }

    protected <I> void readProperties(DynamicOps<I> ops, I input, SelectItemModel.SwitchCase<T> switchCase) {
        ops.get(input, KEY).result().ifPresent(value -> RenameProperties.CODEC.parse(ops, value)
                .resultOrPartial(error -> RPRenames.LOGGER.warn(
                        "Invalid {} in select case {}: {}",
                        KEY,
                        ops.get(input, "when").result().map(Object::toString).orElse("?"),
                        error
                ))
                .ifPresent(RenamePropertiesHolder.of(switchCase)::rprenames$setProperties)
        );
    }
}
