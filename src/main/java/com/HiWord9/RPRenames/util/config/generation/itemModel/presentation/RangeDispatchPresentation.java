package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.numeric.NumericCondition;
import net.minecraft.client.render.item.model.RangeDispatchItemModel;

public class RangeDispatchPresentation extends ItemModelPresentation {
    public RangeDispatchPresentation(RangeDispatchItemModel.Unbaked unbakedModel) {
        for (RangeDispatchItemModel.Entry entry : unbakedModel.entries()) {
            cases.add(new Case(
                    NumericCondition.of(unbakedModel.property(), entry.threshold()),
                    entry.model()
            ));
        }

        addFallback(unbakedModel.fallback().orElse(null));
    }
}
