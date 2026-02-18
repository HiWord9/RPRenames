package com.HiWord9.RPRenames.mod.impl.rename;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.mod.gui.widget.GhostCraft;
import com.HiWord9.RPRenames.mod.item_group.ItemGroupComponent;
import com.HiWord9.RPRenames.mod.util.RenameInfoHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class ResourcePackRename extends Rename implements Informative, ItemGroupComponent, GhostCraft.Loader {
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

    @Override
    public List<ItemStack> getItemGroupStacks() {
        return toStackAll();
    }

    @Override
    public void loadGhostCraft(GhostCraft ghostCraft, ItemStack itemStack) {
        if (!itemStack.isEmpty()) return;

        var source = new ItemStack(getItem());
        var result = toStack();

        var stacks = new ItemStack[ghostCraft.length];
        if (ghostCraft.length >= 1) {
            stacks[0] = source;
            stacks[ghostCraft.length - 1] = result;
        }

        ghostCraft.setStacks(stacks);
        ghostCraft.setRender(true);
    }
}
