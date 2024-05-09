package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.config.generation.CEMParser;
import net.minecraft.item.Item;

import java.util.*;

public class Rename {
    protected final String name;
    protected ArrayList<Item> items;
    protected final String packName;
    protected final String path;
    protected final Integer stackSize;
    protected final Damage damage;
    protected final String enchantment;
    protected final Integer enchantmentLevel;
    protected final Properties properties;
    protected final String description;
    protected final Mob mob;
    protected final boolean cem;

    public Rename(String name) {
        this(name, null, null, null, null, null, null, null, null, null, null);
    }

    public Rename(String name, Item item) {
        this(name, new ArrayList<>(List.of(item)));
    }

    public Rename(String name, ArrayList<Item> items) {
        this(name, items, null, null, null, null, null, null, null, null, null);
    }

    public Rename(String name,
                  String packName,
                  Mob mob) {
        this(name, new ArrayList<>(List.of(CEMParser.DEFAULT_MOB_ITEM)), packName, null, null, null, null, null, null, null, mob);
    }

    public Rename(String name,
                  ArrayList<Item> items,
                  String packName,
                  String path,
                  Integer stackSize,
                  Damage damage,
                  String enchantment,
                  Integer enchantmentLevel,
                  Properties properties,
                  String description,
                  Mob mob) {
        this.name = name;
        this.items = items;
        this.packName = packName;
        this.path = path == null ? null : path.replace("\\", "/");
        this.stackSize = stackSize;
        this.damage = damage;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
        this.properties = properties;
        this.description = description;
        this.mob = mob;
        this.cem = mob != null;
    }


    public String getName() {
        return name;
    }

    public String getOriginalNbtDisplayName() {
        return properties == null ? cem ? mob.getPropName() : null : properties.getProperty("nbt.display.Name");
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public String getPackName() {
        return packName;
    }

    public String getPath() {
        return path;
    }

    public int getStackSize() {
        return stackSize == null ? 1 : stackSize;
    }

    public String getOriginalStackSize() {
        return properties == null ? null : properties.getProperty("stackSize");
    }

    public Damage getDamage() {
        return damage;
    }

    public String getOriginalDamage() {
        return properties == null ? null : properties.getProperty("damage");
    }

    public String getEnchantment() {
        return enchantment;
    }

    public String getOriginalEnchantment() {
        return properties == null ? null : properties.getProperty("enchantmentIDs");
    }

    public int getEnchantmentLevel() {
        return enchantmentLevel == null ? 1 : enchantmentLevel;
    }

    public String getOriginalEnchantmentLevel() {
        return properties == null ? null : properties.getProperty("enchantmentLevels");
    }

    public Properties getProperties() {
        return properties;
    }

    public String getDescription() {
        return description;
    }

    public Mob getMob() {
        return mob;
    }

    public boolean isCEM() {
        return cem;
    }

    public boolean equals(Rename obj) {
        return equals(obj, false);
    }

    public boolean equals(Rename obj, boolean ignoreNull) {
        boolean originalNbtDisplayNameEquals = paramsEquals(this.getOriginalNbtDisplayName(), obj.getOriginalNbtDisplayName(), ignoreNull);
        boolean stackSizeEquals = paramsEquals(this.getStackSize(), obj.getStackSize(), ignoreNull);
        boolean originalStackSizeEquals = paramsEquals(this.getOriginalStackSize(), obj.getOriginalStackSize(), ignoreNull);
        boolean damageEquals = paramsEquals(this.getDamage(), obj.getDamage(), ignoreNull);
        boolean originalDamageEquals = paramsEquals(this.getOriginalDamage(), obj.getOriginalDamage(), ignoreNull);
        boolean enchantmentEquals = paramsEquals(this.getEnchantment(), obj.getEnchantment(), ignoreNull);
        boolean originalEnchantmentEquals = paramsEquals(this.getOriginalEnchantment(), obj.getOriginalEnchantment(), ignoreNull);
        boolean eEnchantmentLevelEquals = paramsEquals(this.getEnchantmentLevel(), obj.getEnchantmentLevel(), ignoreNull);
        boolean originalEnchantmentLevelEquals = paramsEquals(this.getOriginalEnchantmentLevel(), obj.getOriginalEnchantmentLevel(), ignoreNull);

        return (this.name.equals(obj.name)) && originalNbtDisplayNameEquals
                && stackSizeEquals && originalStackSizeEquals
                && damageEquals && originalDamageEquals
                && enchantmentEquals && originalEnchantmentEquals
                && eEnchantmentLevelEquals && originalEnchantmentLevelEquals;
    }

    private boolean paramsEquals(Object obj1, Object obj2, boolean ignoreNull) {
        if (obj1 == null && obj2 == null) {
            return true;
        } else if (obj1 == null || obj2 == null) {
            return ignoreNull;
        } else {
            return obj1.equals(obj2);
        }
    }

    public boolean isContainedIn(ArrayList<Rename> list) {
        return isContainedIn(list, false);
    }

    public boolean isContainedIn(ArrayList<Rename> list, boolean ignoreNull) {
        return this.indexIn(list, ignoreNull) != -1;
    }

    public int indexIn(ArrayList<Rename> list, boolean ignoreNull) {
        int i = 0;
        for (Rename r : list) {
            if (this.equals(r, ignoreNull)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static class Damage {
        public int damage;
        public boolean percent;

        public Damage(Integer damage, boolean percent) {
            this.damage = damage;
            this.percent = percent;
        }

        public int getParsedDamage(Item item) {
            if (!percent) return damage;
            return PropertiesHelper.parseDamagePercent(damage, item);
        }
    }

    public record Mob(String entity, Item icon, Properties properties, String path) {
        public String getPropName() {
            if (properties == null) return null;
            Set<String> propertyNames = properties.stringPropertyNames();
            for (String s : propertyNames) {
                if (s.startsWith("name.")) {
                    if (propertyNames.contains("skins." + s.substring(5))) {
                        return properties.getProperty(s);
                    }
                }
            }
            return null;
        }
    }
}
