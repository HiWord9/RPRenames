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
    protected final List<Component> names;
    protected final List<Item> items = new ArrayList<>();

    public Rename(Component name, Item... items) {
        this(List.of(name), items);
    }

    public Rename(List<Component> names, Item... items) {
        if (names.isEmpty()) throw new IllegalArgumentException("Rename must have at least one name");
        this.names = List.copyOf(names);
        for (Item item : items) if (item != null) this.items.add(item);
    }

    public Component getName() {
        return names.getFirst();
    }

    public List<Component> getNames() {
        return names;
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

    public ItemStack toStack(int itemIndex) {
        return toStack(itemIndex, 0);
    }

    public ItemStack toStack(int itemIndex, int nameIndex) {
        ItemStack stack = new ItemStack(items.get(itemIndex));
        stack.set(DataComponents.CUSTOM_NAME, Component.translationArg(names.get(nameIndex)));
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
        return customName != null && names.contains(customName);
    }

    public RenameRenderer.Builder<?> getNewRendererBuilder(RenameRenderer.RenderArea renderArea) {
        return new SimpleRenameRenderer.Builder<>(this, renderArea);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Rename rename
                && Objects.equals(names, rename.names)
                && Objects.equals(items, rename.items);
    }

    public boolean baseEquals(Rename rename) {
        return equals(rename);
    }
}
