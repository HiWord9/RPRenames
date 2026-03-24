package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.impl.rename.ItemModelRename;
import com.hiword9.rprenames.api.ext.renames_manager.parser.Parser;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.hiword9.rprenames.api.core.renames_manager.RenamesManager;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
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
        var renameDataList = ItemModelDataExplorer.getListMerged(itemAssets);

        renameDataList.forEach(data -> {
            var name = getName(data.applicableConditions);
            if (name == null) return;

            var rename = new ItemModelRename(
                    data.applicableConditions,
                    data.contexts,
                    name,
                    data.items.toArray(new Item[]{})
            );

            for (var item : data.items) renamesManager.addRename(item, rename);
        });
    }

    private static Component getName(Collection<ItemModelCondition.Applicable> conditions) {
        var renameCondition = getRenameCondition(conditions);
        if (renameCondition == null) return null;
        return renameCondition.value.getFirst();
    }

    private static @Nullable SelectCondition<ComponentContents<Component>, Component> getRenameCondition(
            Collection<ItemModelCondition.Applicable> conditions
    ) {
        SelectCondition<ComponentContents<Component>, Component> renameCondition = null;
        for (ItemModelCondition condition : conditions) {
            var candidate = asCustomNameConditionOrNull(condition);
            if (candidate != null) {
                if (renameCondition == null) {
                    renameCondition = candidate;
                } else {
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
