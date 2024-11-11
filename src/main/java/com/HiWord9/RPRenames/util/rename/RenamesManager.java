package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.RPRenamesItemGroup;
import com.HiWord9.RPRenames.util.config.generation.Parser;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;

import java.util.*;
import java.util.stream.Collectors;

public class RenamesManager {
    public final ArrayList<Parser> parsers = new ArrayList<>();

    private final Map<Item, ArrayList<AbstractRename>> renames = new HashMap<>();

    public void updateRenames() {
        MinecraftClient client = MinecraftClient.getInstance();
        updateRenames(client.getResourceManager(), client.getProfiler());
    }

    public void updateRenames(ResourceManager resourceManager, Profiler profiler) {
        profiler.push("rprenames:reloading_renames");

        RPRenames.LOGGER.info("Started collecting resource pack renames");
        long startTime = System.currentTimeMillis();

        clearRenames();

        for (Parser parser : parsers) {
            parser.parse(resourceManager, profiler);
        }

        RPRenamesItemGroup.update();

        long finishTime = System.currentTimeMillis() - startTime;
        String ms = String.valueOf(finishTime % 1000);
        switch (ms.length()) {
            case 1 -> ms = "00" + ms;
            case 2 -> ms = "0" + ms;
        }
        RPRenames.LOGGER.info(
                "Finished collecting resource pack renames [{}.{}s] ({} in total)",
                finishTime / 1000, ms, getAllRenames().size()
        );

        profiler.pop();
    }

    public void clearRenames() {
        renames.clear();
    }

    public ArrayList<AbstractRename> getAllRenames() {
        return (ArrayList<AbstractRename>) renames
                .entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public ArrayList<AbstractRename> getRenames(Item item) {
        if (renames.containsKey(item)) {
            return renames.get(item);
        } else {
            return new ArrayList<>();
        }
    }

    public void addRename(Item item, AbstractRename rename) {
        if (renames.containsKey(item)) {
            renames.get(item).add(rename);
        } else {
            ArrayList<AbstractRename> arrayList = new ArrayList<>();
            arrayList.add(rename);
            renames.put(item, arrayList);
        }
    }

    public void overrideRenames(Item item, ArrayList<AbstractRename> newRenames) {
        renames.put(item, newRenames);
    }

    public void removeRename(Item item, AbstractRename rename) {
        if (renames.containsKey(item)) {
            renames.get(item).remove(rename);
        }
    }

    public ArrayList<Item> renamedItems() {
        return new ArrayList<>(renames.keySet());
    }
}