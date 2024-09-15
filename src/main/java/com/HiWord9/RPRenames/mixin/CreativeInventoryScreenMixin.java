package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenamesItemGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow private TextFieldWidget searchBox;

    @Shadow private static ItemGroup selectedTab;

    @Shadow private float scrollPosition;

    @Inject(method = "search", at = @At(value = "HEAD"), cancellable = true)
    private void onSearch(CallbackInfo ci) {
        if (!RPRenamesItemGroup.verifyItemGroup(selectedTab)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        CreativeInventoryScreen.CreativeScreenHandler handler = ((CreativeInventoryScreen.CreativeScreenHandler) client.player.currentScreenHandler);

        String search = searchBox.getText();

        handler.itemList.clear();
        handler.itemList.addAll(RPRenamesItemGroup.getDisplayStacks(search));

        scrollPosition = 0.0F;
        handler.scrollItems(0.0F);
        ci.cancel();
    }
}
