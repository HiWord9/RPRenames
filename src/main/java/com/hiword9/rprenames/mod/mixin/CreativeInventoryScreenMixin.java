package com.hiword9.rprenames.mod.mixin;

import com.hiword9.rprenames.mod.item_group.RPRenamesItemGroup;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.hiword9.rprenames.mod.util.Util.*;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow private EditBox searchBox;

    @Shadow private static CreativeModeTab selectedTab;

    @Shadow private float scrollOffs;

    @Inject(method = "refreshSearchResults", at = @At(value = "HEAD"), cancellable = true)
    private void onSearch(CallbackInfo ci) {
        if (!RPRenamesItemGroup.verifyItemGroup(selectedTab)) return;

        if (player() == null) return;

        CreativeModeInventoryScreen.ItemPickerMenu handler =
                ((CreativeModeInventoryScreen.ItemPickerMenu) player().containerMenu);

        String search = searchBox.getValue();

        handler.items.clear();
        handler.items.addAll(RPRenamesItemGroup.getDisplayStacks(search));

        scrollOffs = 0.0F;
        handler.scrollTo(0.0F);
        ci.cancel();
    }
}
