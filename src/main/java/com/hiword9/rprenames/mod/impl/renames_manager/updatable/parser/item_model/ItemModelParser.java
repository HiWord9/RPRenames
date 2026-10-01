package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.impl.rename.ItemModelRename;
import com.hiword9.rprenames.api.ext.renames_manager.parser.Parser;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.hiword9.rprenames.api.core.renames_manager.RenamesManager;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties.RenamePropertiesResolver;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemModelParser implements Parser {
    private final Map<Identifier, ClientItem> itemAssets = new HashMap<>();

    public RenamesManager<? super ItemModelRename> renamesManager;

    public ItemModelParser(RenamesManager<? super ItemModelRename> renamesManager) {
        this.renamesManager = renamesManager;
    }

    public void updateItemAssets(Map<Identifier, ClientItem> itemAssets) {
        this.itemAssets.clear();
        this.itemAssets.putAll(itemAssets);
    }

    @Override
    public void parse(ResourceManager resourceManager, ProfilerFiller profiler) {
        var propertiesResolver = new RenamePropertiesResolver(resourceManager);
        var renameDataList = ItemModelData.mergeAllPossible(
                ItemModelDataExplorer.getListUnmerged(itemAssets).stream()
                        .map(data -> withRenameProperties(data, propertiesResolver))
                        .toList()
        );
        var ignoredProperties = new HashSet<SelectCondition<?, ?>>();

        renameDataList.forEach(data -> {
            var renameCondition = getRenameCondition(data.applicableConditions);
            collectIgnoredProperties(ignoredProperties, data, renameCondition);
            if (renameCondition == null) return;

            var rename = new ItemModelRename(
                    data.applicableConditions,
                    data.contexts,
                    renameCondition.value,
                    data.properties,
                    data.items.toArray(new Item[]{})
            );

            for (var item : data.items) renamesManager.addRename(item, rename);
        });

        ignoredProperties.forEach(condition -> RPRenames.LOGGER.warn(
                "Ignoring rename properties outside of custom_name select case {}: {}",
                condition.value,
                condition.properties
        ));
    }

    private static ItemModelData withRenameProperties(ItemModelData data, RenamePropertiesResolver propertiesResolver) {
        var renameConditions = getRenameConditions(data.applicableConditions);
        if (renameConditions.isEmpty()) return data;
        return data.withProperties(propertiesResolver.resolve(renameConditions.getFirst().properties));
    }

    private static void collectIgnoredProperties(
            Set<SelectCondition<?, ?>> ignoredProperties,
            ItemModelData data,
            @Nullable SelectCondition<?, ?> renameCondition
    ) {
        var conditions = new ArrayList<ItemModelCondition>(data.applicableConditions);
        data.contexts.forEach(conditions::addAll);
        for (var condition : conditions) {
            if (condition instanceof SelectCondition<?, ?> select
                    && select != renameCondition
                    && !select.properties.isEmpty()
            ) ignoredProperties.add(select);
        }
    }

    private static @Nullable SelectCondition<ComponentContents<Component>, Component> getRenameCondition(
            Collection<ItemModelCondition.Applicable> conditions
    ) {
        var renameConditions = getRenameConditions(conditions);
        if (renameConditions.isEmpty()) return null;

        var renameCondition = renameConditions.getFirst();
        for (var candidate : renameConditions.subList(1, renameConditions.size())) {
            RPRenames.LOGGER.warn(
                    "Found multiple rename conditions. Already accepted: {}; New: {}",
                    renameCondition.value.toString(),
                    candidate.value.toString()
            );
        }
        return renameCondition;
    }

    private static List<SelectCondition<ComponentContents<Component>, Component>> getRenameConditions(
            Collection<ItemModelCondition.Applicable> conditions
    ) {
        var renameConditions = new ArrayList<SelectCondition<ComponentContents<Component>, Component>>();
        for (ItemModelCondition condition : conditions) {
            var candidate = asCustomNameConditionOrNull(condition);
            if (candidate != null) renameConditions.add(candidate);
        }
        return renameConditions;
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
