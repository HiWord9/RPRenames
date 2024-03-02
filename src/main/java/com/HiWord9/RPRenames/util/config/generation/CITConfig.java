package com.HiWord9.RPRenames.util.config.generation;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.HiWord9.RPRenames.util.config.ConfigManager.*;

public class CITConfig {
    private static final List<String> ROOTS = List.of("mcpatcher", "optifine", "citresewn");

    public static void parseCITs(ResourceManager resourceManager, Profiler profiler) {
        profiler.push("rprenames:collecting_cit_renames");
        for (String root : ROOTS) {
            for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources(root + "/cit", s -> s.getPath().endsWith(".properties")).entrySet()) {
                try {
                    String packName = validatePackName(entry.getValue().getResourcePackName());
                    CITConfig.propertiesToRename(
                            getPropFromResource(entry.getValue()),
                            packName,
                            getFullPathFromIdentifier(packName, entry.getKey())
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        profiler.pop();
    }

    private static void propertiesToRename(Properties p, String packName, String path) {
        String matchItems = p.getProperty("matchItems");
        if (matchItems == null) matchItems = p.getProperty("items");
        if (matchItems == null) return;

        while (matchItems.endsWith(" ") || matchItems.endsWith("\t")) {
            matchItems = matchItems.substring(0, matchItems.length() - 1);
        }

        ArrayList<String> items = splitMatchItems(matchItems);

        String nbtNamePattern = p.getProperty("nbt.display.Name");

        String stackSizeProp = p.getProperty("stackSize");
        String firstStackSize = Rename.getFirstValue(stackSizeProp == null ? "" : stackSizeProp);
        Integer stackSize = null;
        if (!firstStackSize.isEmpty()) {
            int i = Integer.parseInt(firstStackSize);
            if (i <= 64 && i > 0) {
                stackSize = i;
            }
        }

        String damageProp = p.getProperty("damage");
        Rename.Damage damage = null;
        if (damageProp != null) {
            String firstDamage = Rename.getFirstValue(damageProp);

            if (!firstDamage.isEmpty()) {
                try {
                    int d = Integer.parseInt(firstDamage.replace("%", ""));
                    damage = new Rename.Damage(d, firstDamage.contains("%"));
                } catch (NumberFormatException ignored) {
                    RPRenames.LOGGER.warn("Could not get valid damage value " + firstDamage + " for " + path);
                }
            }
        }

        String enchantIdProp = p.getProperty("enchantmentIDs");
        String firstEnchantId = enchantIdProp;
        if (enchantIdProp != null) {
            firstEnchantId = Rename.getFirstValue(enchantIdProp);
        }

        String enchantLvlProp = p.getProperty("enchantmentLevels");
        String firstEnchantLvl = Rename.getFirstValue(enchantLvlProp == null ? "" : enchantLvlProp);
        Integer enchantLvl = firstEnchantLvl.isEmpty() ? null : Integer.parseInt(firstEnchantLvl) <= 0 ? null : Integer.parseInt(firstEnchantLvl);

        String description = p.getProperty("rpr.description");
        if (description == null) description = p.getProperty("description");

        if (nbtNamePattern != null) {
            Rename rename = new Rename(
                    ConfigManager.getFirstName(nbtNamePattern, items),
                    items,
                    packName,
                    path,
                    stackSize,
                    damage,
                    firstEnchantId,
                    enchantLvl,
                    p,
                    description,
                    null
            );

            for (String item : items) {
                ConfigManager.addRename(item, rename);
            }
        }
    }

    private static ArrayList<String> splitMatchItems(String matchItems) {
        ArrayList<String> items = new ArrayList<>();
        int start = 0;
        while (start <= matchItems.length()) {
            String item = Rename.getFirstValue(matchItems.substring(start));
            start += item.length() + 1;
            if (item.startsWith("minecraft:")) {
                item = item.substring(10);
            }
            if (item.equals("air")) {
                continue;
            }
            items.add(item);
        }
        return items;
    }
}
