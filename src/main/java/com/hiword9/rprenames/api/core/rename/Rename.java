package com.hiword9.rprenames.api.core.rename;

import com.hiword9.rprenames.api.core.rename.renderer.RenameRenderer;
import com.hiword9.rprenames.api.core.rename.renderer.SimpleRenameRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rename {
    protected final Component name;
    protected final List<Item> items = new ArrayList<>();

    public Rename(Component name, Item... items) {
        this.name = name;
        for (Item item : items) if (item != null) this.items.add(item);
    }

    public Component getName() {
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
        stack.set(DataComponents.CUSTOM_NAME, Component.translationArg(name));
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
        var customName = stack.get(DataComponents.CUSTOM_NAME);
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
