package com.HiWord9.RPRenames.util.config.generation;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.rename.RenamesManager;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CITParser implements Parser {
    private static final List<String> ROOTS = List.of("mcpatcher", "optifine", "citresewn");

    public void parse(ResourceManager resourceManager, Profiler profiler) {
        profiler.push("rprenames:collecting_cit_renames");
        for (String root : ROOTS) {
            for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources(root + "/cit", s -> s.getPath().endsWith(".properties")).entrySet()) {
                try {
                    String packName = ParserHelper.validatePackName(entry.getValue().getResourcePackName());
                    CITParser.propertiesToRename(
                            ParserHelper.getPropFromResource(entry.getValue()),
                            packName,
                            ParserHelper.getFullPathFromIdentifier(packName, entry.getKey())
                    );
                } catch (IOException e) {
                    RPRenames.LOGGER.error("Something went wrong while parsing CIT Renames", e);
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

        ArrayList<Item> items = itemsFromMatchItems(matchItems);
        if (items.isEmpty()) return;

        String nbtNamePattern = p.getProperty("nbt.display.Name");

        String stackSizeProp = p.getProperty("stackSize");
        String firstStackSize = PropertiesHelper.getFirstValueInList(stackSizeProp == null ? "" : stackSizeProp);
        Integer stackSize = null;
        if (!firstStackSize.isEmpty()) {
            int i = Integer.parseInt(firstStackSize);
            if (i <= 64 && i > 0) {
                stackSize = i;
            }
        }

        String damageProp = p.getProperty("damage");
        CITRename.Damage damage = null;
        if (damageProp != null) {
            String firstDamage = PropertiesHelper.getFirstValueInList(damageProp);

            if (!firstDamage.isEmpty()) {
                try {
                    int d = Integer.parseInt(firstDamage.replace("%", ""));
                    damage = new CITRename.Damage(d, firstDamage.contains("%"));
                } catch (NumberFormatException ignored) {
                    RPRenames.LOGGER.warn("Could not get valid damage value {} for {}", firstDamage, path);
                }
            }
        }

        String enchantIdProp = p.getProperty("enchantmentIDs");
        Identifier enchantment = null;
        if (enchantIdProp != null) {
            String firstEnchantId = PropertiesHelper.getFirstValueInList(enchantIdProp);
            enchantment = Identifier.of(firstEnchantId);
            if (Registries.ENCHANTMENT.get(enchantment) == null) {
                RPRenames.LOGGER.warn("Could not get valid enchantment {} for {}", enchantment, path);
                enchantment = null;
            }
        }

        String enchantLvlProp = p.getProperty("enchantmentLevels");
        String firstEnchantLvl = PropertiesHelper.getFirstValueInList(enchantLvlProp == null ? "" : enchantLvlProp);
        Integer enchantLvl = firstEnchantLvl.isEmpty() ? null : Integer.parseInt(firstEnchantLvl) <= 0 ? null : Integer.parseInt(firstEnchantLvl);

        String description = p.getProperty("%rprenames.description");
        if (description == null) description = p.getProperty("%rpr.description");
        if (description == null) description = p.getProperty("%description");

        if (nbtNamePattern == null) return;

        CITRename rename = new CITRename(
                PropertiesHelper.getFirstName(nbtNamePattern, path),
                items,
                packName,
                path,
                stackSize,
                damage,
                enchantment,
                enchantLvl,
                p,
                description
        );

        for (Item item : items) {
            CITRename simplifiedRename = new CITRename(rename.getName(),
                    null,
                    null,
                    null,
                    rename.getStackSize(),
                    rename.getDamage(),
                    rename.getEnchantment(),
                    rename.getEnchantmentLevel(),
                    null,
                    null);
            if (!simplifiedRename.isContainedIn(RenamesManager.renames.get(item), true)) {
                RenamesManager.addRename(item, rename);
            }
        }
    }

    private static ArrayList<String> splitMatchItems(String matchItems) {
        ArrayList<String> items = new ArrayList<>();
        int start = 0;
        while (start <= matchItems.length()) {
            String item = PropertiesHelper.getFirstValueInList(matchItems.substring(start));
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

    private static ArrayList<Item> itemsFromMatchList(ArrayList<String> matchItemsList) {
        ArrayList<Item> items = new ArrayList<>();
        for (String matchItem : matchItemsList) {
            Item item = Registries.ITEM.get(Identifier.of(matchItem));
            if (item == Items.AIR) continue;
            items.add(item);
        }
        return items;
    }

    private static ArrayList<Item> itemsFromMatchItems(String matchItems) {
        return itemsFromMatchList(splitMatchItems(matchItems));
    }
}
