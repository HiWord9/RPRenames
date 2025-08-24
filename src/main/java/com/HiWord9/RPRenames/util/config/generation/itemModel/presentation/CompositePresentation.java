package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.Condition;
import net.minecraft.client.render.item.model.CompositeItemModel;
import net.minecraft.client.render.item.model.ItemModel;

public class CompositePresentation extends ItemModelPresentation {
    public CompositePresentation(CompositeItemModel.Unbaked unbakedModel) {
        for (ItemModel.Unbaked model : unbakedModel.models()) {
            cases.add(new Case(new Condition("composite"), model));
        }
    }
}
