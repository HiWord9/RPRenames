package com.HiWord9.RPRenames.util.renames_manager.updatable.item_model.presentation;

import com.HiWord9.RPRenames.util.renames_manager.updatable.item_model.condition.Condition;
import net.minecraft.client.render.item.model.*;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemModelPresentation {
    protected List<Case> cases = new ArrayList<>();

    public List<Case> getCases() {
        return List.copyOf(cases);
    }

    protected void addFallback(ItemModel.Unbaked fallback) {
        if (fallback == null) return;
        cases.add(new Case(Condition.FALLBACK, fallback));
    }

    public record Case(Condition condition, ItemModel.Unbaked result) {}

    public static ItemModelPresentation of(ItemModel.Unbaked unbakedModel) {
        return switch (unbakedModel) {
            case CompositeItemModel.Unbaked composite -> new CompositePresentation(composite);
            case ConditionItemModel.Unbaked condition -> new ConditionPresentation(condition);
            case SelectItemModel.Unbaked select -> new SelectPresentation(select);
            case RangeDispatchItemModel.Unbaked rangeDispatch -> new RangeDispatchPresentation(rangeDispatch);
            default -> new FinalModelPresentation(); // basic, empty, bundle/selected_item, special and unknown
        };
    }
}

