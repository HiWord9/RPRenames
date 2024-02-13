package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.ItemGroupBuilderMixinAccessor;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemGroup.Builder.class)
public abstract class ItemGroupBuilderMixin implements ItemGroupBuilderMixinAccessor {

    @Shadow protected abstract ItemGroup.Builder type(ItemGroup.Type type);

    @Override
    public ItemGroup.Builder setType(ItemGroup.Type type) {
        return type(type);
    }
}
