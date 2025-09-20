package com.HiWord9.RPRenames.util.renames_manager.favorite;

import com.HiWord9.RPRenames.util.rename.type.Rename;
import net.minecraft.item.Item;

public class FavoriteRename extends Rename {
    public FavoriteRename(String name, Item item) {
        super(name, item);
    }

    public void setItem(Item item) {
        if (!items.isEmpty()) items.removeFirst();
        items.addFirst(item);
    }
}
