package com.HiWord9.RPRenames.configGeneration;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.Rename;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.resource.ResourcePackProfile;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigManager {

    public static ArrayList<Rename> theList;

    static ModConfig config = ModConfig.INSTANCE;

    public static void configUpdate() {
        configUpdate(getPacksFromOptions());
    }

    public static void configUpdate(List<ResourcePackProfile> enabledPacks) {
        ArrayList<String> packs = new ArrayList<>();
        for (ResourcePackProfile rpp : enabledPacks) {
            packs.add(rpp.getName());
        }
        configUpdate(packs);
    }

    public static void configUpdate(ArrayList<String> enabledPacks) {
        if (RPRenames.configClientFolder.exists() || RPRenames.configServerFolder.exists()) {
            System.out.println("[RPR] Config's folder is already exist. Starting deleting.");
            configDelete(RPRenames.configPathClient);
            configDelete(RPRenames.configPathServer);
        }
        System.out.println("[RPR] Starting creating config for renames.");
        startConfigCreate(enabledPacks);
        System.out.println("[RPR] Finished creating config for renames.");
    }

    public static ArrayList<String> getPacksFromOptions() {
        ArrayList<String> packs = new ArrayList<>();
        String resourcePacks = "";
        try {
            File file = new File("options.txt");
            FileReader options;
            options = new FileReader(file.getAbsolutePath());
            Properties p = new Properties();
            p.load(options);
            resourcePacks = p.getProperty("resourcePacks");
        } catch (IOException e) {
            e.printStackTrace();
        }

        int h = 0;
        while (h < resourcePacks.length()) {
            if (!String.valueOf(resourcePacks.charAt(h)).equals("[") && !String.valueOf(resourcePacks.charAt(h)).equals("]")) {
                if (String.valueOf(resourcePacks.charAt(h)).equals("\"")) {
                    int c = h;
                    h++;
                    while (!String.valueOf(resourcePacks.charAt(h)).equals("\"")) {
                        h++;
                    }
                    String packName = resourcePacks.substring(c + 1,h);
                    packs.add(packName);
                }
            }
            h++;
        }
        if (RPRenames.serverResourcePackURL != null) {
            packs.add("server");
        }
        return packs;
    }

    public static void startConfigCreate(ArrayList<String> enabledPacks) {
        for (String s : enabledPacks) {
            if (s.startsWith("file/")) {
                String packName = s.substring(5);
                System.out.println("[RPR] Starting creating config for \"" + packName + "\".");
                ConfigManager.configCreate("resourcepacks/" + packName, RPRenames.configPathClient);
            }
            if (s.equals("server") && config.createConfigServer) {
                URL url = RPRenames.serverResourcePackURL;
                if (url != null) {
                    System.out.println("[RPR] Starting creating config for Server's Resource Pack");
                    String serverResourcePack = Hashing.sha1().hashString(url.toString(), StandardCharsets.UTF_8).toString();
                    ConfigManager.configCreate("server-resource-packs/" + serverResourcePack, RPRenames.configPathServer);
                } else {
                    System.out.println("[RPR] Unknown error while creating config for Server's Resource Pack");
                }
            }
        }
    }

    public static void configCreate(String filePath, String outputPath) {
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
                            CITConfig.propertiesToJson(p, outputPath + RPRenames.configPathNameCIT + "/");
                        } else if (currentFolder.endsWith("/cem/")) {
                            String fileName = propertiesFile.getFileName().toString();
                            if (Arrays.stream(CEMList.models).toList().contains(fileName.substring(0, propertiesFile.getFileName().toString().length() - 4))) {
                                CEMConfig.startPropToJsonModels(filePath, outputPath + RPRenames.configPathNameCEM + "/");
                            }
                        }
                    } catch (IOException ignored) {}
                });
            } catch (IOException ignored) {}
        }
        try {
            if (zip != null) {
                zip.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Rename configRead(File theFile) {
        try {
            FileReader fileReader = new FileReader(theFile);
            Type type = new TypeToken<ArrayList<Rename>>() {
            }.getType();
            Gson gson = new Gson();
            theList = gson.fromJson(fileReader, type);
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return theList.get(0);
    }

    public static void configDelete(String directoryName) {
        File directory = new File(directoryName);
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFirstName(String nbtDisplayName) {
        if (nbtDisplayName.startsWith("pattern:") || nbtDisplayName.startsWith("ipattern:")) {
            if (nbtDisplayName.startsWith("pattern:")) {
                nbtDisplayName = nbtDisplayName.replace("pattern:", "");
            } else if (nbtDisplayName.startsWith("ipattern:")) {
                nbtDisplayName = nbtDisplayName.replace("ipattern:", "");
            }
            nbtDisplayName = nbtDisplayName.replace("*", "");
            nbtDisplayName = nbtDisplayName.replace("?", "");
        } else if (nbtDisplayName.startsWith("regex:") || nbtDisplayName.startsWith("iregex:")) {
            if (nbtDisplayName.startsWith("regex:")) {
                nbtDisplayName = nbtDisplayName.replace("regex:", "");
            } else if (nbtDisplayName.startsWith("iregex:")) {
                nbtDisplayName = nbtDisplayName.replace("iregex:", "");
            }
            while (nbtDisplayName.contains("(")) {
                int a = 0;
                while (!(String.valueOf(nbtDisplayName.charAt(a)).equals("("))) {
                    a++;
                }
                int b = 0;
                if (nbtDisplayName.contains("|")) {
                    while (!(String.valueOf(nbtDisplayName.charAt(b)).equals("|")) && b < nbtDisplayName.length()-1) {
                        b++;
                    }
                }
                int c = 0;
                while (!(String.valueOf(nbtDisplayName.charAt(c)).equals(")"))) {
                    c++;
                }
                if (b == nbtDisplayName.length() || b > c) {
                    b = 0;
                }
                if (b == 0) {
                    b = c;
                }
                nbtDisplayName = nbtDisplayName.substring(0, b).replace("(", "") + nbtDisplayName.substring(c + 1);
            }
            while (nbtDisplayName.contains("[")) {
                int a = 0;
                while (!(String.valueOf(nbtDisplayName.charAt(a)).equals("["))) {
                    a++;
                }
                int b = 0;
                while (!(String.valueOf(nbtDisplayName.charAt(b)).equals("]"))) {
                    b++;
                }
                nbtDisplayName = nbtDisplayName.substring(0, a + 2).replace("[", "") + nbtDisplayName.substring(b + 1);
            }
            while (nbtDisplayName.contains(".*")) {
                nbtDisplayName = nbtDisplayName.replace(".*", "");
            }
            if (String.valueOf(nbtDisplayName.charAt(0)).equals(" ")) {
                nbtDisplayName = nbtDisplayName.substring(1);
            }
            if (String.valueOf(nbtDisplayName.charAt(nbtDisplayName.length() - 1)).equals(" ")) {
                nbtDisplayName = nbtDisplayName.substring(0, nbtDisplayName.length() - 1);
            }
        }
        return nbtDisplayName;
    }
}
