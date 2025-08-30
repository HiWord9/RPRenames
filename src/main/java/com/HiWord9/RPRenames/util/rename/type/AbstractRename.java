package com.HiWord9.RPRenames.util.rename.type;

import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.renderer.DefaultRenameRenderer;
import com.HiWord9.RPRenames.util.rename.renderer.RenameRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AbstractRename {
    protected final String name;
    protected final String packName;
    protected final String path;
    protected final List<Item> items = new ArrayList<>();

    public AbstractRename(String name) {
        this(name, null);
    }

    public AbstractRename(String name, Item item) {
        this(name, null, null, item);
    }

    public AbstractRename(
            String name,
            String packName,
            String path,
            Item item
    ) {
        this(name, packName, path, item == null ? null : new ArrayList<>(List.of(item)));
    }

    public AbstractRename(
            String name,
            String packName,
            String path,
            List<Item> items
    ) {
        this.name = name;
        this.packName = packName;
        this.path = path == null ? null : path.replace("\\", "/");
        if (items != null) this.items.addAll(items);
    }

    public String getName() {
        return name;
    }

    public String getPackName() {
        return packName;
    }

    public String getPath() {
        return path;
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

    public RenameRenderer getNewRenderer(RPRWidget rprWidget, boolean favorite) {
        return new DefaultRenameRenderer<>(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractRename abstractRename
                && Objects.equals(name, abstractRename.name)
                && Objects.equals(items, abstractRename.items);
    }

    public boolean baseEquals(AbstractRename abstractRename) {
        return equals(abstractRename);
    }
}
