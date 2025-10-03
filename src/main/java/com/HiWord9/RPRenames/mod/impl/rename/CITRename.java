package com.HiWord9.RPRenames.mod.impl.rename;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.mod.util.PropertiesHelper;
import com.HiWord9.RPRenames.mod.util.RenamesHelper;
import com.HiWord9.RPRenames.mod.impl.rename.renderer.builder.CITRenameRendererBuilder;
import com.HiWord9.RPRenames.api.rename.renderer.builder.RenameRendererBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

public class CITRename extends ResourcePackRename implements HasProperties, HasNamePattern, HasDescription {
    protected final Integer stackSize;
    protected final Damage damage;
    protected final Identifier enchantment;
    protected final Integer enchantmentLevel;
    protected String description;

    protected final Properties properties;

    public CITRename(String name, Item... items) {
        this(name, null, null, null, null, null, null, null, null, items);
    }

    public CITRename(
            String name,
            String packName,
            String path,
            Integer stackSize,
            Damage damage,
            Identifier enchantment,
            Integer enchantmentLevel,
            Properties properties,
            String description,
            Item... items
    ) {
        super(name, packName, path, items);
        this.stackSize = stackSize;
        this.damage = damage;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
        this.description = description;
        this.properties = properties;
    }

    @Override
    public String getOriginalNamePattern() {
        return properties == null ? null : PropertiesHelper.getCustomName(properties);
    }

    @Override
    public Pattern getNamePattern() {
        return PropertiesHelper.getPropPattern(getOriginalNamePattern());
    }

    @Override
    public String getDescription() {
        return description;
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

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public boolean baseEquals(Rename rename) {
        return stackSize == null
                && damage == null
                && enchantment == null
                && enchantmentLevel == null
                && super.equals(rename);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                && obj instanceof CITRename citRename
                && Objects.equals(stackSize, citRename.stackSize)
                && Objects.equals(damage, citRename.damage)
                && Objects.equals(enchantment, citRename.enchantment)
                && Objects.equals(enchantmentLevel, citRename.enchantmentLevel);
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
        var item = super.toStack(index);
        item.setCount(getStackSize());
        if (getDamage() != null) {
            item.setDamage(getDamage().getParsedDamage(item.getItem()));
        }
        if (getEnchantment() != null) {
            RenamesHelper.enchantItemStackWithRename(this, item);
        }
        return item;
    }

    @Override
    public boolean matchesStack(ItemStack stack) {
        boolean bl = false;
        var namePattern = getNamePattern();
        if (namePattern == null) {
            bl = super.matchesStack(stack);
        } else {
            if (getItems().contains(stack.getItem())) {
                var customName = stack.get(DataComponentTypes.CUSTOM_NAME);
                if (customName != null) {
                    bl = namePattern.matcher(customName.getString()).matches();
                }
            }
        }
        return bl && new CraftMatcher(this, stack).matches();
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

    public RenameRendererBuilder<CITRename> getNewRendererBuilder() {
        return new CITRenameRendererBuilder(this);
    }
}
