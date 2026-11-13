package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.impl.rename.ItemModelRename;
import com.hiword9.rprenames.api.ext.renames_manager.parser.Parser;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.hiword9.rprenames.api.core.renames_manager.RenamesManager;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties.RenamePropertiesResolver;
import com.hiword9.rprenames.mod.util.ParserHelper;
import com.hiword9.rprenames.util.Util;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.hiword9.rprenames.util.Util.config;

public class ItemModelParser implements Parser {
    private CompletableFuture<Map<Identifier, ClientItem>> itemAssets = CompletableFuture.completedFuture(Map.of());

    public RenamesManager<? super ItemModelRename> renamesManager;

    public ItemModelParser(RenamesManager<? super ItemModelRename> renamesManager) {
        this.renamesManager = renamesManager;
    }

    public void updateItemAssets(CompletableFuture<Map<Identifier, ClientItem>> itemAssets) {
        this.itemAssets = itemAssets;
    }

    @Override
    public void parse(ResourceManager resourceManager, ProfilerFiller profiler) {
        var itemAssets = getItemAssets();
        if (itemAssets == null) return;

        var propertiesResolver = new RenamePropertiesResolver(resourceManager);
        var ignoredProperties = new LinkedHashMap<SelectCondition<?, ?>, Set<Item>>();
        // same renames of one item are merged from any packs, of different items only from one pack
        var renameDataList = ItemModelData.mergeAllPossible(
                ItemModelDataExplorer.getMapUnmerged(itemAssets).entrySet().stream()
                        .map(entry -> {
                            var pack = getPack(resourceManager, entry.getKey());
                            return ItemModelData.mergeAllPossible(
                                    entry.getValue().stream()
                                            .map(data -> {
                                                var renameCondition = getRenameCondition(data.applicableConditions, false);
                                                collectIgnoredProperties(ignoredProperties, data, renameCondition);
                                                return withRenameData(data.withPack(pack), renameCondition, propertiesResolver);
                                            })
                                            .toList(),
                                    false
                            );
                        })
                        .flatMap(Collection::stream)
                        .toList()
        );

        renameDataList.forEach(data -> {
            var renameCondition = getRenameCondition(data.applicableConditions);
            if (renameCondition == null) return;

            var rename = new ItemModelRename(
                    data.applicableConditions,
                    data.contexts,
                    renameCondition.value,
                    data.properties,
                    data.pack,
                    data.items.toArray(new Item[]{})
            );

            for (var item : data.items) renamesManager.addRename(item, rename);

            if (config().logRenameProperties && !renameCondition.propertiesDefinition.isEmpty()) {
                RPRenames.LOGGER.info(
                        "Rename properties of '{}' in {}: {} -> {}",
                        renameCondition.value.getFirst().getString(),
                        itemsToString(data.items),
                        renameCondition.propertiesDefinition,
                        data.properties
                );
            }
        });

        ignoredProperties.forEach((condition, items) -> RPRenames.LOGGER.warn(
                "Ignoring rename properties outside of custom_name select case {} in {}: {}",
                condition.value,
                itemsToString(items),
                condition.propertiesDefinition
        ));
    }

    private @Nullable Map<Identifier, ClientItem> getItemAssets() {
        try {
            return itemAssets.join();
        } catch (CompletionException | CancellationException e) {
            RPRenames.LOGGER.error("Could not get item assets", e);
            return null;
        }
    }

    private static @Nullable PackLocationInfo getPack(ResourceManager resourceManager, Item item) {
        return resourceManager.getResource(ParserHelper.getItemAssetFile(item))
                .map(resource -> resource.source().location())
                .orElse(null);
    }

    private static ItemModelData withRenameData(
            ItemModelData data,
            @Nullable SelectCondition<?, ?> renameCondition,
            RenamePropertiesResolver propertiesResolver
    ) {
        if (renameCondition == null) return data;
        return data
                .withProperties(propertiesResolver.resolve(renameCondition.propertiesDefinition))
                .withPack(renameCondition.pack == null ? data.pack : renameCondition.pack);
    }

    private static void collectIgnoredProperties(
            Map<SelectCondition<?, ?>, Set<Item>> ignoredProperties,
            ItemModelData data,
            @Nullable SelectCondition<?, ?> renameCondition
    ) {
        var conditions = new ArrayList<ItemModelCondition>(data.applicableConditions);
        data.contexts.forEach(conditions::addAll);
        for (var condition : conditions) {
            if (condition instanceof SelectCondition<?, ?> select
                    && select != renameCondition
                    && !select.propertiesDefinition.isEmpty()
            ) ignoredProperties.computeIfAbsent(select, k -> new LinkedHashSet<>()).addAll(data.items);
        }
    }

    private static @Nullable SelectCondition<ComponentContents<Component>, Component> getRenameCondition(
            Collection<ItemModelCondition.Applicable> conditions
    ) {
        return getRenameCondition(conditions, true);
    }

    private static @Nullable SelectCondition<ComponentContents<Component>, Component> getRenameCondition(
            Collection<ItemModelCondition.Applicable> conditions,
            boolean logging
    ) {
        SelectCondition<ComponentContents<Component>, Component> renameCondition = null;
        for (ItemModelCondition condition : conditions) {
            var candidate = asCustomNameConditionOrNull(condition);
            if (candidate != null) {
                if (renameCondition == null) {
                    renameCondition = candidate;
                } else if (logging) {
                    RPRenames.LOGGER.warn(
                            "Found multiple rename conditions. Already accepted: {}; New: {}",
                            renameCondition.value.toString(),
                            candidate.value.toString()
                    );
                }
            }
        }
        return renameCondition;
    }

    private static String itemsToString(Collection<Item> items) {
        return items.stream().map(Util::idFromItem).collect(Collectors.joining(", ", "[", "]"));
    }

    @SuppressWarnings("unchecked")
    private static SelectCondition<ComponentContents<Component>, Component> asCustomNameConditionOrNull(
            ItemModelCondition condition
    ) {
        if (condition instanceof SelectCondition<?, ?> select
                && select.property instanceof ComponentContents<?>(DataComponentType<?> componentType)
                && componentType.equals(DataComponents.CUSTOM_NAME)
        ) return (SelectCondition<ComponentContents<Component>, Component>) select;

        return null;
    }
}
