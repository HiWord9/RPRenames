package com.HiWord9.RPRenames.mod;

import com.HiWord9.RPRenames.mod.config.ModConfig;
import com.HiWord9.RPRenames.mod.util.RenamesHelper;
import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.mod.impl.rename.CITRename;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RPRenamesItemGroup {
    private static final ModConfig config = ModConfig.INSTANCE;

    public static final ArrayList<ItemStack> renamedItemStacks = new ArrayList<>();

    public static void register() {
        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(RPRenames.MOD_ID, "item_group"),
                FabricItemGroup.builder()
                        .displayName(Text.translatable("rprenames.item_group"))
                        .icon(RPRenamesItemGroup::getItemGroupIcon)
                        .type(ItemGroup.Type.SEARCH)
                        .texture(ItemGroup.getTabTextureId("item_search"))
                        .entries((displayContext, entries) -> update())
                        .build()
        );
    }

    public static void update() {
        renamedItemStacks.clear();
        if (MinecraftClient.getInstance().world == null) return;
        renamedItemStacks.addAll(getAllRenamedStacks());
    }

    static ItemStack getItemGroupIcon() {
        ItemStack stack = new ItemStack(Items.KNOWLEDGE_BOOK);

        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        NbtCompound nbt = new NbtCompound();
        nbt.putString(RPRenames.MOD_ID, "");
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        return stack;
    }

    public static boolean verifyItemGroup(ItemGroup itemGroup) {
        ItemStack icon = itemGroup.getIcon();
        NbtComponent nbtComponent = icon.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return false;
        return nbtComponent.contains(RPRenames.MOD_ID);
    }

    public static List<ItemStack> searchStacks(List<ItemStack> renamedItemStacks, String search) {
        ArrayList<ItemStack> list = new ArrayList<>();

        for (ItemStack stack : renamedItemStacks) {
            if (stack.getName().getString().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
                list.add(stack);
            }
        }

        return list;
    }

    private static ItemStack getNoRenamesFoundItem() {
        ItemStack itemStack = new ItemStack(Items.PAPER);
        itemStack.set(DataComponentTypes.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
        itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("rprenames.gui.noRenamesFound"));
        return itemStack;
    }

    private static ItemStack getFavoriteItem() {
        ItemStack itemStack = new ItemStack(Items.PAPER);
        itemStack.set(DataComponentTypes.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
        itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("rprenames.gui.tabs.tooltip.FAVORITE"));
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

        itemList.addAll(getFavoriteStacks());
    }

    public static List<ItemStack> getFavoriteStacks() {
        ArrayList<ItemStack> list = new ArrayList<>();
        for (Rename r : RPRenames.favoritesManager.getAllRenames()) {
            for (int i = 0; i < r.getItems().size(); i++) {
                ItemStack stack = RenamesHelper.createItemOrSpawnEgg(r, i);
                list.add(stack);
            }
        }
        return list;
    }

    public static List<ItemStack> getAllRenamedStacks() {
        ArrayList<ItemStack> list = new ArrayList<>();
        for (Rename r : RPRenames.renamesManager.getAllRenames()) {
            if (r instanceof CITRename citRename && citRename.getItems().size() > 1 && !config.compareItemGroupRenames) {
                for (int i = 0; i < citRename.getItems().size(); i++) {
                    ItemStack stack = RenamesHelper.createItemOrSpawnEgg(citRename, i);
                    list.add(stack);
                }
            } else {
                list.add(RenamesHelper.createItemOrSpawnEgg(r));
            }
        }
        return list;
    }
}
