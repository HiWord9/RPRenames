package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser;

import com.hiword9.rprenames.api.ext.renames_manager.parser.Parser;
import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.util.ParserHelper;
import com.hiword9.rprenames.mod.util.PropertiesHelper;
import com.hiword9.rprenames.api.core.renames_manager.RenamesManager;
import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.mod.impl.rename.CITRename;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.*;

import static com.hiword9.rprenames.mod.util.Util.config;

public class CITParser implements Parser {
    private static final List<String> ROOTS = List.of("mcpatcher", "optifine", "citresewn");

    public RenamesManager<? super CITRename> renamesManager;

    public CITParser(RenamesManager<? super CITRename> renamesManager) {
        this.renamesManager = renamesManager;
    }

    public void parse(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("rprenames:collecting_cit_renames");

        if (config().ignoreCIT) {
            profiler.pop();
            return;
        }

        for (String root : ROOTS) {
            for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(root + "/cit", s -> s.getPath().endsWith(".properties")).entrySet()) {
                try {
                    String packName = ParserHelper.validatePackName(entry.getValue().source().packId());
                    propertiesToRename(
                            ParserHelper.getPropFromResource(entry.getValue()),
                            packName,
                            ParserHelper.getFullPathFromIdentifier(packName, entry.getKey())
                    );
                } catch (Exception e) {
                    RPRenames.LOGGER.error("Something went wrong while parsing CIT Renames", e);
                }
            }
        }
        profiler.pop();
    }

    private void propertiesToRename(Properties p, String packName, String path) {
        String matchItems = p.getProperty("matchItems");
        if (matchItems == null) matchItems = p.getProperty("items");
        if (matchItems == null) return;

        while (matchItems.endsWith(" ") || matchItems.endsWith("\t")) {
            matchItems = matchItems.substring(0, matchItems.length() - 1);
        }

        var items = itemsFromMatchItems(matchItems);
        if (items.isEmpty()) return;

        String customName = PropertiesHelper.getCustomName(p);
        if (customName == null) return;
        //todo lore

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
            enchantment = Identifier.parse(firstEnchantId);
        }

        String enchantLvlProp = p.getProperty("enchantmentLevels");
        String firstEnchantLvl = PropertiesHelper.getFirstValueInList(enchantLvlProp == null ? "" : enchantLvlProp);
        Integer enchantLvl = firstEnchantLvl.isEmpty() ? null : Integer.parseInt(firstEnchantLvl) <= 0 ? null : Integer.parseInt(firstEnchantLvl);

        String description = p.getProperty("$rprenames.description");
        if (description == null) description = p.getProperty("$rpr.description");
        if (description == null) description = p.getProperty("$description");

        CITRename rename = new CITRename(
                PropertiesHelper.getFirstName(customName, path),
                packName,
                path,
                stackSize,
                damage,
                enchantment,
                enchantLvl,
                p,
                description,
                items.toArray(new Item[]{})
        );

        for (Item item : items) {
            boolean contained = false;
            for (Rename r : renamesManager.getRenames(item)) {
                if (r instanceof CITRename citRename
                        && Objects.equals(citRename.getName(), rename.getName())
                        && Objects.equals(citRename.getStackSize(), rename.getStackSize())
                        && Objects.equals(citRename.getDamage(), rename.getDamage())
                        && Objects.equals(citRename.getEnchantment(), rename.getEnchantment())
                        && Objects.equals(citRename.getEnchantmentLevel(), rename.getEnchantmentLevel())
                ) contained = true;
            }
            if (!contained) {
                renamesManager.addRename(item, rename);
            }
        }
    }

    private static List<String> splitMatchItems(String matchItems) {
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

    private static List<Item> itemsFromMatchList(List<String> matchItemsList) {
        ArrayList<Item> items = new ArrayList<>();
        for (String matchItem : matchItemsList) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(matchItem));
            if (item == Items.AIR) continue;
            items.add(item);
        }
        return items;
    }

    private static List<Item> itemsFromMatchItems(String matchItems) {
        return itemsFromMatchList(splitMatchItems(matchItems));
    }
}
