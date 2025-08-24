package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.Condition;
import net.minecraft.client.render.item.model.ConditionItemModel;

public class ConditionPresentation extends ItemModelPresentation {
    public ConditionPresentation(ConditionItemModel.Unbaked unbakedModel) {
        String property = unbakedModel.property().toString();

        // todo define conditions
        cases.add(new Case(new Condition(property + "[true]"), unbakedModel.onTrue()));
        cases.add(new Case(new Condition(property + "[false]"), unbakedModel.onFalse()));
    }
}
