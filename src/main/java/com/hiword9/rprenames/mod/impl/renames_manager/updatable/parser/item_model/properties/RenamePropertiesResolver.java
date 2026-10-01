package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties;

import com.hiword9.rprenames.mod.RPRenames;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.PlaceholderLookupProvider;
import net.minecraft.util.StrictJsonParser;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class RenamePropertiesResolver {
    public static final FileToIdConverter TEMPLATES_LISTER = FileToIdConverter.json(RPRenames.MOD_ID + "/properties");

    protected final Map<Identifier, RenamePropertiesDefinition> templates = new HashMap<>();
    protected final Map<Identifier, RenameProperties> resolvedTemplates = new HashMap<>();

    public RenamePropertiesResolver(ResourceManager resourceManager) {
        var ops = new PlaceholderLookupProvider(ClientRegistryLayer.createRegistryAccess().compositeAccess())
                .createSerializationContext(JsonOps.INSTANCE);
        TEMPLATES_LISTER.listMatchingResources(resourceManager).forEach((file, resource) ->
                loadTemplate(ops, file, resource)
        );
    }

    public RenameProperties resolve(RenamePropertiesDefinition definition) {
        return resolve(definition, new ArrayDeque<>());
    }

    protected RenameProperties resolve(RenamePropertiesDefinition definition, Deque<Identifier> chain) {
        var properties = RenameProperties.EMPTY;
        for (var template : definition.templates()) {
            properties = properties.overlay(resolveTemplate(template, chain));
        }
        return properties.overlay(definition.properties());
    }

    protected RenameProperties resolveTemplate(Identifier id, Deque<Identifier> chain) {
        if (chain.contains(id)) {
            RPRenames.LOGGER.warn(
                    "Cyclic rename properties templates: {} -> {}",
                    chain.stream().map(Identifier::toString).collect(Collectors.joining(" -> ")),
                    id
            );
            return RenameProperties.EMPTY;
        }

        var resolved = resolvedTemplates.get(id);
        if (resolved != null) return resolved;

        var template = templates.get(id);
        if (template == null) {
            var file = TEMPLATES_LISTER.idToFile(id);
            RPRenames.LOGGER.warn(
                    "Rename properties template {} not found at assets/{}/{}",
                    id, file.getNamespace(), file.getPath()
            );
            resolved = RenameProperties.EMPTY;
        } else {
            chain.addLast(id);
            resolved = resolve(template, chain);
            chain.removeLast();
        }

        resolvedTemplates.put(id, resolved);
        return resolved;
    }

    protected void loadTemplate(DynamicOps<JsonElement> ops, Identifier file, Resource resource) {
        var id = TEMPLATES_LISTER.fileToId(file);
        try (var reader = resource.openAsReader()) {
            RenamePropertiesDefinition.CODEC.parse(ops, StrictJsonParser.parse(reader))
                    .resultOrPartial(error -> RPRenames.LOGGER.warn(
                            "Invalid rename properties template {} from pack '{}': {}",
                            id, resource.sourcePackId(), error
                    ))
                    .ifPresent(definition -> templates.put(id, definition));
        } catch (Exception e) {
            RPRenames.LOGGER.error("Failed to read rename properties template {} from pack '{}'", id, resource.sourcePackId(), e);
        }
    }
}
