package com.HiWord9.RPRenames.util.config.generation;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;

import java.util.Properties;

public class CITConfig {

    public static void propertiesToRename(Properties p, String packName, String path) {
        String items = p.getProperty("matchItems") == null ? p.getProperty("items") : p.getProperty("matchItems");
        if (items != null) {
            while (items.endsWith(" ")) {
                items = items.substring(0, items.length() - 1);
            }
            int start = 0;
            while (start <= items.length()) {
                String item = Rename.getFirstValue(items.substring(start));
                start += item.length() + 1;
                if (item.startsWith("minecraft:")) {
                    item = item.substring(10);
                }

                String nbtNamePattern = p.getProperty("nbt.display.Name");

                String stackSizeProp = p.getProperty("stackSize");
                String firstStackSize = Rename.getFirstValue(stackSizeProp == null ? "" : stackSizeProp);
                Integer stackSize = firstStackSize.isEmpty() ? null : Integer.parseInt(firstStackSize) > 64 || Integer.parseInt(firstStackSize) <= 0 ? null : Integer.parseInt(firstStackSize);

                String damageProp = p.getProperty("damage");
                String firstDamage = Rename.getFirstValue(damageProp == null ? "" : damageProp);
                Integer damage;
                if (firstDamage.contains("%")) {
                    damage = Rename.parseDamagePercent(firstDamage, item);
                } else {
                    damage = firstDamage.isEmpty() ? null : Integer.parseInt(firstDamage) <= 0 ? null : Integer.parseInt(firstDamage);
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
                    Rename rename = new Rename(ConfigManager.getFirstName(nbtNamePattern, item),
                            item,
                            packName,
                            path,
                            stackSize,
                            damage,
                            firstEnchantId,
                            enchantLvl,
                            p,
                            null);
                    ConfigManager.renamesAdd(packName.equals("server") ? RPRenames.renamesServer : RPRenames.renames, item, rename);
                }
            }
        }
    }
}
