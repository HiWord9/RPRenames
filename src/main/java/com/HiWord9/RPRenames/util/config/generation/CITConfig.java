package com.HiWord9.RPRenames.util.config.generation;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;

import java.util.ArrayList;
import java.util.Properties;

public class CITConfig {

    public static void propertiesToRename(Properties p, String packName, String path) {
        String matchItems = p.getProperty("matchItems") == null ? p.getProperty("items") : p.getProperty("matchItems");
        if (matchItems == null) return;

        while (matchItems.endsWith(" ") || matchItems.endsWith("\t")) {
            matchItems = matchItems.substring(0, matchItems.length() - 1);
        }

        ArrayList<String> items = splitMatchItems(matchItems);

        String nbtNamePattern = p.getProperty("nbt.display.Name");

        String stackSizeProp = p.getProperty("stackSize");
        String firstStackSize = Rename.getFirstValue(stackSizeProp == null ? "" : stackSizeProp);
        Integer stackSize = firstStackSize.isEmpty() ? null : Integer.parseInt(firstStackSize) > 64 || Integer.parseInt(firstStackSize) <= 0 ? null : Integer.parseInt(firstStackSize);

        String damageProp = p.getProperty("damage");
        String firstDamage = Rename.getFirstValue(damageProp == null ? "" : damageProp);
        Rename.Damage damage = null;

        if (!firstDamage.isEmpty()) {
            try {
                int d = Integer.parseInt(firstDamage);
                if (d > 0) {
                    damage = new Rename.Damage(d, firstDamage.contains("%"));
                }
            } catch (NumberFormatException ignored) {}
        }

        String enchantIdProp = p.getProperty("enchantmentIDs");
        String firstEnchantId = enchantIdProp;
        if (enchantIdProp != null) {
            firstEnchantId = Rename.getFirstValue(enchantIdProp);
        }

        String enchantLvlProp = p.getProperty("enchantmentLevels");
        String firstEnchantLvl = Rename.getFirstValue(enchantLvlProp == null ? "" : enchantLvlProp);
        Integer enchantLvl = firstEnchantLvl.isEmpty() ? null : Integer.parseInt(firstEnchantLvl) <= 0 ? null : Integer.parseInt(firstEnchantLvl);

        if (nbtNamePattern != null) {
            Rename rename = new Rename(ConfigManager.getFirstName(nbtNamePattern, items),
                    items,
                    packName,
                    path,
                    stackSize,
                    damage,
                    firstEnchantId,
                    enchantLvl,
                    p,
                    null);
            for (String item : items) {
                ConfigManager.renamesAdd(packName.equals("server") ? RPRenames.renamesServer : RPRenames.renames, item, rename);
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
