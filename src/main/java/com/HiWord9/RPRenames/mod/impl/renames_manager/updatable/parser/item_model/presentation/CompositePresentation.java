package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.presentation;

import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.Condition;
import net.minecraft.client.render.item.model.CompositeItemModel;
import net.minecraft.client.render.item.model.ItemModel;

public class CompositePresentation extends ItemModelPresentation {
    public CompositePresentation(CompositeItemModel.Unbaked unbakedModel) {
        for (ItemModel.Unbaked model : unbakedModel.models()) {
            cases.add(new Case(Condition.COMPOSITE, model));
        }
    }
}
