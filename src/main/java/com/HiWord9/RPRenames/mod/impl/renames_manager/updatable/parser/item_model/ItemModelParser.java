package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.HiWord9.RPRenames.mod.impl.rename.ItemModelRename;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.Parser;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.presentation.ItemModelPresentation;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.Condition;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import com.HiWord9.RPRenames.api.RenamesManager;
import com.HiWord9.RPRenames.mod.util.Util;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        var renameDataMap = getIdToRenameInfoMap(itemAssets);
        renameDataMap.forEach((id, renameDataList) -> {
            renameDataList.forEach(renameData -> {
                var rename = bakeRename(renameData, Util.itemFromId(id));
                renamesManager.addRename(rename);
            });
        });
        // todo: merge similar renames for different items
    }

    private static ItemModelRename bakeRename(RenameData renameData, Item... items) {
        return new ItemModelRename(
                renameData.conditions,
                renameData.renameCondition.values().getFirst().getString(),
                items
        );
    }

    private static Map<Identifier, List<RenameData>> getIdToRenameInfoMap(Map<Identifier, ItemAsset> itemAssets) {
        var renameDataMap = new HashMap<Identifier, List<RenameData>>();
        itemAssets.forEach((id, asset) -> {
            var renameConditionsMap = new HashMap<List<Condition>, SelectCondition<ComponentSelectProperty<Text>, Text>>();
            fillRenamesConditionsMap(renameConditionsMap, List.of(), asset.model());
            if (renameConditionsMap.isEmpty()) return;
            var renameDataList = new ArrayList<RenameData>();
            renameConditionsMap.forEach(
                    (conditions, renameCondition) ->
                            renameDataList.add(new RenameData(conditions, renameCondition))
            );
            renameDataMap.put(id, renameDataList);
        });
        return renameDataMap;
    }

    private static void fillRenamesConditionsMap(
            Map<List<Condition>, SelectCondition<ComponentSelectProperty<Text>, Text>> renameConditionsMap,
            List<Condition> conditions,
            ItemModel.Unbaked unbakedModel
    ) {
        var model = ItemModelPresentation.of(unbakedModel);
        if (!model.getCases().isEmpty()) {
            for (var modelCase : model.getCases()) {
                var newConditions = new ArrayList<>(conditions);
                newConditions.add(modelCase.condition());
                fillRenamesConditionsMap(renameConditionsMap, newConditions, modelCase.result());
            }
        } else {
            var renameCondition = getRenameCondition(conditions);
            if (renameCondition != null) {
                renameConditionsMap.put(conditions, renameCondition);
            }
        }
    }

    private static @Nullable SelectCondition<ComponentSelectProperty<Text>, Text> getRenameCondition(
            List<Condition> conditions
    ) {
        SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition = null;
        for (Condition condition : conditions) {
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
    private static @Nullable SelectCondition<ComponentSelectProperty<Text>, Text> asCustomNameConditionOrNull(
            Condition condition
    ) {
        if (condition instanceof SelectCondition<?,?> select) {
            if (select.property() instanceof ComponentSelectProperty<?>(ComponentType<?> componentType)) {
                if (componentType.equals(DataComponentTypes.CUSTOM_NAME)) {
                    return (SelectCondition<ComponentSelectProperty<Text>, Text>) select;
                }
            }
        }
        return null;
    }

    private record RenameData(
            List<Condition> conditions,
            SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition
    ) {}
}
