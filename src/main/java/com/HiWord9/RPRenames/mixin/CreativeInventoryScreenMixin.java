package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow @Final private static String CUSTOM_CREATIVE_LOCK_KEY;

    @Redirect(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getType()Lnet/minecraft/item/ItemGroup$Type;", ordinal = 0))
    public ItemGroup.Type qwe(ItemGroup instance) {
        if (instance.getType() != ItemGroup.Type.HOTBAR || !RPRenames.verifyItemGroup(instance))
            return instance.getType();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return instance.getType();

        if (RPRenames.renamedItemStacks.isEmpty()) {
            ItemStack itemStack = new ItemStack(Items.PAPER);
            itemStack.getOrCreateSubNbt(CUSTOM_CREATIVE_LOCK_KEY);
            itemStack.setCustomName(Text.translatable("rprenames.gui.noRenamesFound"));

            ((CreativeInventoryScreen.CreativeScreenHandler) client.player.currentScreenHandler)
                    .itemList.add(itemStack);
        } else {
            ((CreativeInventoryScreen.CreativeScreenHandler) client.player.currentScreenHandler)
                    .itemList.addAll(RPRenames.renamedItemStacks);
        }

        return ItemGroup.Type.CATEGORY;
    }
}
