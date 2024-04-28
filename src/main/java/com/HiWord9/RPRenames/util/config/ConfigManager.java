package com.HiWord9.RPRenames.util.config;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.generation.CEMConfig;
import com.HiWord9.RPRenames.util.config.generation.CEMList;
import com.HiWord9.RPRenames.util.config.generation.CITConfig;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigManager {
    private static final ModConfig config = ModConfig.INSTANCE;

    public static void parseRenames() {
        MinecraftClient client = MinecraftClient.getInstance();
        parseRenames(client.getResourceManager(), client.getProfiler());
    }

    public static void parseRenames(ResourceManager resourceManager, Profiler profiler) {
        profiler.push("rprenames:reloading_renames");

        configClear();
        long startTime = System.currentTimeMillis();
        RPRenames.LOGGER.info("Starting collecting renames");

        CITConfig.parseCITs(resourceManager, profiler);
        CEMConfig.parseCEMs(resourceManager, profiler);

        long finishTime = System.currentTimeMillis() - startTime;
        updateItemGroup();
        RPRenames.LOGGER.info("Finished collecting renames [{}.{}s]", finishTime / 1000, finishTime % 1000);

        profiler.pop();
    }

    public static void updateItemGroup() {
        RPRenames.renamedItemStacks.clear();
        ArrayList<ItemStack> list = new ArrayList<>();
        ArrayList<Rename> parsedRenames = new ArrayList<>();
        for (String key : RPRenames.renames.keySet()) {
            for (Rename r : RPRenames.renames.get(key)) {
                if (parsedRenames.contains(r)) continue;
                parsedRenames.add(r);
                if (r.getItems().size() > 1 && !config.compareItemGroupRenames) {
                    for (int i = 0; i < r.getItems().size(); i++) {
                        ItemStack stack = createItem(r, true, i);
                        list.add(stack);
                    }
                } else {
                    ItemStack stack = createItemOrSpawnEgg(r);
                    list.add(stack);
                }
            }
        }
        RPRenames.renamedItemStacks.addAll(list);
    }

    public static Properties getPropFromResource(Resource resource) throws IOException {
        Properties prop = new Properties();
        prop.load(resource.getInputStream());
        return prop;
    }

    public static String getFullPathFromIdentifier(String packName, Identifier identifier) {
        return validatePackName(packName) + "/assets/" + identifier.getNamespace() + "/" + identifier.getPath();
    }

    public static String validatePackName(String packName) {
        return packName.startsWith("file/") ? packName.substring(5) : packName;
    }

    public static Map<String, ArrayList<Rename>> getAllFavorites() {
        Map<String, ArrayList<Rename>> favoriteRenames = new HashMap<>();
        File[] files = RPRenames.configPathFavorite.toFile().listFiles();
        if (files == null) return favoriteRenames;
        for (File file : files) {
            String fileName = file.getName();
            String item = fileName.substring(0, fileName.length() - 5);
            favoriteRenames.put(item.replace(".", ":"), getFavorites(item));
        }
        return favoriteRenames;
    }

    public static ArrayList<Rename> getFavorites(String item) {
        ArrayList<Rename> renames = new ArrayList<>();
        File favoritesFile = new File(RPRenames.configPathFavorite + File.separator + item.replace(":", ".") + ".json");
        if (favoritesFile.exists()) {
            try {
                FileReader fileReader = new FileReader(favoritesFile);
                Type type = new TypeToken<ArrayList<Rename>>() {
                }.getType();
                Gson gson = new Gson();
                renames = gson.fromJson(fileReader, type);
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fixRenameItemsIfNeeded(renames, item);

        return renames;
    }

    private static void fixRenameItemsIfNeeded(ArrayList<Rename> renames, String item) {
        boolean fix = false;
        for (Rename rename : renames) {
            if (rename.getItems() != null) continue;
            RPRenames.LOGGER.error("Fixing items list for favorite Rename \"" + rename.getName() + "\". Looks like it was created in ver <0.8.0");
            rename.setItems(new ArrayList<>(List.of(item)));
            fix = true;
        }
        if (!fix) return;
        RPRenames.LOGGER.warn("Recreating Favorite Renames List File for \"" + item + "\" with fixed Items.");
        deleteFavoriteConfigFile(item);
        for (Rename rename : renames) {
            ConfigManager.addToFavorites(rename.getName(), item);
        }
    }

    public static void addToFavorites(String favoriteName, String item) {
        ArrayList<Rename> listNames = new ArrayList<>();
        Rename rename = new Rename(favoriteName, item);
        ArrayList<Rename> alreadyExist = ConfigManager.getFavorites(item);
        if (!alreadyExist.isEmpty()) {
            ArrayList<Rename> newConfig = new ArrayList<>(alreadyExist);
            newConfig.add(rename);
            listNames = newConfig;
        } else {
            if (RPRenames.configPathFavorite.toFile().mkdirs()) {
                RPRenames.LOGGER.info("Created folder for favorites config: {}", RPRenames.configPathFavorite);
            }
            RPRenames.LOGGER.info("Created new file for favorites config: {}{}{}.json", RPRenames.configPathFavorite, File.separator, item.replaceAll(":", "."));
            listNames.add(rename);
        }


        writeFavoriteFile(listNames, item);
    }

    public static void removeFromFavorites(String favoriteName, String item) {
        ArrayList<Rename> renamesList = ConfigManager.getFavorites(item);
        int indexInRenamesList = new Rename(favoriteName).indexIn(renamesList, true);
        if (indexInRenamesList >= 0) {
            renamesList.remove(indexInRenamesList);
        }

        if (!renamesList.isEmpty()) {
            writeFavoriteFile(renamesList, item);
        } else {
            deleteFavoriteConfigFile(item);
        }
    }

    private static void writeFavoriteFile(ArrayList<Rename> renames, String item) {
        try {
            FileWriter fileWriter = new FileWriter(RPRenames.configPathFavorite + File.separator + item.replaceAll(":", ".") + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(renames, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void deleteFavoriteConfigFile(String item) {
        try {
            Files.deleteIfExists(Path.of(RPRenames.configPathFavorite + File.separator + item.replaceAll(":", ".") + ".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFirstName(String nbtDisplayName) {
        return getFirstName(nbtDisplayName, null);
    }

    public static String getFirstName(String nbtDisplayName, @Nullable ArrayList<String> items) {
        String name = parseEscapes(nbtDisplayName);
        if (name.startsWith("pattern:") || name.startsWith("ipattern:")) {
            if (name.startsWith("i")) {
                name = name.substring(1);
            }
            name = name.replaceFirst("pattern:", "");
            name = name.replace("*", "");
            name = name.replace("?", "_");
        } else if (name.startsWith("regex:") || name.startsWith("iregex")) {
            if (name.startsWith("i")) {
                name = name.substring(1);
            }
            name = name.replaceFirst("regex:", "");
            name = name.replace(".*", "");
            name = name.replace(".+", "_");

            String originalRegex = name;
            name = solveRegex(name);
            try {
                if (!name.matches(originalRegex)) {
                    RPRenames.LOGGER.error("Couldn't get valid string from regex" + (items != null ? " for " + items : ""));
                    RPRenames.LOGGER.error("regex:" + originalRegex);
                    RPRenames.LOGGER.error("received string:" + name);
                }
            } catch (PatternSyntaxException e) {
                RPRenames.LOGGER.error("INVALID REGEX");
            }
        }
        return name;
    }

    public static String parseEscapes(String string) {
        char[] chars = string.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\\') {
                if (chars[i + 1] == 'u') {
                    char unicode;
                    String unicodeNumbers = string.substring(i + 2, i + 6);
                    try {
                        unicode = (char) Integer.parseInt(unicodeNumbers, 16);
                    } catch (Exception e) {
                        stringBuilder.append(chars[i]);
                        RPRenames.LOGGER.warn("Invalid unicode \"" + unicodeNumbers + "\" for String: " + string);
                        continue;
                    }
                    i += 5;
                    stringBuilder.append(unicode);
                } else if (chars[i + 1] == 'n') {
                    stringBuilder.append("\n");
                } else if (chars[i + 1] == 'r') {
                    stringBuilder.append("\r");
                } else if (chars[i + 1] == 'f') {
                    stringBuilder.append("\f");
                } else if (chars[i + 1] == 't') {
                    stringBuilder.append("\t");
                } else {
                    stringBuilder.append(chars[i]);
                    i++;
                }
            } else {
                stringBuilder.append(chars[i]);
            }
        }
        return stringBuilder.toString();
    }

    public static String solveRegex(String string) {
        if (!string.startsWith("(")) {
            string = "(" + string + ")";
        }
        return initialSolveRegex(string);
    }

    public static String initialSolveRegex(String string) {
        try {
            StringBuilder builder = new StringBuilder();
            char[] chars = string.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                StringBuilder builder2 = new StringBuilder();
                if (chars[i] == '[') {
                    if (chars[i + 1] != '^') {
                        builder2.append(chars[i + 1]);
                        i += 2;
                        while (chars[i] != ']') {
                            i++;
                        }
                    } else {
                        int start = i;
                        i += 3;
                        while (chars[i] != ']') {
                            i++;
                        }
                        int ch = chars[start + 2];
                        int count = 0;
                        while (count != 65536) {
                            if (string.substring(start, i + 1).matches(String.valueOf((char) ch))) {
                                break;
                            }
                            ch++;
                            count++;
                            if (ch == 65536) {
                                ch = 0;
                            }
                            if (count == 65536) {
                                ch = 65535;
                            }
                        }
                        builder2.append((char) ch);
                    }
                } else if (chars[i] == '(') {
                    StringBuilder builder3 = new StringBuilder();
                    int brackets = 0;
                    ArrayList<Character> bracketsOrder = new ArrayList<>();
                    while (i + 1 < chars.length) {
                        i++;
                        if (chars[i] == '(') {
                            bracketsOrder.add('(');
                            brackets++;
                        } else if (chars[i] == ')' && (bracketsOrder.size() <= 0 || bracketsOrder.get(bracketsOrder.size() - 1) != '[')) {
                            if (brackets == 0) {
                                break;
                            }
                            bracketsOrder.add(')');
                            brackets--;
                        } else if (chars[i] == '[') {
                            bracketsOrder.add('[');
                            brackets++;
                        } else if (chars[i] == ']' && chars[i - 1] != '[') {
                            bracketsOrder.add(']');
                            brackets--;
                        }
                        builder3.append(chars[i]);
                    }
                    builder3 = new StringBuilder(initialSolveRegex(builder3.toString()));
                    if (!builder3.toString().startsWith("|") && !builder3.toString().endsWith("|") && !builder3.toString().contains("||") && !builder3.isEmpty()) {
                        for (int i1 = 0; i1 != builder3.length() && builder3.charAt(i1) != '|'; i1++) {
                            builder2.append(builder3.charAt(i1));
                        }
                    }
                } else if (chars[i] != '^' && chars[i] != '$') {
                    if (chars[i] == '\\') {
                        i++;
                    }
                    builder2.append(chars[i]);
                }

                if (i + 1 < chars.length && chars[i + 1] == '{') {
                    StringBuilder builder3 = new StringBuilder();
                    i += 2;
                    while (chars[i] != '}') {
                        builder3.append(chars[i]);
                        i++;
                    }
                    int s = builder3.indexOf(",") == -1 ? builder3.length() : builder3.indexOf(",");
                    if (s > 0 || s == builder3.length() - 1) {
                        builder.append(String.valueOf(builder2).repeat(Math.max(0, Integer.parseInt(builder3.substring(0, s)))));
                        continue;
                    } else if (s == 0) {
                        builder.append(String.valueOf(builder2).repeat(Math.max(0, Integer.parseInt(builder3.substring(s + 1)))));
                        continue;
                    }
                } else if (i + 1 < chars.length) {
                    if (chars[i + 1] == '*' || chars[i + 1] == '+' || chars[i + 1] == '?') {
                        i++;
                        if (chars[i] == '*' || chars[i] == '?') {
                            continue;
                        }
                    }
                }
                builder.append(builder2);
            }
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static Item itemFromName(String id) {
        return Registries.ITEM.get(new Identifier(id));
    }

    public static ArrayList<Rename> getAllRenames() {
        ArrayList<Rename> names = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Rename>> entry : RPRenames.renames.entrySet()) {
            for (Rename r : entry.getValue()) {
                if (!names.contains(r)) names.add(r);
            }
        }
        return names;
    }

    public static ArrayList<Rename> getRenames(String item) {
        if (RPRenames.renames.containsKey(item)) {
            return RPRenames.renames.get(item);
        } else {
            return new ArrayList<>();
        }
    }

    public static void addRename(String item, Rename rename) {
        if (RPRenames.renames.containsKey(item)) {
            Rename simplifiedRename = new Rename(rename.getName(),
                    rename.getItems(),
                    null,
                    null,
                    rename.getStackSize(),
                    rename.getDamage(),
                    rename.getEnchantment(),
                    rename.getEnchantmentLevel(),
                    null,
                    null,
                    null);
            if (!simplifiedRename.isContainedIn(RPRenames.renames.get(item), true)) {
                RPRenames.renames.get(item).add(rename);
            }
        } else {
            ArrayList<Rename> arrayList = new ArrayList<>();
            arrayList.add(rename);
            RPRenames.renames.put(item, arrayList);
        }
    }

    public static ItemStack[] getGhostCraftItems(Rename rename) {
        ItemStack ghostSource = new ItemStack(ConfigManager.itemFromName(rename.getItems().get(0)));
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
            enchantItemStack(rename, ghostEnchant);
        }

        return ghostEnchant;
    }

    public static void enchantItemStack(Rename rename, ItemStack itemStack) {
        itemStack.getOrCreateNbt();
        assert itemStack.getNbt() != null;
        if (!itemStack.getNbt().contains("Enchantments", 9)) {
            itemStack.getNbt().put("Enchantments", new NbtList());
        }
        NbtList nbtList = itemStack.getNbt().getList("Enchantments", 10);
        nbtList.add(EnchantmentHelper.createNbt(new Identifier(rename.getEnchantment()), rename.getEnchantmentLevel()));
    }

    public static ItemStack createItemOrSpawnEgg(Rename rename) {
        if (rename.isCEM() && config.generateSpawnEggsInItemGroup) return createSpawnEgg(rename);
        return createItem(rename);
    }

    public static ItemStack createItem(Rename rename) {
        return createItem(rename, true, 0);
    }

    public static ItemStack createItem(Rename rename, boolean withCustomName, int itemIndex) {
        ItemStack item = new ItemStack(ConfigManager.itemFromName(rename.getItems().get(itemIndex >= rename.getItems().size() ? 0 : itemIndex)));
        if (withCustomName) item.setCustomName(Text.of(rename.getName()));
        item.setCount(rename.getStackSize());
        if (rename.getDamage() != null) {
            item.setDamage(rename.getDamage().getParsedDamage(item.getItem()));
        }
        if (rename.getEnchantment() != null) {
            enchantItemStack(rename, item);
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

    public static String getIdAndPath(Item item) {
        String idAndPath = Registries.ITEM.getId(item).toString();
        if (idAndPath.startsWith("minecraft:")) {
            return idAndPath.substring(10);
        }
        return idAndPath;
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
                    for (String item : r.getItems()) {
                        if (item.toUpperCase(Locale.ROOT).contains(itemName.toUpperCase(Locale.ROOT))) {
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
                        if (Rename.isInBounds(Integer.parseInt(stackSize), r.getOriginalStackSize())) {
                            cutList.add(r);
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("DAMAGE:")) {
                String damage = matchTag.substring(7);
                if (damage.matches("[0-9]+")) {
                    for (Rename r : list) {
                        for (String item : r.getItems()) {
                            if (Rename.isInBounds(Integer.parseInt(damage), r.getOriginalDamage(), item)) {
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
                        ArrayList<String> split = Rename.split(r.getOriginalEnchantment());
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
                    for (String item : r.getItems()) {
                        if (Rename.isFavorite(item, r.getName())) {
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

    public static ArrayList<Text> parseCustomDescription(String description) {
        ArrayList<Text> lines = new ArrayList<>();
        String[] splited = description
                .replaceAll("\\\\&", String.valueOf(Formatting.FORMATTING_CODE_PREFIX))
                .split("\n");

        for (String s : splited) {
            MutableText line = Text.empty();
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                MutableText text = Text.empty();

                if (chars[i] == '\\'
                        && i != chars.length - 1
                        && chars[i + 1] == '#'
                        && i + 7 < chars.length) {
                    StringBuilder color = new StringBuilder();
                    for (int j = 2; j < 8; j++) {
                        color.append(chars[i + j]);
                    }
                    if (color.toString().matches("[0-9a-fA-F]*")) {
                        text.fillStyle(Style.EMPTY.withColor(Integer.parseInt(color.toString(), 16)));
                    } else {
                        text.append("\\#" + color);
                    }
                    i += 8;
                }

                StringBuilder stringBuilder = new StringBuilder();
                while (i < chars.length) {
                    if (chars[i] == '\\' && chars[i + 1] == '#') {
                        i--;
                        break;
                    }
                    stringBuilder.append(chars[i]);
                    i++;
                }

                text.append(Text.translatable(stringBuilder.toString()));
                line.append(text);
            }
            lines.add(line);
        }
        return lines;
    }

    public static void configClear() {
        RPRenames.renames.clear();
    }
}