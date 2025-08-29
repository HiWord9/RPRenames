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
            ArrayList<Item> items
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

    public boolean equals(Object obj) {
        if (obj instanceof AbstractRename abstractRename) {
            return this.equals(abstractRename);
        }
        return false;
    }

    public boolean equals(AbstractRename obj) {
        return equals(obj, false);
    }

    public boolean equals(AbstractRename obj, boolean ignoreNull) {
        return this.same(obj, ignoreNull)
                && paramsEquals(this.packName, obj.packName, ignoreNull)
                && paramsEquals(this.path, obj.path, ignoreNull);
    }

    public boolean same(AbstractRename obj, boolean ignoreNull) {
        return paramsEquals(this.name, obj.name, ignoreNull)
                && paramsEquals(this.getItem(), obj.getItem(), ignoreNull);
    }

    protected static boolean paramsEquals(Object obj1, Object obj2, boolean ignoreNull) {
        if (obj1 == null && obj2 == null) {
            return true;
        } else if (obj1 == null || obj2 == null) {
            return ignoreNull;
        } else {
            return obj1.equals(obj2);
        }
    }

    public boolean isContainedIn(ArrayList<AbstractRename> list) {
        return isContainedIn(list, false);
    }

    public boolean isContainedIn(ArrayList<AbstractRename> list, boolean ignoreNull) {
        return this.indexIn(list, ignoreNull) != -1;
    }

    public int indexIn(ArrayList<AbstractRename> list, boolean ignoreNull) {
        int i = -1;
        if (list == null) return i;
        for (AbstractRename r : list) {
            i++;
            if (r.equals(this, ignoreNull)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                "name='" + name + '\'' +
                ", packName='" + packName + '\'' +
                ", path='" + path + '\'' +
                ", items=" + items +
                '}';
    }
}
