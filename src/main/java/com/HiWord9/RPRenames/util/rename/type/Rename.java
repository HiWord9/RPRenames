package com.HiWord9.RPRenames.util.rename.type;

import com.HiWord9.RPRenames.util.rename.renderer.builder.RenameRendererBuilder;
import com.HiWord9.RPRenames.util.rename.renderer.builder.SimpleRenameRendererBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rename {
    protected final String name;
    protected final List<Item> items = new ArrayList<>();

    public Rename(String name, Item... items) {
        this.name = name;
        for (Item item : items) if (item != null) this.items.add(item);
    }

    public String getName() {
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

    public RenameRendererBuilder<?> getNewRendererBuilder() {
        return new SimpleRenameRendererBuilder<>(this);
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
