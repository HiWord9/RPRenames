package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.FavoritesManager;
import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.config.generation.CEMList;
import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RenamesHelper {
    private static final ModConfig config = ModConfig.INSTANCE;

    public static ItemStack[] getGhostCraftItems(Rename rename) {
        ItemStack ghostSource = new ItemStack(rename.getItems().get(0));
        ghostSource.setCount(rename.getStackSize());
        if (rename.getDamage() != null) {
            ghostSource.setDamage(rename.getDamage().getParsedDamage(ghostSource.getItem()));
        }

        ItemStack ghostEnchant = getGhostCraftEnchant(rename);

        ItemStack ghostResult = createItem(rename);

        return new ItemStack[]{ghostSource, ghostEnchant, ghostResult};
    }

    public static ItemStack getGhostCraftEnchant(Rename rename) {
        ItemStack ghostEnchant = ItemStack.EMPTY;
        if (rename.getEnchantment() != null) {
            ghostEnchant = new ItemStack(Items.ENCHANTED_BOOK);
            enchantItemStackWithRename(rename, ghostEnchant);
        }

        return ghostEnchant;
    }

    public static void enchantItemStackWithRename(Rename rename, ItemStack itemStack) {
        itemStack.getOrCreateNbt();
        assert itemStack.getNbt() != null;
        if (!itemStack.getNbt().contains("Enchantments", 9)) {
            itemStack.getNbt().put("Enchantments", new NbtList());
        }
        NbtList nbtList = itemStack.getNbt().getList("Enchantments", 10);
        nbtList.add(EnchantmentHelper.createNbt(new Identifier(rename.getEnchantment()), rename.getEnchantmentLevel()));
    }

    public static ItemStack createItemOrSpawnEgg(Rename rename) {
        return createItemOrSpawnEgg(rename, true, 0);
    }

    public static ItemStack createItemOrSpawnEgg(Rename rename, boolean withCustomName, int itemIndex) {
        if (rename.isCEM() && config.generateSpawnEggsInItemGroup) return createSpawnEgg(rename);
        return createItem(rename, withCustomName, itemIndex);
    }

    public static ItemStack createItem(Rename rename) {
        return createItem(rename, true, 0);
    }

    public static ItemStack createItem(Rename rename, boolean withCustomName, int itemIndex) {
        ItemStack item = new ItemStack(rename.getItems().get(itemIndex >= rename.getItems().size() ? 0 : itemIndex));
        if (withCustomName) item.setCustomName(Text.of(rename.getName()));
        item.setCount(rename.getStackSize());
        if (rename.getDamage() != null) {
            item.setDamage(rename.getDamage().getParsedDamage(item.getItem()));
        }
        if (rename.getEnchantment() != null) {
            enchantItemStackWithRename(rename, item);
        }
        return item;
    }

    public static ItemStack createSpawnEgg(Rename rename) {
        Item item = SpawnEggItem.forEntity(CEMList.EntityFromName(rename.getMob().entity()));
        ItemStack spawnEgg = new ItemStack(item == null ? Items.ALLAY_SPAWN_EGG : item);
        spawnEgg.setCustomName(Text.of(rename.getName()));
        NbtCompound nbt = spawnEgg.getOrCreateNbt();
        NbtCompound nbtName = new NbtCompound();
        nbtName.putString("CustomName", rename.getName());
        nbt.put("EntityTag", nbtName);
        if (item == null) {
            NbtCompound nbt2 = nbt.getCompound("EntityTag");
            nbt2.putString("id", rename.getMob().entity());
        }
        return spawnEgg;
    }

    public static ArrayList<Rename> search(ArrayList<Rename> list, String match) {
        ArrayList<Rename> cutList = new ArrayList<>();
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
                    for (Rename r : list) {
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
                for (Rename r : list) {
                    if (r.getPackName() != null && r.getPackName().replace(" ", "_").toUpperCase(Locale.ROOT).contains(packName.toUpperCase(Locale.ROOT))) {
                        cutList.add(r);
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("ITEM:")) {
                String itemName = matchTag.substring(5);
                for (Rename r : list) {
                    if (r.getItems() == null) break;
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
                    for (Rename r : list) {
                        if (PropertiesHelper.matchesRange(Integer.parseInt(stackSize), r.getOriginalStackSize())) {
                            cutList.add(r);
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("DAMAGE:")) {
                String damage = matchTag.substring(7);
                if (damage.matches("[0-9]+")) {
                    for (Rename r : list) {
                        for (Item item : r.getItems()) {
                            if (PropertiesHelper.matchesRange(Integer.parseInt(damage), r.getOriginalDamage(), item)) {
                                cutList.add(r);
                                break;
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
                for (Rename r : list) {
                    if (r.getEnchantment() != null) {
                        ArrayList<String> split = PropertiesHelper.splitList(r.getOriginalEnchantment());
                        for (String s : split) {
                            if (s.toUpperCase(Locale.ROOT).contains(enchant)) {
                                cutList.add(r);
                                break;
                            }
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("FAV:") || matchTag.toUpperCase(Locale.ROOT).startsWith("FAVORITE:")) {
                for (Rename r : list) {
                    for (Item item : r.getItems()) {
                        if (FavoritesManager.isFavorite(item, r.getName())) {
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
            for (Rename r : list) {
                if (r.getName().toUpperCase(Locale.ROOT).contains(match.toUpperCase(Locale.ROOT)) || (isRegex && r.getName().toUpperCase(Locale.ROOT).matches(match.toUpperCase(Locale.ROOT)))) {
                    cutList.add(r);
                }
            }
        }
        return cutList;
    }
}
