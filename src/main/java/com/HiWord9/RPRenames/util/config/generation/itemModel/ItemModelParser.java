package com.HiWord9.RPRenames.util.config.generation.itemModel;

import com.HiWord9.RPRenames.util.config.generation.Parser;
import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.ItemModelPresentation;
import com.HiWord9.RPRenames.util.config.generation.itemModel.condition.Condition;
import com.HiWord9.RPRenames.util.config.generation.itemModel.condition.SelectCondition;
import com.HiWord9.RPRenames.util.rename.RenamesManager;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.property.select.ComponentSelectProperty;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
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

    public RenamesManager renamesManager;

    public ItemModelParser(RenamesManager renamesManager) {
        this.renamesManager = renamesManager;
    }

    public void updateItemAssets(Map<Identifier, ItemAsset> itemAssets) {
        this.itemAssets.clear();
        this.itemAssets.putAll(itemAssets);
    }

    @Override
    public void parse(ResourceManager resourceManager, Profiler profiler) {
        var renamesMap = getIdToRenameInfoMap(itemAssets);
        // todo: implement renamesMap to renames parsing
    }

    private static Map<Identifier, List<RenameInfo>> getIdToRenameInfoMap(Map<Identifier, ItemAsset> itemAssets) {
        var renamesMap = new HashMap<Identifier, List<RenameInfo>>();
        itemAssets.forEach((id, asset) -> {
            var renameConditionsMap = new HashMap<List<Condition>, SelectCondition<ComponentSelectProperty<Text>, Text>>();
            fillRenamesConditionsMap(renameConditionsMap, List.of(), asset.model());
            if (renameConditionsMap.isEmpty()) return;
            var renameInfoList = new ArrayList<RenameInfo>();
            renameConditionsMap.forEach(
                    (conditions, renameCondition) ->
                            renameInfoList.add(new RenameInfo(conditions, renameCondition))
            );
            renamesMap.put(id, renameInfoList);
        });
        return renamesMap;
    }

    private static void fillRenamesConditionsMap(
            Map<List<Condition>, SelectCondition<ComponentSelectProperty<Text>, Text>> renamesMap,
            List<Condition> conditions,
            ItemModel.Unbaked unbakedModel
    ) {
        var model = ItemModelPresentation.of(unbakedModel);
        if (!model.getCases().isEmpty()) {
            for (var modelCase : model.getCases()) {
                var newConditions = new ArrayList<>(conditions);
                newConditions.add(modelCase.condition());
                fillRenamesConditionsMap(renamesMap, newConditions, modelCase.result());
            }
        } else {
            var renameCondition = getRenameCondition(conditions);
            if (renameCondition != null) {
                renamesMap.put(conditions, renameCondition);
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

    private record RenameInfo(
            List<Condition> conditions,
            SelectCondition<ComponentSelectProperty<Text>, Text> renameCondition
    ) {}
}
