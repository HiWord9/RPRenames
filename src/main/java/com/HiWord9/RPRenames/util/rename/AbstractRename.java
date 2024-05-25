package com.HiWord9.RPRenames.util.rename;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AbstractRename {
    protected String name;
    protected String packName;
    protected String path;
    protected Properties properties;
    protected ArrayList<Item> items = new ArrayList<>();

    public AbstractRename(String name) {
        this(name, null);
    }

    public AbstractRename(String name, Item item) {
        this(name, null, null, null, item);
    }

    public AbstractRename(String name,
                          String packName,
                          String path,
                          Properties properties,
                          Item item) {
        this(name, packName, path, properties, item == null ? null : new ArrayList<>(List.of(item)));
    }

    public AbstractRename(String name,
                          String packName,
                          String path,
                          Properties properties,
                          ArrayList<Item> items) {
        this.name = name;
        this.packName = packName;
        this.path = path == null ? null : path.replace("\\", "/");
        this.properties = properties;
        this.setItems(items);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        if (items == null) {
            this.items = new ArrayList<>();
            return;
        }
        this.items = items;
    }

    public Item getItem() {
        return items.isEmpty() ? null : items.get(0);
    }

    public void setItem(Item item) {
        if (items == null || items.isEmpty()) {
            items = new ArrayList<>();
            items.add(item);
        } else {
            this.items.set(0, item);
        }
    }

    public String getNamePattern() {
        return null;
    }

    public ItemStack toStack() {
        return toStack(0);
    }

    public ItemStack toStack(int index) {
        return new ItemStack(items.get(index)).setCustomName(Text.of(name));
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
                && paramsEquals(this.path, obj.path, ignoreNull)
                && paramsEquals(this.properties, obj.properties, ignoreNull);
    }

    public boolean same(AbstractRename obj, boolean ignoreNull) {
        return paramsEquals(this.name, obj.name, ignoreNull)
                && paramsEquals(this.getItem(), obj.getItem(), ignoreNull);
    }

    protected boolean paramsEquals(Object obj1, Object obj2, boolean ignoreNull) {
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
}
