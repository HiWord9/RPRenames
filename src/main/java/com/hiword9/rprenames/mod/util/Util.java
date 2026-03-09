package com.hiword9.rprenames.mod.util;

import com.hiword9.rprenames.mod.config.ModConfig;
import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public class Util {
    private static final String MINECRAFT_COLON = Identifier.DEFAULT_NAMESPACE + Identifier.NAMESPACE_SEPARATOR;

    public static final Gson GSON = new Gson();

    public static ModConfig config() {
        return ModConfig.INSTANCE;
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }

    public static ClientPlayerEntity player() throws AssertionError {
        assert client() != null;
        return client().player;
    }

    public static TextRenderer textRenderer() throws AssertionError {
        assert client() != null;
        return client().textRenderer;
    }

    public static Screen currentScreen() throws AssertionError {
        assert client() != null;
        return client().currentScreen;
    }

    public static List<ItemStack> inventoryCopy() throws AssertionError {
        assert player() != null;
        return player()
                .getInventory()
                .getMainStacks()
                .stream().map(ItemStack::copy)
                .toList();
    }

    public static Item itemFromId(String id) {
        return itemFromId(Identifier.of(id));
    }

    public static Item itemFromId(Identifier id) {
        return Registries.ITEM.get(id);
    }

    public static String idFromItem(Item item) {
        String id = Registries.ITEM.getId(item).toString();
        if (id.startsWith(MINECRAFT_COLON))
            return id.substring(MINECRAFT_COLON.length());
        return id;
    }

    public static int randomNumber() {
        assert player() != null;
        return player().getRandom().nextBetween(0, Integer.MAX_VALUE - 1);
    }

    public static <T> List<T> setAndFillMissing(List<T> values, int index, T value, T filler) {
        if (values.size() <= index)
            for (int i = values.size(); i <= index; i++)
                values.add(filler);

        values.set(index, value);

        return values;
    }
}
