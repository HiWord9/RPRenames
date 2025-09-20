package com.HiWord9.RPRenames.util.config.generation.item_model.presentation;

import com.HiWord9.RPRenames.util.config.generation.item_model.condition.NumericCondition;
import net.minecraft.client.render.item.model.RangeDispatchItemModel;

public class RangeDispatchPresentation extends ItemModelPresentation {
    public RangeDispatchPresentation(RangeDispatchItemModel.Unbaked unbakedModel) {
        for (RangeDispatchItemModel.Entry entry : unbakedModel.entries()) {
            cases.add(new Case(
                    new NumericCondition(unbakedModel.property(), entry.threshold()),
                    entry.model()
            ));
        }

        addFallback(unbakedModel.fallback().orElse(null));
    }
}
