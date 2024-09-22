package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.config.favorite.FavoritesManager;
import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.HiWord9.RPRenames.util.rename.type.CEMRename;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RenamesHelper {
    private static final ModConfig config = ModConfig.INSTANCE;

    public static ItemStack[] getGhostCraftItems(AbstractRename rename) {
        ItemStack ghostSource = new ItemStack(rename.getItem());
        ItemStack ghostEnchant = ItemStack.EMPTY;
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
        ItemStack ghostEnchant = ItemStack.EMPTY;
        if (rename.getEnchantment() != null) {
            ghostEnchant = new ItemStack(Items.ENCHANTED_BOOK);
            enchantItemStackWithRename(rename, ghostEnchant);
        }

        return ghostEnchant;
    }

    public static void enchantItemStackWithRename(CITRename rename, ItemStack itemStack) {
        if (MinecraftClient.getInstance().world == null) {
            RPRenames.LOGGER.warn(
                    "Could not enchant item stack {} with rename\n{}\ncause client world is null",
                    itemStack, rename
            );
            return;
        }

        Optional<RegistryEntry.Reference<Enchantment>> optionalEnchantment = MinecraftClient.getInstance()
                .world
                .getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
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

    public static ItemStack createItemOrSpawnEgg(AbstractRename rename) {
        return createItemOrSpawnEgg(rename, 0);
    }

    public static ItemStack createItemOrSpawnEgg(AbstractRename rename, int itemIndex) {
        if (rename instanceof CEMRename cemRename && config.generateSpawnEggsInItemGroup) return cemRename.toSpawnEgg();
        return rename.toStack(itemIndex);
    }

    public static ArrayList<AbstractRename> search(ArrayList<AbstractRename> list, String match) {
        ArrayList<AbstractRename> cutList = new ArrayList<>();
        if (match.startsWith("#")) {
            String matchTag = match.substring(1);
            if (matchTag.contains(" ") && !matchTag.toUpperCase(Locale.ROOT).contains("#REGEX:") && !matchTag.toUpperCase(Locale.ROOT).contains("#IREGEX:")) {
                matchTag = matchTag.substring(0, matchTag.indexOf(" "));
            } else if (matchTag.contains(" #")) {
                matchTag = matchTag.substring(0, matchTag.indexOf(" #"));
            }
            if (matchTag.toUpperCase(Locale.ROOT).startsWith("REGEX:") || matchTag.toUpperCase(Locale.ROOT).startsWith("IREGEX:")) {
                String regex = matchTag;
                boolean caseInsensitive = false;
                if (matchTag.toUpperCase(Locale.ROOT).startsWith("I")) {
                    regex = regex.substring(1);
                    caseInsensitive = true;
                }
                regex = regex.substring(6);

                boolean isRegex;
                try {
                    Pattern.compile(regex);
                    isRegex = true;
                } catch (PatternSyntaxException e) {
                    isRegex = false;
                }

                if (isRegex) {
                    for (AbstractRename r : list) {
                        if (caseInsensitive ? r.getName().toUpperCase(Locale.ROOT).matches(regex.toUpperCase(Locale.ROOT)) : r.getName().matches(regex)) {
                            cutList.add(r);
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("PACK:") || matchTag.toUpperCase(Locale.ROOT).startsWith("PACKNAME:")) {
                String packName = matchTag.substring(4);
                while (packName.charAt(0) != ':') {
                    packName = packName.substring(1);
                }
                packName = packName.substring(1);
                for (AbstractRename r : list) {
                    if (r.getPackName() != null
                            && r.getPackName().replace(" ", "_").toUpperCase(Locale.ROOT)
                            .contains(packName.toUpperCase(Locale.ROOT))) {
                        cutList.add(r);
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("ITEM:")) {
                String itemName = matchTag.substring(5);
                for (AbstractRename r : list) {
                    for (Item item : r.getItems()) {
                        if (ParserHelper.idFromItem(item).toUpperCase(Locale.ROOT).contains(itemName.toUpperCase(Locale.ROOT))) {
                            cutList.add(r);
                            break;
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("STACKSIZE:") || matchTag.toUpperCase(Locale.ROOT).startsWith("STACK:") || matchTag.toUpperCase(Locale.ROOT).startsWith("SIZE:")) {
                String stackSize = matchTag.toUpperCase(Locale.ROOT).substring(4);
                while (stackSize.charAt(0) != ':') {
                    stackSize = stackSize.substring(1);
                }
                stackSize = stackSize.substring(1);
                if (stackSize.matches("[0-9]+")) {
                    for (AbstractRename r : list) {
                        if (r instanceof CITRename citRename) {
                            if (PropertiesHelper.matchesRange(Integer.parseInt(stackSize), citRename.getOriginalStackSize())) {
                                cutList.add(r);
                            }
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("DAMAGE:")) {
                String damage = matchTag.substring(7);
                if (damage.matches("[0-9]{1,9}")) {
                    for (AbstractRename r : list) {
                        if (r instanceof CITRename citRename) {
                            for (Item item : citRename.getItems()) {
                                if (PropertiesHelper.matchesRange(Integer.parseInt(damage), citRename.getOriginalDamage(), item)) {
                                    cutList.add(r);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("ENCH:") || matchTag.toUpperCase(Locale.ROOT).startsWith("ENCHANT:") || matchTag.toUpperCase(Locale.ROOT).startsWith("ENCHANTMENT:")) {
                String enchant = matchTag.toUpperCase(Locale.ROOT).substring(4);
                while (enchant.charAt(0) != ':') {
                    enchant = enchant.substring(1);
                }
                enchant = enchant.substring(1);
                for (AbstractRename r : list) {
                    if (r instanceof CITRename citRename) {
                        if (citRename.getEnchantment() != null) {
                            ArrayList<String> split = PropertiesHelper.splitList(citRename.getOriginalEnchantment());
                            for (String s : split) {
                                if (s.toUpperCase(Locale.ROOT).contains(enchant)) {
                                    cutList.add(r);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("FAV:") || matchTag.toUpperCase(Locale.ROOT).startsWith("FAVORITE:")) {
                for (AbstractRename r : list) {
                    for (Item item : r.getItems()) {
                        if (FavoritesManager.getInstance().isFavorite(item, r.getName())) {
                            cutList.add(r);
                            break;
                        }
                    }
                }
            }
            if (match.substring(1).contains(" ") && !matchTag.toUpperCase(Locale.ROOT).contains("#REGEX:") && !matchTag.toUpperCase(Locale.ROOT).contains("#IREGEX:")) {
                cutList = search(cutList, match.substring(match.indexOf(" ") + 1));
            } else if (match.substring(1).contains(" #")) {
                cutList = search(cutList, match.substring(match.indexOf(" #") + 1));
            }
        } else {
            if (match.startsWith("\\#")) {
                match = match.substring(1);
            }
            boolean isRegex = false;
            try {
                Pattern.compile(match);
                isRegex = true;
            } catch (Exception ignored) {
            }
            for (AbstractRename r : list) {
                if (r.getName().toUpperCase(Locale.ROOT).contains(match.toUpperCase(Locale.ROOT)) || (isRegex && r.getName().toUpperCase(Locale.ROOT).matches(match.toUpperCase(Locale.ROOT)))) {
                    cutList.add(r);
                }
            }
        }
        return cutList;
    }
}
