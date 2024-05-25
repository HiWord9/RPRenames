package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.*;

public class CITRename extends AbstractRename implements Describable {
    protected final Integer stackSize;
    protected final Damage damage;
    protected final String enchantment;
    protected final Integer enchantmentLevel;
    protected String description;

    public CITRename(String name, Item item) {
        this(name, new ArrayList<>(List.of(item)));
    }

    public CITRename(String name, ArrayList<Item> items) {
        this(name, items, null, null, null, null, null, null, null, null);
    }

    public CITRename(String name,
                     ArrayList<Item> items,
                     String packName,
                     String path,
                     Integer stackSize,
                     Damage damage,
                     String enchantment,
                     Integer enchantmentLevel,
                     Properties properties,
                     String description) {
        super(name, packName, path, properties, items);
        this.stackSize = stackSize;
        this.damage = damage;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
        this.description = description;
    }

    public String getNamePattern() {
        return properties == null ? null : properties.getProperty("nbt.display.Name");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean equals(CITRename obj, boolean ignoreNull) {
        boolean originalNbtDisplayNameEquals = paramsEquals(this.getNamePattern(), obj.getNamePattern(), ignoreNull);
        boolean originalStackSizeEquals = paramsEquals(this.getOriginalStackSize(), obj.getOriginalStackSize(), ignoreNull);
        boolean originalDamageEquals = paramsEquals(this.getOriginalDamage(), obj.getOriginalDamage(), ignoreNull);
        boolean originalEnchantmentEquals = paramsEquals(this.getOriginalEnchantment(), obj.getOriginalEnchantment(), ignoreNull);
        boolean originalEnchantmentLevelEquals = paramsEquals(this.getOriginalEnchantmentLevel(), obj.getOriginalEnchantmentLevel(), ignoreNull);
        boolean descriptionEquals = paramsEquals(this.getDescription(), obj.getDescription(), ignoreNull);

        return super.equals(obj, ignoreNull) && this.same(obj, ignoreNull)
                && originalNbtDisplayNameEquals
                && originalStackSizeEquals
                && originalDamageEquals
                && originalEnchantmentEquals
                && originalEnchantmentLevelEquals
                && descriptionEquals;
    }

    public boolean same(CITRename obj, boolean ignoreNull) {
        boolean stackSizeEquals = paramsEquals(this.getStackSize(), obj.getStackSize(), ignoreNull);
        boolean damageEquals = paramsEquals(this.getDamage(), obj.getDamage(), ignoreNull);
        boolean enchantmentEquals = paramsEquals(this.getEnchantment(), obj.getEnchantment(), ignoreNull);
        boolean enchantmentLevelEquals = paramsEquals(this.getEnchantmentLevel(), obj.getEnchantmentLevel(), ignoreNull);
        return super.same(obj, ignoreNull)
                && stackSizeEquals
                && damageEquals
                && enchantmentEquals
                && enchantmentLevelEquals;
    }

    public static class Damage {
        public final int damage;
        public final boolean percent;

        public Damage(Integer damage, boolean percent) {
            this.damage = damage;
            this.percent = percent;
        }

        public int getParsedDamage(Item item) {
            if (!percent) return damage;
            return PropertiesHelper.parseDamagePercent(damage, item);
        }
    }

    @Override
    public ItemStack toStack(int index) {
        ItemStack item = new ItemStack(this.getItems().get(index >= this.getItems().size() ? 0 : index));
        item.setCustomName(Text.of(getName()));
        item.setCount(getStackSize());
        if (getDamage() != null) {
            item.setDamage(getDamage().getParsedDamage(item.getItem()));
        }
        if (getEnchantment() != null) {
            RenamesHelper.enchantItemStackWithRename(this, item);
        }
        return item;
//        return super.toStack(index).
//        return RenamesHelper.createItemStackCIT(this, index);
    }
}
