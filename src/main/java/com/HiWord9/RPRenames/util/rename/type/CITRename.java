package com.HiWord9.RPRenames.util.rename.type;

import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.RenamesHelper;
import com.HiWord9.RPRenames.util.rename.renderer.CITRenameRenderer;
import com.HiWord9.RPRenames.util.rename.renderer.RenameRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class CITRename extends AbstractRename implements Describable {
    protected final Integer stackSize;
    protected final Damage damage;
    protected final Identifier enchantment;
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
                     Identifier enchantment,
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
        if (properties == null) return null; // todo replace with more reliable logic
        String nbtNamePattern = properties.getProperty("components.custom_name");
        if (nbtNamePattern == null) nbtNamePattern = properties.getProperty("components.minecraft\\:custom_name");
        if (nbtNamePattern == null) nbtNamePattern = properties.getProperty("components.~custom_name");
        if (nbtNamePattern == null) nbtNamePattern = properties.getProperty("nbt.display.Name");
        return nbtNamePattern;
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

    public Identifier getEnchantment() {
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

    public boolean equals(AbstractRename obj, boolean ignoreNull) {
        if (obj instanceof CITRename citRename) {
            return equals(citRename, ignoreNull);
        }
        return false;
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

    public boolean same(AbstractRename obj, boolean ignoreNull) {
        if (obj instanceof CITRename citRename) {
            return same(citRename, ignoreNull);
        }
        return false;
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
        item.set(DataComponentTypes.CUSTOM_NAME, Text.of(getName()));
        item.setCount(getStackSize());
        if (getDamage() != null) {
            item.setDamage(getDamage().getParsedDamage(item.getItem()));
        }
        if (getEnchantment() != null) {
            RenamesHelper.enchantItemStackWithRename(this, item);
        }
        return item;
    }

    /**
     * This class tells is given {@link ItemStack} passes given {@link CITRename} required conditions.
     * Calculations are executed only on initialization, so any further stack's changes won't affect result.
     * It does not take in count stack's name and item.
     */

    public static class CraftMatcher {
        boolean enoughStackSize = true;
        boolean enoughDamage = true;
        boolean hasEnchant = false;
        boolean hasEnoughLevels = false;

        public CraftMatcher(CITRename rename, ItemStack stack) {
            if (rename.getStackSize() > 1) {
                enoughStackSize = PropertiesHelper.matchesRange(stack.getCount(), rename.getOriginalStackSize());
            }

            if (rename.getDamage() != null && rename.getDamage().damage > 0) {
                enoughDamage = PropertiesHelper.matchesRange(stack.getDamage(), rename.getOriginalDamage(), stack.getItem());
            }

            if (rename.getEnchantment() == null) {
                hasEnchant = true;
                hasEnoughLevels = true;
            } else {
                ItemEnchantmentsComponent enchantments;
                enchantments = EnchantmentHelper.getEnchantments(stack);

                for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
                    Optional<RegistryKey<Enchantment>> key = entry.getKey();
                    if (key.isEmpty()) continue;
                    Identifier id = key.get().getValue();
                    if (id == null) continue;
                    if (id.equals(rename.getEnchantment())) {
                        hasEnchant = true;
                        if (PropertiesHelper.matchesRange(enchantments.getLevel(entry), rename.getOriginalEnchantmentLevel())) {
                            hasEnoughLevels = true;
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Returns true if given stack's count passes rename's requirements, false otherwise.
         */
        public boolean enoughStackSize() {
            return enoughStackSize;
        }

        /**
         * Returns true if given stack's damage passes rename's requirements, false otherwise.
         */
        public boolean enoughDamage() {
            return enoughDamage;
        }

        /**
         * Returns true if given stack's enchantment list passes rename's requirements, false otherwise.
         */
        public boolean hasEnchant() {
            return hasEnchant;
        }

        /**
         * Returns true if given stack's enchantment levels passes rename's requirements, false otherwise.
         */
        public boolean hasEnoughLevels() {
            return hasEnoughLevels;
        }

        /**
         * Returns true if given stack passes rename's requirements, false if at least one does not.
         * Basically means "This stack can (not) be renamed with no additional changes".
         * Note that it does not take in count stack's item.
         */
        public boolean matches() {
            return enoughStackSize()
                    && enoughDamage()
                    && hasEnchant()
                    && hasEnoughLevels();
        }
    }

    public RenameRenderer getNewRenderer(RPRWidget rprWidget, boolean favorite) {
        return new CITRenameRenderer(this, rprWidget, favorite);
    }
}
