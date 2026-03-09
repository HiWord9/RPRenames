package com.hiword9.rprenames.mod.impl.rename;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.core.rename.renderer.RenameRenderer;
import com.hiword9.rprenames.api.ext.rename.HasDescription;
import com.hiword9.rprenames.api.ext.rename.HasResourcePack;
import com.hiword9.rprenames.api.ext.rename.Informative;
import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.widget.GhostCraft;
import com.hiword9.rprenames.mod.impl.rename.renderer.CITRenameRenderer;
import com.hiword9.rprenames.mod.item_group.ItemGroupComponent;
import com.hiword9.rprenames.mod.util.PropertiesHelper;
import com.hiword9.rprenames.mod.util.RenameInfoHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.regex.Pattern;

import static com.hiword9.rprenames.mod.util.Util.client;
import static com.hiword9.rprenames.mod.util.Util.config;

public class CITRename
        extends Rename
        implements HasResourcePack, HasDescription, ItemGroupComponent, GhostCraft.Loader, Informative
{
    protected final String packName;
    protected final String path;
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
        super(Text.of(name), items);
        this.packName = packName;
        this.path = path;
        this.stackSize = stackSize;
        this.damage = damage;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
        this.description = description;
        this.properties = properties;
    }

    public String getOriginalNamePattern() {
        return properties == null ? null : PropertiesHelper.getCustomName(properties);
    }

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

    @Override
    public String getPackName() {
        return packName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public List<Text> getInfo() {
        var info = new ArrayList<Text>();
        info.addAll(RenameInfoHelper.getProperties(properties));
        info.addAll(RenameInfoHelper.getRPPath(packName, path));
        return info;
    }

    @Override
    public List<ItemStack> getItemGroupStacks() {
        if (config().compareItemGroupRenames) return List.of(toStack());
        return toStackAll();
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
            enchantItemStack(item);
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

    @Override
    public void loadGhostCraft(GhostCraft craft, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            var stacks = new ItemStack[craft.length];

            var source = new ItemStack(getItem());
            source.setCount(getStackSize());
            if (getDamage() != null) {
                source.setDamage(getDamage().getParsedDamage(source.getItem()));
            }

            var enchant = getEnchantingStack();
            var result = toStack();

            if (craft.length >= 2) {
                stacks[0] = source;
                stacks[1] = enchant;
                stacks[craft.length - 1] = result;
            }

            craft.setStacks(stacks);
            craft.setRender(true);
            return;
        }

        var craftMatcher = new CraftMatcher(this, itemStack);
        if (!craftMatcher.enoughStackSize() || !craftMatcher.enoughDamage()) {
            var highlights = new Boolean[craft.length];
            if (craft.length >= 1) {
                highlights[0] = true;
                highlights[craft.length - 1] = true;
            }
            craft.setSpecialHighlight(highlights);

            craft.setRender(true);
        }
        if (!craftMatcher.hasEnchant() || !craftMatcher.hasEnoughLevels()) {
            var stacks = new ItemStack[craft.length];
            if (craft.length >= 2)
                stacks[1] = getEnchantingStack();
            craft.setStacks(stacks);

            var highlights = new Boolean[craft.length];
            if (craft.length >= 1)
                highlights[craft.length - 1] = true;
            craft.setSpecialHighlight(highlights);

            craft.setRender(true);
        }
    }

    protected ItemStack getEnchantingStack() {
        ItemStack ghostEnchant = null;
        if (getEnchantment() != null) {
            ghostEnchant = new ItemStack(Items.ENCHANTED_BOOK);
            enchantItemStack(ghostEnchant);
        }
        return ghostEnchant;
    }

    public void enchantItemStack(ItemStack itemStack) {
        if (client().world == null) {
            RPRenames.LOGGER.warn(
                    "Could not enchant item stack {} with rename\n{}\ncause client world is null",
                    itemStack, this
            );
            return;
        }

        Optional<Registry<Enchantment>> optionalRegistry = client()
                .world
                .getRegistryManager()
                .getOptional(RegistryKeys.ENCHANTMENT);

        if (optionalRegistry.isEmpty()) {
            RPRenames.LOGGER.warn(
                    "Could not enchant item stack {} with rename\n{}\ncause {} registry was not found",
                    itemStack, this, RegistryKeys.ENCHANTMENT.getRegistry()
            );
            return;
        }

        Optional<RegistryEntry.Reference<Enchantment>> optionalEnchantment = optionalRegistry
                .get()
                .getEntry(this.getEnchantment());

        if (optionalEnchantment.isPresent()) {
            itemStack.addEnchantment(optionalEnchantment.get(), this.getEnchantmentLevel());
        } else {
            RPRenames.LOGGER.warn(
                    "Could not enchant item stack {} with rename\n{}\ncause enchantment {} is not loaded",
                    itemStack, this, this.getEnchantment()
            );
        }
    }

    public RenameRenderer.Builder<CITRename> getNewRendererBuilder(RenameRenderer.RenderArea renderArea) {
        return new CITRenameRenderer.Builder(this, renderArea);
    }
}
