package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import java.util.List;

public record RenamePropertiesDefinition(List<Identifier> templates, RenameProperties properties) {
    public static final String EXTENDS_KEY = "extends";
    public static final RenamePropertiesDefinition EMPTY = new RenamePropertiesDefinition(List.of(), RenameProperties.EMPTY);

    public static final Codec<List<Identifier>> TEMPLATES_CODEC = ExtraCodecs.compactListCodec(
            Codec.STRING.comapFlatMap(RenamePropertiesDefinition::readTemplateId, Identifier::toString)
    );
    public static final Codec<RenamePropertiesDefinition> CODEC = new DefinitionCodec();

    public boolean isEmpty() {
        return templates.isEmpty() && properties.isEmpty();
    }

    private static DataResult<Identifier> readTemplateId(String id) {
        return id.indexOf(Identifier.NAMESPACE_SEPARATOR) < 0
                ? DataResult.error(() -> "Template reference " + id + " has no namespace")
                : Identifier.read(id);
    }

    private static class DefinitionCodec implements Codec<RenamePropertiesDefinition> {
        @Override
        public <T> DataResult<Pair<RenamePropertiesDefinition, T>> decode(DynamicOps<T> ops, T input) {
            var definition = ops.getMap(input).isSuccess()
                    ? decodeObject(ops, input)
                    : TEMPLATES_CODEC.parse(ops, input).map(templates -> new RenamePropertiesDefinition(templates, RenameProperties.EMPTY));
            return definition.map(result -> Pair.of(result, input));
        }

        private <T> DataResult<RenamePropertiesDefinition> decodeObject(DynamicOps<T> ops, T input) {
            var templates = ops.get(input, EXTENDS_KEY).result()
                    .map(value -> TEMPLATES_CODEC.parse(ops, value))
                    .map(result -> result.hasResultOrPartial() ? result : result.setPartial(List.of()))
                    .orElseGet(() -> DataResult.success(List.of()));
            var properties = RenameProperties.CODEC.parse(ops, ops.remove(input, EXTENDS_KEY));
            return templates.apply2stable(RenamePropertiesDefinition::new, properties);
        }

        @Override
        public <T> DataResult<T> encode(RenamePropertiesDefinition input, DynamicOps<T> ops, T prefix) {
            var encoded = RenameProperties.CODEC.encode(input.properties(), ops, prefix);
            if (input.templates().isEmpty()) return encoded;

            return encoded.flatMap(map -> TEMPLATES_CODEC.encodeStart(ops, input.templates())
                    .flatMap(templates -> ops.mergeToMap(map, ops.createString(EXTENDS_KEY), templates))
            );
        }
    }
}
