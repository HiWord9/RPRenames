package com.hiword9.rprenames.api.core.renames_manager;

import com.hiword9.rprenames.api.core.rename.Rename;
import net.minecraft.world.item.Item;
import java.util.List;

public interface RenamesProvider<R extends Rename> {
    List<R> getAllRenames();
    List<R> getRenames(Item item);
}
