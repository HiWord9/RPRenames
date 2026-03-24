package com.hiword9.rprenames.mod.util;

import com.hiword9.rprenames.mod.config.ModConfig;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Util {
    private static final String MINECRAFT_COLON = Identifier.DEFAULT_NAMESPACE + Identifier.NAMESPACE_SEPARATOR;

    public static final Gson GSON = new Gson();

    public static ModConfig config() {
        return ModConfig.INSTANCE;
    }

    public static Minecraft client() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() throws AssertionError {
        assert client() != null;
        return client().player;
    }

    public static Font textRenderer() throws AssertionError {
        assert client() != null;
        return client().font;
    }

    public static Screen currentScreen() throws AssertionError {
        assert client() != null;
        return client().screen;
    }

    public static boolean hasShiftDown() {
        return client().hasShiftDown();
    }

    public static List<ItemStack> inventoryCopy() throws AssertionError {
        assert player() != null;
        return player()
                .getInventory()
                .getNonEquipmentItems()
                .stream().map(ItemStack::copy)
                .toList();
    }

    public static Item itemFromId(String id) {
        return itemFromId(Identifier.parse(id));
    }

    public static Item itemFromId(Identifier id) {
        return BuiltInRegistries.ITEM.getValue(id);
    }

    public static String idFromItem(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        if (id.startsWith(MINECRAFT_COLON))
            return id.substring(MINECRAFT_COLON.length());
        return id;
    }

    public static int randomNumber() {
        assert player() != null;
        return player().getRandom().nextIntBetweenInclusive(0, Integer.MAX_VALUE - 1);
    }

    public static <T> List<T> setAndFillMissing(List<T> values, int index, T value, T filler) {
        if (values.size() <= index)
            for (int i = values.size(); i <= index; i++)
                values.add(filler);

        values.set(index, value);

        return values;
    }
}
