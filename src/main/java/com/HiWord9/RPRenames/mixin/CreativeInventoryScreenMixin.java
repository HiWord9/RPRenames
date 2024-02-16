package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import com.HiWord9.RPRenames.util.gui.RenameButtonHolder;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow @Final private static String CUSTOM_CREATIVE_LOCK_KEY;

    @Redirect(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getType()Lnet/minecraft/item/ItemGroup$Type;", ordinal = 0))
    public ItemGroup.Type onSetSelectedTab(ItemGroup instance) {
        if (instance.getType() != ItemGroup.Type.HOTBAR || !RPRenames.verifyItemGroup(instance))
            return instance.getType();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return instance.getType();

        CreativeInventoryScreen.CreativeScreenHandler handler = ((CreativeInventoryScreen.CreativeScreenHandler) client.player.currentScreenHandler);

        if (RPRenames.renamedItemStacks.isEmpty()) {
            ItemStack itemStack = new ItemStack(Items.PAPER);
            itemStack.getOrCreateSubNbt(CUSTOM_CREATIVE_LOCK_KEY);
            itemStack.setCustomName(Text.translatable("rprenames.gui.noRenamesFound"));

            handler.itemList.add(itemStack);
        } else {
            ((CreativeInventoryScreen.CreativeScreenHandler) client.player.currentScreenHandler)
                    .itemList.addAll(RPRenames.renamedItemStacks);
        }
        File[] favoriteDirFiles = RPRenames.configPathFavorite.toFile().listFiles();
        if (favoriteDirFiles != null && favoriteDirFiles.length > 0) {
            int j = 9 - (handler.itemList.size() % 9);
            for (int i = 0; i < j; i++) {
                handler.itemList.add(ItemStack.EMPTY);
            }

            ItemStack itemStack = new ItemStack(Items.PAPER);
            itemStack.getOrCreateSubNbt(CUSTOM_CREATIVE_LOCK_KEY);
            itemStack.setCustomName(Text.translatable("rprenames.gui.tabs.tooltip.FAVORITE"));
            handler.itemList.add(itemStack);
            for (int i = 0; i < 8; i++) {
                handler.itemList.add(ItemStack.EMPTY);
            }

            Map<String, ArrayList<Rename>> favoriteRenames = ConfigManager.getAllFavorites();
            ArrayList<ItemStack> list = new ArrayList<>();
            for (String key : favoriteRenames.keySet()) {
                for (Rename r : favoriteRenames.get(key)) {
                    for (int i = 0; i < r.getItems().size(); i++) {
                        ItemStack stack = RenameButtonHolder.createItem(r, true, i);
                        list.add(stack);
                    }
                }
            }

            handler.itemList.addAll(list);
        }

        return ItemGroup.Type.CATEGORY;
    }
}
