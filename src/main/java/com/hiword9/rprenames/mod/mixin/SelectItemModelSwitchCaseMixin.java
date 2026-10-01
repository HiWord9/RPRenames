package com.hiword9.rprenames.mod.mixin;

import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties.RenameProperties;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties.RenamePropertiesHolder;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties.SwitchCasePropertiesCodec;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.item.SelectItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SelectItemModel.SwitchCase.class)
public class SelectItemModelSwitchCaseMixin implements RenamePropertiesHolder {
    @Unique
    private RenameProperties rprenames$properties = RenameProperties.EMPTY;

    @ModifyReturnValue(at = @At("RETURN"), method = "codec")
    private static <T> Codec<SelectItemModel.SwitchCase<T>> withProperties(Codec<SelectItemModel.SwitchCase<T>> original) {
        return new SwitchCasePropertiesCodec<>(original);
    }

    @Override
    public RenameProperties rprenames$getProperties() {
        return rprenames$properties;
    }

    @Override
    public void rprenames$setProperties(RenameProperties properties) {
        rprenames$properties = properties;
    }
}
