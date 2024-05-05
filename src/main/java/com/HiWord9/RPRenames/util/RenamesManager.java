package com.HiWord9.RPRenames.util;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.generation.CEMParser;
import com.HiWord9.RPRenames.util.config.generation.CITParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

public class RenamesManager {
    private static final ModConfig config = ModConfig.INSTANCE;

    public static Map<String, ArrayList<Rename>> renames = new HashMap<>();

    public static ArrayList<ItemStack> renamedItemStacks = new ArrayList<>();

    public static void updateRenames() {
        MinecraftClient client = MinecraftClient.getInstance();
        updateRenames(client.getResourceManager(), client.getProfiler());
    }

    public static void updateRenames(ResourceManager resourceManager, Profiler profiler) {
        profiler.push("rprenames:reloading_renames");

        clearRenames();
        long startTime = System.currentTimeMillis();
        RPRenames.LOGGER.info("Starting collecting renames");

        CITParser.parse(resourceManager, profiler);
        CEMParser.parse(resourceManager, profiler);

        long finishTime = System.currentTimeMillis() - startTime;
        updateItemGroup();
        RPRenames.LOGGER.info("Finished collecting renames [{}.{}s]", finishTime / 1000, finishTime % 1000);

        profiler.pop();
    }

    public static void updateItemGroup() {
        renamedItemStacks.clear();
        ArrayList<ItemStack> list = new ArrayList<>();
        ArrayList<Rename> parsedRenames = new ArrayList<>();
        for (String key : renames.keySet()) {
            for (Rename r : renames.get(key)) {
                if (parsedRenames.contains(r)) continue;
                parsedRenames.add(r);
                if (r.getItems().size() > 1 && !config.compareItemGroupRenames) {
                    for (int i = 0; i < r.getItems().size(); i++) {
                        ItemStack stack = RenamesHelper.createItem(r, true, i);
                        list.add(stack);
                    }
                } else {
                    ItemStack stack = RenamesHelper.createItemOrSpawnEgg(r);
                    list.add(stack);
                }
            }
        }
        renamedItemStacks.addAll(list);
    }

    public static void clearRenames() {
        renames.clear();
    }

    public static ArrayList<Rename> getAllRenames() {
        ArrayList<Rename> names = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Rename>> entry : renames.entrySet()) {
            for (Rename r : entry.getValue()) {
                if (!names.contains(r)) names.add(r);
            }
        }
        return names;
    }

    public static ArrayList<Rename> getRenames(String item) {
        if (renames.containsKey(item)) {
            return renames.get(item);
        } else {
            return new ArrayList<>();
        }
    }

    public static void addRename(String item, Rename rename) {
        if (renames.containsKey(item)) {
            Rename simplifiedRename = new Rename(rename.getName(),
                    rename.getItems(),
                    null,
                    null,
                    rename.getStackSize(),
                    rename.getDamage(),
                    rename.getEnchantment(),
                    rename.getEnchantmentLevel(),
                    null,
                    null,
                    null);
            if (!simplifiedRename.isContainedIn(renames.get(item), true)) {
                renames.get(item).add(rename);
            }
        } else {
            ArrayList<Rename> arrayList = new ArrayList<>();
            arrayList.add(rename);
            renames.put(item, arrayList);
        }
    }
}