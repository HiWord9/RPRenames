package com.HiWord9.RPRenames;

import net.minecraft.item.ItemGroup;

public interface ItemGroupBuilderMixinAccessor {
    ItemGroup.Builder setType(ItemGroup.Type type);
}
