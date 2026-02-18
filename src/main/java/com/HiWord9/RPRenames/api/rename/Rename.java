package com.HiWord9.RPRenames.api.rename;

import com.HiWord9.RPRenames.api.rename.renderer.RenameRenderer;
import com.HiWord9.RPRenames.api.rename.renderer.SimpleRenameRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rename {
    protected final Text name;
    protected final List<Item> items = new ArrayList<>();

    public Rename(Text name, Item... items) {
        this.name = name;
        for (Item item : items) if (item != null) this.items.add(item);
    }

    public Text getName() {
        return name;
    }

    public List<Item> getItems() {
        return items;
    }

    public Item getItem() {
        return items.isEmpty() ? null : items.getFirst();
    }

    public ItemStack toStack() {
        return toStack(0);
    }

    public ItemStack toStack(int index) {
        ItemStack stack = new ItemStack(items.get(index));
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.of(name));
        return stack;
    }

    public List<ItemStack> toStackAll() {
        var list = new ArrayList<ItemStack>();
        for (int i = 0; i < items.size(); i++) {
            list.add(toStack(i));
        }
        return list;
    }

    public boolean matchesStack(ItemStack stack) {
        if (!getItems().contains(stack.getItem())) return false;
        var customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        return customName != null && customName.equals(getName());
    }

    public RenameRenderer.Builder<?> getNewRendererBuilder(RenameRenderer.RenderArea renderArea) {
        return new SimpleRenameRenderer.Builder<>(this, renderArea);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Rename rename
                && Objects.equals(name, rename.name)
                && Objects.equals(items, rename.items);
    }

    public boolean baseEquals(Rename rename) {
        return equals(rename);
    }
}
