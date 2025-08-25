package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.BooleanCondition;
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
