package com.HiWord9.RPRenames.mod.impl.rename;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.api.rename.renderer.RenameRenderer;
import com.HiWord9.RPRenames.mod.impl.rename.renderer.ItemModelRenameRenderer;
import com.HiWord9.RPRenames.mod.impl.renames_manager.updatable.parser.item_model.condition.ItemModelCondition;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class ItemModelRename extends Rename {
    protected final List<ItemModelCondition.Applicable> conditions;

    public ItemModelRename(List<ItemModelCondition.Applicable> conditions, Text name, Item... items) {
        super(name, items);
        this.conditions = conditions;
    }

    @Override
    public ItemStack toStack(int index) {
        ItemStack stack = new ItemStack(items.get(index));
        for (ItemModelCondition.Applicable condition : conditions) {
            condition.apply(stack);
        }
        return stack;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                && obj instanceof ItemModelRename i
                && Objects.deepEquals(conditions, i.conditions);
    }

    @Override
    public RenameRenderer.Builder<?> getNewRendererBuilder(RenameRenderer.RenderArea renderArea) {
        return new ItemModelRenameRenderer.Builder(this, renderArea);
    }
}
