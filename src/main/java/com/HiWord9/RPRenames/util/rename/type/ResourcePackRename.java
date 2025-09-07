package com.HiWord9.RPRenames.util.rename.type;

import net.minecraft.item.Item;

public class ResourcePackRename extends Rename {
    protected final String packName;
    protected final String path;

    public ResourcePackRename(String name, Item... items) {
        this(name, null, null, items);
    }

    public ResourcePackRename(String name, String packName, String path, Item... items) {
        super(name, items);
        this.packName = packName;
        this.path = path == null ? null : path.replace("\\", "/");
    }

    public String getPackName() {
        return packName;
    }

    public String getPath() {
        return path;
    }
}
