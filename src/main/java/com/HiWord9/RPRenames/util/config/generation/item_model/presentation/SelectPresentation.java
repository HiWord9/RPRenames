package com.HiWord9.RPRenames.util.config.generation.item_model.presentation;

import com.HiWord9.RPRenames.util.config.generation.item_model.condition.SelectCondition;
import net.minecraft.client.render.item.model.SelectItemModel;
import net.minecraft.client.render.item.property.select.SelectProperty;

public class SelectPresentation extends ItemModelPresentation {
    public SelectPresentation(SelectItemModel.Unbaked unbakedModel) {
        addSwitchCases(unbakedModel.unbakedSwitch());
        addFallback(unbakedModel.fallback().orElse(null));
    }

    private <P extends SelectProperty<T>, T> void addSwitchCases(SelectItemModel.UnbakedSwitch<P, T> unbakedSwitch) {
        for (SelectItemModel.SwitchCase<T> switchCase : unbakedSwitch.cases()) {
            cases.add(new Case(
                    new SelectCondition<>(
                            unbakedSwitch.property(),
                            switchCase.values()
                    ),
                    switchCase.model()
            ));
        }
    }
}
