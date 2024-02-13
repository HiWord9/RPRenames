package com.HiWord9.RPRenames.util.config;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.generation.CEMConfig;
import com.HiWord9.RPRenames.util.config.generation.CEMList;
import com.HiWord9.RPRenames.util.config.generation.CITConfig;
import com.HiWord9.RPRenames.util.gui.RenameButtonHolder;
import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigManager {
    private static final ModConfig config = ModConfig.INSTANCE;

    public static void configUpdate() {
        configUpdate(MinecraftClient.getInstance().getResourcePackManager().getEnabledProfiles().stream().toList());
    }

    public static void configUpdate(List<ResourcePackProfile> enabledPacks) {
        ArrayList<String> packs = new ArrayList<>();
        for (ResourcePackProfile rpp : enabledPacks) {
            packs.add(rpp.getName());
        }
        configUpdate(packs);
    }

    public static void configUpdateServer() {
        RPRenames.renamesServer.clear();
        long startTime = System.currentTimeMillis();
        RPRenames.LOGGER.info("Starting creating config server");
        startConfigCreateServer();
        long finishTime = System.currentTimeMillis() - startTime;
        RPRenames.LOGGER.info("Finished creating config server [" + finishTime / 1000 + "." + finishTime % 1000 + "s]");
    }

    public static void configUpdate(ArrayList<String> enabledPacks) {
        configClear();
        long startTime = System.currentTimeMillis();
        RPRenames.LOGGER.info("Starting creating config");
        startConfigCreate(enabledPacks);
        long finishTime = System.currentTimeMillis() - startTime;
        updateItemGroup();
        RPRenames.LOGGER.info("Finished creating config [" + finishTime / 1000 + "." + finishTime % 1000 + "s]");
    }

    public static void updateItemGroup() {
        System.out.println("updating item group");
        RPRenames.renamedItemStacks.clear();
        ArrayList<ItemStack> list = new ArrayList<>();
        ArrayList<Rename> parsedRenames = new ArrayList<>();
        for (String key : RPRenames.renames.keySet()) {
            for (Rename r : RPRenames.renames.get(key)) {
                if (parsedRenames.contains(r)) continue;
                parsedRenames.add(r);
                if (!config.compareItemGroupRenames) {
                    for (int i = 0; i < r.getItems().size(); i++) {
                        ItemStack stack = RenameButtonHolder.createItem(r, true, i);
                        list.add(stack);
                    }
                } else {
                    ItemStack stack = RenameButtonHolder.createItem(r);
                    list.add(stack);
                }
            }
        }
        RPRenames.renamedItemStacks.addAll(list);
    }

    public static void startConfigCreate(ArrayList<String> enabledPacks) {
        for (String s : enabledPacks) {
            if (s.startsWith("file/")) {
                String packName = s.substring(5);
                RPRenames.LOGGER.info("Starting creating config for \"" + packName + "\".");
                configCreate(packName);
            }
            if (s.equals("server")) {
                startConfigCreateServer();
            }
        }
    }

    public static void startConfigCreateServer() {
        URL url = RPRenames.serverResourcePackURL;
        if (url != null) {
            RPRenames.LOGGER.info("Starting creating config for Server's Resource Pack");
            String serverResourcePack = Hashing.sha1().hashString(url.toString(), StandardCharsets.UTF_8).toString();
            ConfigManager.configCreate("server-resource-packs/" + serverResourcePack);
        } else {
            RPRenames.LOGGER.info("Unknown error while creating config for Server's Resource Pack");
        }
    }

    public static void configCreate(String packName) {
        String filePath = packName.startsWith("server-resource-packs/") ? packName : "resourcepacks/" + packName;
        FileSystem zip = null;
        if (new File(filePath).isFile()) {
            try {
                zip = FileSystems.newFileSystem(Paths.get(filePath), (ClassLoader) null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Path currentPath;

        ArrayList<String> folders = new ArrayList<>();
        folders.add("/assets/minecraft/optifine/cit/");
        folders.add("/assets/minecraft/optifine/cem/");

        for (String currentFolder : folders) {
            if (new File(filePath).isFile()) {
                assert zip != null;
                currentPath = zip.getPath(currentFolder);
            } else {
                currentPath = Path.of(filePath + currentFolder);
            }

            String FT = null;
            if (currentFolder.endsWith("/cit/")) {
                FT = ".properties";
            } else if (currentFolder.endsWith("/cem/")) {
                FT = ".jem";
            }
            String fileType = FT;

            try {
                Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().endsWith(fileType)).forEach(propertiesFile -> {
                    try {
                        if (currentFolder.endsWith("/cit/")) {
                            InputStream inputStream = Files.newInputStream(propertiesFile);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                            Properties p = new Properties();
                            p.load(bufferedReader);
                            CITConfig.propertiesToRename(p, packName, propertiesFile.toString());
                        } else if (currentFolder.endsWith("/cem/")) {
                            String fileName = propertiesFile.getFileName().toString();
                            if (Arrays.stream(CEMList.models).toList().contains(fileName.substring(0, propertiesFile.getFileName().toString().length() - 4))) {
                                CEMConfig.startPropToRenameMob(packName, filePath);
                            }
                        }
                    } catch (IOException ignored) {
                    }
                });
            } catch (IOException ignored) {
            }
        }
        try {
            if (zip != null) {
                zip.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Rename> getFavorites(String item) {
        ArrayList<Rename> renames = new ArrayList<>();
        File favoritesFile = new File(RPRenames.configPathFavorite + "/" + item.replace(":", ".") + ".json");
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
                RPRenames.LOGGER.info("Created folder for favorites config: " + RPRenames.configPathFavorite);
            }
            RPRenames.LOGGER.info("Created new file for favorites config: " + RPRenames.configPathFavorite + "\\" + item.replaceAll(":", ".") + ".json");
            listNames.add(rename);
        }

        try {
            FileWriter fileWriter = new FileWriter(RPRenames.configPathFavorite + "\\" + item.replaceAll(":", ".") + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(listNames, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeFromFavorites(String favoriteName, String item) {
        ArrayList<Rename> renamesList = ConfigManager.getFavorites(item);
        int indexInRenamesList = new Rename(favoriteName).indexIn(renamesList, true);
        if (indexInRenamesList >= 0) {
            renamesList.remove(indexInRenamesList);
        }

        if (renamesList.size() > 0) {
            try {
                FileWriter fileWriter = new FileWriter(RPRenames.configPathFavorite + "\\" + item.replaceAll(":", ".") + ".json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(renamesList, fileWriter);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            deleteFavoriteConfigFile(item);
        }
    }

    private static void deleteFavoriteConfigFile(String item) {
        try {
            Files.deleteIfExists(Path.of(RPRenames.configPathFavorite + "\\" + item.replaceAll(":", ".") + ".json"));
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
        for (Map<String, ArrayList<Rename>> map : List.of(RPRenames.renames, RPRenames.renamesServer)) {
            for (Map.Entry<String, ArrayList<Rename>> entry : map.entrySet()) {
                for (Rename r : entry.getValue()) {
                    if (!names.contains(r)) names.add(r);
                }
            }
        }
        return names;
    }

    public static ArrayList<Rename> getAllRenames(String item) {
        ArrayList<Rename> names = new ArrayList<>(getRenames(RPRenames.renames, item));
        for (Rename r : getRenames(RPRenames.renamesServer, item)) {
            if (!names.contains(r)) {
                names.add(r);
            }
        }
        return names;
    }

    public static ArrayList<Rename> getRenames(Map<String, ArrayList<Rename>> map, String item) {
        if (map.containsKey(item)) {
            return map.get(item);
        } else {
            return new ArrayList<>();
        }
    }

    public static void addRenames(Map<String, ArrayList<Rename>> map, String item, Rename rename) {
        if (map.containsKey(item)) {
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
            if (!simplifiedRename.isContainedIn(map.get(item), true)) {
                map.get(item).add(rename);
            }
        } else {
            ArrayList<Rename> arrayList = new ArrayList<>();
            arrayList.add(rename);
            map.put(item, arrayList);
        }
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