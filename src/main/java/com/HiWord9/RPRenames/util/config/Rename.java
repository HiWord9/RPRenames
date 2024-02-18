package com.HiWord9.RPRenames.util.config;

import com.HiWord9.RPRenames.util.config.generation.CEMConfig;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class Rename {
    private final String name;
    private ArrayList<String> items;
    private final String packName;
    private final String path;
    private final Integer stackSize;
    private final Damage damage;
    private final String enchantment;
    private final Integer enchantmentLevel;
    private final Properties properties;
    private final String description;
    private final Mob mob;
    private final boolean cem;

    public Rename(String name) {
        this(name, null, null, null, null, null, null, null, null, null, null);
    }

    public Rename(String name, String item) {
        this(name, new ArrayList<>(List.of(item)));
    }

    public Rename(String name, ArrayList<String> items) {
        this(name, items, null, null, null, null, null, null, null, null, null);
    }

    public Rename(String name,
                  String packName,
                  Mob mob) {
        this(name, new ArrayList<>(List.of(CEMConfig.DEFAULT_MOB_ITEM)), packName, null, null, null, null, null, null, null, mob);
    }

    public Rename(String name,
                  ArrayList<String> items,
                  String packName,
                  String path,
                  Integer stackSize,
                  Damage damage,
                  String enchantment,
                  Integer enchantmentLevel,
                  Properties properties,
                  String description,
                  @Nullable Mob mob) {
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

    public ArrayList<String> getItems() {
        return items;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public String getPackName() {
        return packName;
    }

    public String getPath() {
        return path;
    }

    public Integer getStackSize() {
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

    public Integer getEnchantmentLevel() {
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
        for (Rename r : list) {
            if (this.equals(r, ignoreNull)) {
                return true;
            }
        }
        return false;
    }

    public int indexIn(ArrayList<Rename> list, boolean ignoreNull) {
        if (!this.isContainedIn(list, ignoreNull)) {
            return -1;
        }
        int i = 0;
        for (Rename r : list) {
            if (this.equals(r, ignoreNull)) {
                break;
            }
            i++;
        }
        return i;
    }


    public static boolean isInBounds(int n, String list) {
        return isInBounds(n, list, null);
    }

    public static boolean isInBounds(int n, String list, @Nullable String damagedItem) {
        if (list == null) return true;
        if (!list.contains(" ") && !list.contains("-") && !list.contains("%"))
            return n == Integer.parseInt(getFirstValue(list));

        for (String s : split(list)) {
            if (s.contains("-")) {
                assert damagedItem != null;
                Item item = ConfigManager.itemFromName(damagedItem);
                if (s.indexOf("-") == s.length() - 1) {
                    String min = s.substring(0, s.length() - 1);
                    if (min.endsWith("%")) {
                        min = min.substring(0, min.length() - 1);

                        Damage minDamage = new Damage(Integer.parseInt(min), true);

                        if (n >= minDamage.getParsedDamage(item)) return true;
                    } else {
                        if (n >= Integer.parseInt(min)) return true;
                    }
                } else {
                    int i = s.indexOf('-');
                    String min = s.substring(0, i);
                    String max = s.substring(i + 1);
                    if (max.endsWith("%")) {
                        max = max.replace("%", "");

                        Damage minDamage = new Damage(Integer.parseInt(min), true);
                        Damage maxDamage = new Damage(Integer.parseInt(max), true);

                        if (n >= minDamage.getParsedDamage(item)
                                && n <= maxDamage.getParsedDamage(item)) {
                            return true;
                        }
                    } else {
                        if (n >= Integer.parseInt(min) && n <= Integer.parseInt(max)) return true;
                    }
                }
            } else if (n == Integer.parseInt(s)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> split(String list) {
        ArrayList<String> split = new ArrayList<>();
        if (list.contains(" ")) {
            int i = 0;
            int i1 = 0;
            while (i <= list.length()) {
                if (i == list.length() || list.charAt(i) == ' ') {
                    split.add(list.substring(i1, i));
                    i1 = i + 1;
                }
                i++;
            }
        } else {
            split.add(list);
        }
        return split;
    }

    public static int parseDamagePercent(int percent, Item item) {
        int maxDamage = item.getMaxDamage();
        return maxDamage * percent / 100;
    }

    public static String getFirstValue(String string) {
        StringBuilder builder = new StringBuilder();
        int n = 0;
        while (n < string.length()) {
            if (string.charAt(n) != '-' && string.charAt(n) != ' ') {
                builder.append(string.charAt(n));
                n++;
            } else {
                break;
            }
        }
        if (string.contains("%")) {
            builder.append("%");
        }
        return builder.toString();
    }

    public static boolean isFavorite(String item, String name) {
        ArrayList<Rename> favoriteList = ConfigManager.getFavorites(item);
        for (Rename r : favoriteList) {
            if (r.getName().equals(name)) {
                return true;
            }
        }
        return false;
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
            return parseDamagePercent(damage, item);
        }
    }

    public record Mob(String entity, String icon, Properties properties, String path) {
        public String getPropName() {
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
