package com.HiWord9.RPRenames.util.config.generation.item_model.presentation;

import com.HiWord9.RPRenames.util.config.generation.item_model.condition.BooleanCondition;
import net.minecraft.client.render.item.model.ConditionItemModel;

public class ConditionPresentation extends ItemModelPresentation {
    public ConditionPresentation(ConditionItemModel.Unbaked unbakedModel) {
        cases.add(new Case(
                new BooleanCondition(unbakedModel.property(), true),
                unbakedModel.onTrue()
        ));
        cases.add(new Case(
                new BooleanCondition(unbakedModel.property(), false),
                unbakedModel.onFalse()
        ));
    }
}
