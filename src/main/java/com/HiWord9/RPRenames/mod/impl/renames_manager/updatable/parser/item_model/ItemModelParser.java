package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.HiWord9.RPRenames.mod.impl.rename.ItemModelRename;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.Parser;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import com.HiWord9.RPRenames.api.RenamesManager;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemModelParser implements Parser {
    private final Map<Identifier, ItemAsset> itemAssets = new HashMap<>();

    public RenamesManager<? super ItemModelRename> renamesManager;

    public ItemModelParser(RenamesManager<? super ItemModelRename> renamesManager) {
        this.renamesManager = renamesManager;
    }

    public void updateItemAssets(Map<Identifier, ItemAsset> itemAssets) {
        this.itemAssets.clear();
        this.itemAssets.putAll(itemAssets);
    }

    @Override
    public void parse(ResourceManager resourceManager, Profiler profiler) {
        var renameDataList = ItemModelDataExplorer.getListMerged(itemAssets);

        renameDataList.forEach(data -> {
            var name = getName(data.applicableConditions);
            if (name == null) return;

            renamesManager.addRename(bakeRename(data, name));
        });
    }

    private static ItemModelRename bakeRename(ItemModelData itemModelData, String name) {
        return new ItemModelRename(
                itemModelData.applicableConditions,
                name,
                itemModelData.items.toArray(new Item[]{})
        );
    }

    private static String getName(Collection<ItemModelCondition.Applicable> conditions) {
        var renameCondition = getRenameCondition(conditions);
        if (renameCondition == null) return null;
        return renameCondition.value.getFirst().getString();
    }

    private static @Nullable SelectCondition<ComponentSelectProperty<Text>, Text> getRenameCondition(
            Collection<ItemModelCondition.Applicable> conditions
    ) {
        SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition = null;
        for (ItemModelCondition condition : conditions) {
            var candidate = asCustomNameConditionOrNull(condition);
            if (candidate != null) {
                if (renameCondition == null) {
                    renameCondition = candidate;
                } else {
                    // todo handle multiple rename conditions; probably an error
                }
            }
        }
        return renameCondition;
    }

    @SuppressWarnings("unchecked")
    private static SelectCondition<ComponentSelectProperty<Text>, Text> asCustomNameConditionOrNull(
            ItemModelCondition condition
    ) {
        if (condition instanceof SelectCondition<?, ?> select
                && select.property instanceof ComponentSelectProperty<?>(ComponentType<?> componentType)
                && componentType.equals(DataComponentTypes.CUSTOM_NAME)
        ) return (SelectCondition<ComponentSelectProperty<Text>, Text>) select;

        return null;
    }
}
