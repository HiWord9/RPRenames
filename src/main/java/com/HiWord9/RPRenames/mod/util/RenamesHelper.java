package com.HiWord9.RPRenames.mod.util;

import com.HiWord9.RPRenames.mod.RPRenames;
import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.mod.impl.rename.CEMRename;
import com.HiWord9.RPRenames.mod.impl.rename.CITRename;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Optional;

import static com.HiWord9.RPRenames.mod.util.Util.*;

public class RenamesHelper {
    public static ItemStack[] getGhostCraftItems(Rename rename) {
        ItemStack ghostSource = new ItemStack(rename.getItem());
        ItemStack ghostEnchant = null;
        ItemStack ghostResult = rename.toStack();

        if (rename instanceof CITRename citRename) {
            ghostSource.setCount(citRename.getStackSize());
            if (citRename.getDamage() != null) {
                ghostSource.setDamage(citRename.getDamage().getParsedDamage(ghostSource.getItem()));
            }

            ghostEnchant = getGhostCraftEnchant(citRename);
        }

        return new ItemStack[]{ghostSource, ghostEnchant, ghostResult};
    }

    public static ItemStack getGhostCraftEnchant(CITRename rename) {
        ItemStack ghostEnchant = null;
        if (rename.getEnchantment() != null) {
            ghostEnchant = new ItemStack(Items.ENCHANTED_BOOK);
            enchantItemStackWithRename(rename, ghostEnchant);
        }

        return ghostEnchant;
    }

    public static void enchantItemStackWithRename(CITRename rename, ItemStack itemStack) {
        if (client().world == null) {
            RPRenames.LOGGER.warn(
                    "Could not enchant item stack {} with rename\n{}\ncause client world is null",
                    itemStack, rename
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
                    itemStack, rename, RegistryKeys.ENCHANTMENT.getRegistry()
            );
            return;
        }

        Optional<RegistryEntry.Reference<Enchantment>> optionalEnchantment = optionalRegistry
                .get()
                .getEntry(rename.getEnchantment());

        if (optionalEnchantment.isPresent()) {
            itemStack.addEnchantment(optionalEnchantment.get(), rename.getEnchantmentLevel());
        } else {
            RPRenames.LOGGER.warn(
                    "Could not enchant item stack {} with rename\n{}\ncause enchantment {} is not loaded",
                    itemStack, rename, rename.getEnchantment()
            );
        }
    }

    public static ItemStack createItemOrSpawnEgg(Rename rename) {
        return createItemOrSpawnEgg(rename, 0);
    }

    public static ItemStack createItemOrSpawnEgg(Rename rename, int itemIndex) {
        if (rename instanceof CEMRename cemRename && config().generateSpawnEggsInItemGroup)
            return cemRename.toSpawnEgg();
        return rename.toStack(itemIndex);
    }
}
