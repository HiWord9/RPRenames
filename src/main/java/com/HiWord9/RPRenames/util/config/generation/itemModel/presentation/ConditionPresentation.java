package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation;

import net.minecraft.client.render.item.model.ConditionItemModel;

public class ConditionPresentation extends ItemModelPresentation {
    public ConditionPresentation(ConditionItemModel.Unbaked unbakedModel) {
        String property = unbakedModel.property().toString();

        // todo define conditions
        cases.add(new Case(new Case.Condition(property + "[true]"), unbakedModel.onTrue()));
        cases.add(new Case(new Case.Condition(property + "[false]"), unbakedModel.onFalse()));
    }
}
