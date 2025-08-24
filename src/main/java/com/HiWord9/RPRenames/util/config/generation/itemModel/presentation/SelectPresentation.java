package com.HiWord9.RPRenames.util.config.generation.itemModel.presentation;

import com.HiWord9.RPRenames.util.config.generation.itemModel.presentation.condition.Condition;
import net.minecraft.client.render.item.model.SelectItemModel;
import net.minecraft.client.render.item.property.select.SelectProperty;

import java.util.List;

public class SelectPresentation extends ItemModelPresentation {
    public SelectPresentation(SelectItemModel.Unbaked unbakedModel) {
        addSwitchCases(unbakedModel.unbakedSwitch());
        addFallback(unbakedModel.fallback().orElse(null));
    }

    private <P extends SelectProperty<T>, T> void addSwitchCases(SelectItemModel.UnbakedSwitch<P, T> unbakedSwitch) {
        for (SelectItemModel.SwitchCase<T> switchCase : unbakedSwitch.cases()) {
            cases.add(new Case(new Condition(
                    new PropertyValuesHolder<T>( // todo define select properties
                            unbakedSwitch.property(),
                            switchCase.values()
                    ).toString()
            ), switchCase.model()));
        }
    }

    private record PropertyValuesHolder<T>(SelectProperty<T> property, List<T> values) {}
}
