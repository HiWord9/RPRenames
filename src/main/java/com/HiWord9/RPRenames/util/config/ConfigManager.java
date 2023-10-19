package com.HiWord9.RPRenames.util.config;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.generation.CEMConfig;
import com.HiWord9.RPRenames.util.config.generation.CEMList;
import com.HiWord9.RPRenames.util.config.generation.CITConfig;
import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.regex.PatternSyntaxException;

public class ConfigManager {
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
        RPRenames.LOGGER.info("Starting creating config server");
        startConfigCreateServer();
        RPRenames.LOGGER.info("Finished creating config server");
    }

    public static void configUpdate(ArrayList<String> enabledPacks) {
        configClear();
        RPRenames.LOGGER.info("Starting creating config");
        startConfigCreate(enabledPacks);
        RPRenames.LOGGER.info("Finished creating config");
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
        File favoritesFile = new File(RPRenames.configPathFavorite + "/" + item.replaceAll(":", ".") + ".json");
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
        return renames;
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
            try {
                Files.deleteIfExists(Path.of(RPRenames.configPathFavorite + "\\" + item.replaceAll(":", ".") + ".json"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFirstName(String nbtDisplayName) {
        return getFirstName(nbtDisplayName, null);
    }

    public static String getFirstName(String nbtDisplayName, @Nullable String item) {
        nbtDisplayName = parseEscapes(nbtDisplayName);
        if (nbtDisplayName.startsWith("pattern:") || nbtDisplayName.startsWith("ipattern:")) {
            if (nbtDisplayName.startsWith("i")) {
                nbtDisplayName = nbtDisplayName.substring(1);
            }
            nbtDisplayName = nbtDisplayName.replaceFirst("pattern:", "");
            nbtDisplayName = nbtDisplayName.replace("*", "");
            nbtDisplayName = nbtDisplayName.replace("?", "_");
        } else if (nbtDisplayName.startsWith("regex:") || nbtDisplayName.startsWith("iregex")) {
            if (nbtDisplayName.startsWith("i")) {
                nbtDisplayName = nbtDisplayName.substring(1);
            }
            nbtDisplayName = nbtDisplayName.replaceFirst("regex:", "");
            nbtDisplayName = nbtDisplayName.replace(".*", "");
            nbtDisplayName = nbtDisplayName.replace(".+", "_");

            String originalRegex = nbtDisplayName;
            nbtDisplayName = solveRegex(nbtDisplayName);
            try {
                if (!nbtDisplayName.matches(originalRegex)) {
                    RPRenames.LOGGER.error("Couldn't get valid string from regex" + (item != null ? " for " + item : ""));
                    RPRenames.LOGGER.error("regex:" + originalRegex);
                    RPRenames.LOGGER.error("received string:" + nbtDisplayName);
                }
            } catch (PatternSyntaxException e) {
                RPRenames.LOGGER.error("INVALID REGEX");
            }
        }
        return nbtDisplayName;
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
                        System.out.println("Invalid unicode \"" + unicodeNumbers + "\" for String: " + string);
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
            names.addAll(entry.getValue());
        }
        for (Map.Entry<String, ArrayList<Rename>> entry : RPRenames.renamesServer.entrySet()) {
            names.addAll(entry.getValue());
        }
        return names;
    }

    public static ArrayList<Rename> getAllRenames(String item) {
        ArrayList<Rename> names = new ArrayList<>(renamesGet(RPRenames.renames, item));
        for (Rename r : renamesGet(RPRenames.renamesServer, item)) {
            if (!names.contains(r)) {
                names.add(r);
            }
        }
        return names;
    }

    public static ArrayList<Rename> renamesGet(Map<String, ArrayList<Rename>> map, String item) {
        if (map.containsKey(item)) {
            return map.get(item);
        } else {
            return new ArrayList<>();
        }
    }

    public static void renamesAdd(Map<String, ArrayList<Rename>> map, String item, Rename rename) {
        if (map.containsKey(item)) {
            Rename simplifiedRename = new Rename(rename.getName(), item, null, null, rename.getStackSize(), rename.getDamage(), rename.getEnchantment(), rename.getEnchantmentLevel(), null, null);
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

    public static void configClear() {
        RPRenames.renames.clear();
    }
}