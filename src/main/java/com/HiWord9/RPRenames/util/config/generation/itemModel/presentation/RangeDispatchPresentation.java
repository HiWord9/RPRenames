package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.Condition;
import net.minecraft.client.render.item.model.RangeDispatchItemModel;

public class RangeDispatchPresentation extends ItemModelPresentation {
    public RangeDispatchPresentation(RangeDispatchItemModel.Unbaked unbakedModel) {
        String property = unbakedModel.property().toString();

        // todo define range properties
        for (RangeDispatchItemModel.Entry entry : unbakedModel.entries()) {
            cases.add(new Case(
                    new Condition(property + "[%s]".formatted(entry.threshold())),
                    entry.model())
            );
        }

        addFallback(unbakedModel.fallback().orElse(null));
    }
}
