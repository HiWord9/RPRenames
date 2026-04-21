package com.hiword9.rprenames.mod.item_group;

import com.hiword9.rprenames.api.core.renames_manager.RenamesProvider;
import com.hiword9.rprenames.mod.RPRenames;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import static com.hiword9.rprenames.mod.util.Util.*;

public class RPRenamesItemGroup {
    public static final List<ItemStack> renamedItemStacks = new ArrayList<>();
    protected static CreativeModeTab itemGroup;

    public static void register(BiConsumer<Identifier, CreativeModeTab> registry) {
        registry.accept(
                Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "item_group"),
                itemGroup = CreativeModeTab.builder(null, -1)
                        .title(Component.translatable("rprenames.item_group"))
                        .icon(RPRenamesItemGroup::getItemGroupIcon)
                        .type(CreativeModeTab.Type.SEARCH)
                        .backgroundTexture(CreativeModeTab.createTextureLocation("item_search"))
                        .displayItems((_, entries) -> {
                            // some loaders may not show tab if it's "empty"
                            entries.accept(getItemGroupIcon());
                            update();
                        })
                        .build()
        );
    }

    public static void update() {
        renamedItemStacks.clear();
        if (client().level == null) return;
        renamedItemStacks.addAll(getAllRenamedStacks(RPRenames.updatableRenamesManager));
    }

    static ItemStack getItemGroupIcon() {
        ItemStack stack = new ItemStack(Items.KNOWLEDGE_BOOK);
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    public static boolean verifyItemGroup(CreativeModeTab itemGroup) {
        return RPRenamesItemGroup.itemGroup == itemGroup;
    }

    public static List<ItemStack> searchStacks(List<ItemStack> renamedItemStacks, String search) {
        ArrayList<ItemStack> list = new ArrayList<>();

        for (ItemStack stack : renamedItemStacks) {
            if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                list.add(stack);
            }
        }

        return list;
    }

    private static ItemStack getNoRenamesFoundItem() {
        ItemStack itemStack = new ItemStack(Items.PAPER);
        itemStack.set(DataComponents.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
        itemStack.set(DataComponents.CUSTOM_NAME, Component.translatable("rprenames.gui.noRenamesFound"));
        return itemStack;
    }

    private static ItemStack getFavoriteItem() {
        ItemStack itemStack = new ItemStack(Items.PAPER);
        itemStack.set(DataComponents.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
        itemStack.set(DataComponents.CUSTOM_NAME, Component.translatable("rprenames.gui.tabs.tooltip.FAVORITE"));
        return itemStack;
    }

    public static List<ItemStack> getDisplayStacks(String search) {
        return search(search, renamedItemStacks);
    }

    public static List<ItemStack> search(String search, List<ItemStack> itemList) {
        ArrayList<ItemStack> list = new ArrayList<>();

        if (!search.isEmpty()) {
            var filteredList = searchStacks(itemList, search);
            if (filteredList.isEmpty()) {
                list.add(getNoRenamesFoundItem());
            } else {
                list.addAll(filteredList);
            }
        } else {
            list.addAll(renamedItemStacks);
            if (list.isEmpty()) {
                list.add(getNoRenamesFoundItem());
            }

            addFavoriteStacks(list);
        }

        return list;
    }

    public static void addFavoriteStacks(List<ItemStack> itemList) {
        File[] favoriteDirFiles = RPRenames.configPathFavorite.toFile().listFiles();

        if (favoriteDirFiles == null || favoriteDirFiles.length == 0) {
            return;
        }

        int j = 9 - (itemList.size() % 9);
        for (int i = 0; i < j; i++) {
            itemList.add(ItemStack.EMPTY);
        }

        itemList.add(getFavoriteItem());
        for (int i = 0; i < 8; i++) {
            itemList.add(ItemStack.EMPTY);
        }

        itemList.addAll(getAllRenamedStacks(RPRenames.favoritesManager));
    }

    public static List<ItemStack> getAllRenamedStacks(RenamesProvider<?> renamesManager) {
        var list = new ArrayList<ItemStack>();
        for (var r : renamesManager.getAllRenames()) {
            if (r instanceof ItemGroupComponent itemGroupComponent) {
                list.addAll(itemGroupComponent.getItemGroupStacks());
            }
        }
        return list;
    }
}
