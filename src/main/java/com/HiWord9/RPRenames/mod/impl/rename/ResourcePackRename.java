package com.HiWord9.RPRenames.mod.impl.rename;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.mod.util.RenameInfoHelper;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

import java.util.List;

public class ResourcePackRename extends Rename implements Informative {
    protected final String packName;
    protected final String path;

    public ResourcePackRename(Text name, Item... items) {
        this(name, null, null, items);
    }

    public ResourcePackRename(Text name, String packName, String path, Item... items) {
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

    @Override
    public List<Text> getInfo() {
        return RenameInfoHelper.getRPPath(packName, path);
    }
}
