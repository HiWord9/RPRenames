package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.RPRenamesItemGroup;
import com.HiWord9.RPRenames.util.config.generation.Parser;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RenamesManager {
    public static final ArrayList<Parser> parsers = new ArrayList<>();

    public static final Map<Item, ArrayList<AbstractRename>> renames = new HashMap<>();

    public static void updateRenames() {
        MinecraftClient client = MinecraftClient.getInstance();
        updateRenames(client.getResourceManager(), client.getProfiler());
    }

    public static void updateRenames(ResourceManager resourceManager, Profiler profiler) {
        profiler.push("rprenames:reloading_renames");

        clearRenames();
        long startTime = System.currentTimeMillis();
        RPRenames.LOGGER.info("Starting collecting renames");

        for (Parser parser : parsers) {
            parser.parse(resourceManager, profiler);
        }

        RPRenamesItemGroup.update();
        long finishTime = System.currentTimeMillis() - startTime;
        RPRenames.LOGGER.info("Finished collecting renames [{}.{}s]", finishTime / 1000, finishTime % 1000);

        profiler.pop();
    }

    public static void clearRenames() {
        renames.clear();
    }

    public static ArrayList<AbstractRename> getAllRenames() {
        ArrayList<AbstractRename> names = new ArrayList<>();
        for (Map.Entry<Item, ArrayList<AbstractRename>> entry : renames.entrySet()) {
            for (AbstractRename r : entry.getValue()) {
                if (!r.isContainedIn(names)) names.add(r);
            }
        }
        return names;
    }

    public static ArrayList<AbstractRename> getRenames(Item item) {
        if (renames.containsKey(item)) {
            return renames.get(item);
        } else {
            return new ArrayList<>();
        }
    }

    public static void addRename(Item item, AbstractRename rename) {
        if (renames.containsKey(item)) {
            renames.get(item).add(rename);
        } else {
            ArrayList<AbstractRename> arrayList = new ArrayList<>();
            arrayList.add(rename);
            renames.put(item, arrayList);
        }
    }
}