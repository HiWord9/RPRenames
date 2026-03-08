package com.HiWord9.RPRenames.api;

import com.HiWord9.RPRenames.api.rename.Rename;
import net.minecraft.item.Item;

import java.util.List;

public interface RenamesProvider<R extends Rename> {
    List<R> getAllRenames();
    List<R> getRenames(Item item);
}
