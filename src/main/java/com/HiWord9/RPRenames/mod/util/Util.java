package com.HiWord9.RPRenames.mod.util;

import com.HiWord9.RPRenames.mod.config.ModConfig;
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

    public static ModConfig config() {
        return ModConfig.INSTANCE;
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }

    public static ClientPlayerEntity player() {
        return client().player;
    }

    public static TextRenderer textRenderer() {
        return client().textRenderer;
    }

    public static Screen currentScreen() {
        return client().currentScreen;
    }

    public static List<ItemStack> inventoryCopy() {
        assert player() != null;
        return player()
                .getInventory()
                .getMainStacks()
                .stream().map(ItemStack::copy)
                .toList();
    }

    public static Item itemFromId(String id) {
        return Registries.ITEM.get(Identifier.of(id));
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
}
