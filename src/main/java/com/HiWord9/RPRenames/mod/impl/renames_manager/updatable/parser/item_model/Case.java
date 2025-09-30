package com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model;

import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.BooleanCondition;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.Condition;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.NumericCondition;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.SelectCondition;
import net.minecraft.client.render.item.model.*;
import net.minecraft.client.render.item.property.select.SelectProperty;

import java.util.ArrayList;
import java.util.List;

public record Case(Condition condition, ItemModel.Unbaked result) {

    public static List<Case> getCases(ItemModel.Unbaked unbakedModel) {
        var cases = new ArrayList<Case>();
        switch (unbakedModel) {
            case CompositeItemModel.Unbaked composite -> fillCases(cases, composite);
            case ConditionItemModel.Unbaked condition -> fillCases(cases, condition);
            case SelectItemModel.Unbaked select -> fillCases(cases, select);
            case RangeDispatchItemModel.Unbaked rangeDispatch -> fillCases(cases, rangeDispatch);
            default -> {} // basic, empty, bundle/selected_item, special and unknown
        }
        return cases;
    }

    private static void fillCases(List<Case> cases, CompositeItemModel.Unbaked unbakedCompositeModel) {
        for (ItemModel.Unbaked model : unbakedCompositeModel.models()) {
            cases.add(new Case(Condition.COMPOSITE, model));
        }
    }

    private static void fillCases(List<Case> cases, ConditionItemModel.Unbaked unbakedConditionModel) {
        cases.add(new Case(
                new BooleanCondition<>(unbakedConditionModel.property(), true),
                unbakedConditionModel.onTrue()
        ));
        cases.add(new Case(
                new BooleanCondition<>(unbakedConditionModel.property(), false),
                unbakedConditionModel.onFalse()
        ));
    }

    @SuppressWarnings("unchecked")
    private static <P extends SelectProperty<T>, T> void fillCases(
            List<Case> cases, SelectItemModel.Unbaked unbakedSelectModel
    ) {
        var unbakedSwitch = (SelectItemModel.UnbakedSwitch<P, T>) unbakedSelectModel.unbakedSwitch();

        for (var switchCase : unbakedSwitch.cases()) {
            cases.add(new Case(
                    new SelectCondition<>(unbakedSwitch.property(), switchCase.values()),
                    switchCase.model()
            ));
        }

        unbakedSelectModel.fallback().ifPresent(fallback ->
                cases.add(new Case(Condition.FALLBACK, fallback))
        );
    }

    private static void fillCases(List<Case> cases, RangeDispatchItemModel.Unbaked unbakedRangeDispatchModel) {
        for (RangeDispatchItemModel.Entry entry : unbakedRangeDispatchModel.entries()) {
            cases.add(new Case(
                    new NumericCondition<>(unbakedRangeDispatchModel.property(), entry.threshold()),
                    entry.model()
            ));
        }

        unbakedRangeDispatchModel.fallback().ifPresent(fallback ->
                cases.add(new Case(Condition.FALLBACK, fallback))
        );
    }
}

