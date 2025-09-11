package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.config.favorite.FavoritesManager;
import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import com.HiWord9.RPRenames.util.rename.type.Rename;
import com.HiWord9.RPRenames.util.rename.type.ResourcePackRename;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RenamesSearchEngine {
    public static List<Rename> search(List<Rename> list, String match) {
        return search(list, match, RPRenames.favoritesManager);
    }

    public static List<Rename> search(List<Rename> list, String match, FavoritesManager favoritesManager) {
        List<Rename> resultList = new ArrayList<>();
        if (match.startsWith("#")) {
            String matchTag = match.substring(1);
            String upMatchTag = up(matchTag);
            if (matchTag.contains(" ") && !upMatchTag.contains("REGEX:") && !upMatchTag.contains("IREGEX:")) {
                matchTag = matchTag.substring(0, matchTag.indexOf(" "));
            } else if (matchTag.contains(" #")) {
                matchTag = matchTag.substring(0, matchTag.indexOf(" #"));
            }

            String tagUp = beforeColon(upMatchTag);

            switch (tagUp) {
                case "REGEX", "IREGEX" -> handleRegex(list, matchTag, resultList);
                case "PACK", "PACKNAME" -> handlePackName(list, matchTag, resultList);
                case "ITEM" -> handleItem(list, matchTag, resultList);
                case "STACKSIZE", "STACK", "SIZE" -> handleStackSize(list, matchTag, resultList);
                case "DAMAGE" -> handleDamage(list, matchTag, resultList);
                case "ENCH", "ENCHANT", "ENCHANTMENT" -> handleEnchantment(list, matchTag, resultList);
                case "FAV", "FAVORITE" -> handleFavorite(list, favoritesManager, resultList);
            }

            if (match.substring(1).contains(" ") && !upMatchTag.contains("REGEX:") && !upMatchTag.contains("IREGEX:")) {
                resultList = search(resultList, match.substring(match.indexOf(" ") + 1), favoritesManager);
            } else if (match.substring(1).contains(" #")) {
                resultList = search(resultList, match.substring(match.indexOf(" #") + 1), favoritesManager);
            }
        } else {
            if (match.startsWith("\\#")) {
                match = match.substring(1);
            }
            for (Rename r : list) {
                if (up(r.getName()).contains(up(match))) {
                    resultList.add(r);
                }
            }
        }
        return resultList;
    }

    private static void handleRegex(List<Rename> renames, String regexTag, List<Rename> resultList) {
        boolean caseInsensitive = up(regexTag).startsWith("I");
        String regexText = afterColon(regexTag);

        try {
            Pattern pattern = caseInsensitive ?
                    Pattern.compile(regexText, Pattern.CASE_INSENSITIVE) :
                    Pattern.compile(regexText);

            for (Rename r : renames) {
                if (pattern.matcher(r.getName()).matches()) {
                    resultList.add(r);
                }
            }
        } catch (PatternSyntaxException ignored) {} // invalid pattern -> ignore
    }

    private static void handlePackName(List<Rename> renames, String packNameTag, List<Rename> resultList) {
        String packNameUp = up(afterColon(packNameTag));

        for (Rename r : renames) {
            if (!(r instanceof ResourcePackRename rpRename)) continue;
            if (rpRename.getPackName() == null) continue;

            if (up(rpRename.getPackName())
                    .replace(" ", "_")
                    .contains(packNameUp)
            ) resultList.add(rpRename);
        }
    }

    private static void handleItem(List<Rename> renames, String itemTag, List<Rename> resultList) {
        String itemNameUp = up(afterColon(itemTag));

        for (Rename r : renames) {
            for (Item item : r.getItems()) {
                if (up(ParserHelper.idFromItem(item)).contains(itemNameUp)) {
                    resultList.add(r);
                    break;
                }
            }
        }
    }

    private static void handleStackSize(List<Rename> renames, String stackSizeTag, List<Rename> resultList) {
        String stackSize = afterColon(stackSizeTag);
        if (!stackSize.matches("[0-9]{1,9}")) return;

        int stackSizeValue = Integer.parseInt(stackSize);

        for (Rename r : renames) {
            if (!(r instanceof CITRename citRename)) continue;

            if (PropertiesHelper.matchesRange(
                    stackSizeValue,
                    citRename.getOriginalStackSize()
            )) resultList.add(r);
        }
    }

    private static void handleDamage(List<Rename> renames, String damageTag, List<Rename> resultList) {
        String damage = afterColon(damageTag);
        if (!damage.matches("[0-9]{1,9}")) return;

        int damageValue = Integer.parseInt(damage);
        for (Rename r : renames) {
            if (!(r instanceof CITRename citRename)) continue;

            String originalDamage = citRename.getOriginalDamage();
            for (Item item : citRename.getItems()) {
                if (PropertiesHelper.matchesRange(damageValue, originalDamage, item)) {
                    resultList.add(r);
                    break;
                }
            }
        }
    }

    private static void handleEnchantment(List<Rename> renames, String enchantmentTag, List<Rename> resultList) {
        String enchantUp = up(afterColon(enchantmentTag));

        for (Rename r : renames) {
            if (!(r instanceof CITRename citRename)) continue;
            if (citRename.getEnchantment() == null) continue;

            var split = PropertiesHelper.splitList(citRename.getOriginalEnchantment());
            for (String s : split) {
                if (up(s).contains(enchantUp)) {
                    resultList.add(r);
                    break;
                }
            }
        }
    }

    private static void handleFavorite(List<Rename> renames, FavoritesManager favoritesManager, List<Rename> resultList) {
        for (Rename r : renames) {
            for (Item item : r.getItems()) {
                if (favoritesManager.isFavorite(item, r.getName())) {
                    resultList.add(r);
                    break;
                }
            }
        }
    }

    private static @NotNull String afterColon(String matchTag) {
        int i;
        return (i = matchTag.indexOf(':')) == -1 ? "" : matchTag.substring(i + 1);
    }

    private static @NotNull String beforeColon(String matchTag) {
        int i;
        return (i = matchTag.indexOf(':')) == -1 ? matchTag : matchTag.substring(0, i);
    }

    private static @NotNull String up(String string) {
        return string.toUpperCase(Locale.ROOT);
    }
}
