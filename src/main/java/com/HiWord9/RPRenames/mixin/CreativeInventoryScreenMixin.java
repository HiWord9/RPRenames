package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import com.HiWord9.RPRenames.util.gui.RenameButtonHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin {

    @Shadow @Final private static String CUSTOM_CREATIVE_LOCK_KEY;

    @Shadow private TextFieldWidget searchBox;

    @Shadow private static ItemGroup selectedTab;

    @Shadow private float scrollPosition;

    @Inject(method = "search", at = @At(value = "HEAD"), cancellable = true)
    private void onSearch(CallbackInfo ci) {
        if (!RPRenames.verifyItemGroup(selectedTab)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        CreativeInventoryScreen.CreativeScreenHandler handler = ((CreativeInventoryScreen.CreativeScreenHandler) client.player.currentScreenHandler);
        handler.itemList.clear();

        String search = searchBox.getText();

        if (!search.isEmpty()) {
            ArrayList<ItemStack> filteredList = searchStacks(RPRenames.renamedItemStacks, search);
            if (filteredList.isEmpty()) {
                handler.itemList.add(getNoRenamesFoundItem());
            } else {
                handler.itemList.addAll(filteredList);
            }
        } else {
            if (RPRenames.renamedItemStacks.isEmpty()) {
                handler.itemList.add(getNoRenamesFoundItem());
            } else {
                handler.itemList.addAll(RPRenames.renamedItemStacks);
            }

            File[] favoriteDirFiles = RPRenames.configPathFavorite.toFile().listFiles();
            if (favoriteDirFiles != null && favoriteDirFiles.length > 0) {
                int j = 9 - (handler.itemList.size() % 9);
                for (int i = 0; i < j; i++) {
                    handler.itemList.add(ItemStack.EMPTY);
                }

                handler.itemList.add(getFavoriteItem());
                for (int i = 0; i < 8; i++) {
                    handler.itemList.add(ItemStack.EMPTY);
                }

                ArrayList<ItemStack> list = new ArrayList<>();
                Map<String, ArrayList<Rename>> favoriteRenames = ConfigManager.getAllFavorites();
                for (String key : favoriteRenames.keySet()) {
                    for (Rename r : favoriteRenames.get(key)) {
                        if (r.getItems().size() > 1) {
                            for (int i = 0; i < r.getItems().size(); i++) {
                                ItemStack stack = ConfigManager.createItem(r, true, i);
                                list.add(stack);
                            }
                        } else {
                            list.add(ConfigManager.createItemOrSpawnEgg(r));
                        }
                    }
                }

                handler.itemList.addAll(list);
            }
        }

        scrollPosition = 0.0F;
        handler.scrollItems(0.0F);
        ci.cancel();
    }

    private ArrayList<ItemStack> searchStacks(ArrayList<ItemStack> renamedItemStacks, String search) {
        ArrayList<ItemStack> list = new ArrayList<>();

        for (ItemStack stack : renamedItemStacks) {
            if (stack.getName().getString().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                list.add(stack);
            }
        }

        return list;
    }

    private ItemStack getNoRenamesFoundItem() {
        ItemStack itemStack = new ItemStack(Items.PAPER);
        itemStack.getOrCreateSubNbt(CUSTOM_CREATIVE_LOCK_KEY);
        itemStack.setCustomName(Text.translatable("rprenames.gui.noRenamesFound"));
        return itemStack;
    }

    private ItemStack getFavoriteItem() {
        ItemStack itemStack = new ItemStack(Items.PAPER);
        itemStack.getOrCreateSubNbt(CUSTOM_CREATIVE_LOCK_KEY);
        itemStack.setCustomName(Text.translatable("rprenames.gui.tabs.tooltip.FAVORITE"));
        return itemStack;
    }
}
