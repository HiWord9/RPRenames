package com.HiWord9.RPRenames.config;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.Rename;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public class ConfigManager {

    private static ArrayList<Rename> theList;

    public static String configPath = RPRenames.configPath;
    public static File configFolder = RPRenames.configFolder;
    public static String configPathModels = RPRenames.configPathModels;
    public static File configFolderModels = RPRenames.configFolderModels;

    public static void configUpdate() {
        if (configFolder.exists()) {
            System.out.println("[RPR] Config's folder is already exist. Starting recreate");
            configDeleter(configPath);
            configDeleter(configPathModels);
            startConfigCreate();
            if (Objects.requireNonNull(configFolder.listFiles()).length == 0) {
                configFolder.delete();
            }
            if (Objects.requireNonNull(configFolderModels.listFiles()).length == 0) {
                configFolderModels.delete();
            }
        } else {
            startConfigCreate();
        }
    }

    private static void startConfigCreate() {
        configFolder.mkdirs();
        configFolderModels.mkdirs();

        String resourcePacks = null;
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
        String currentRP = "";
        while (h < Objects.requireNonNull(resourcePacks).length()) {
            if (String.valueOf(resourcePacks.charAt(h)).equals("[") || String.valueOf(resourcePacks.charAt(h)).equals("\"")) {
                h++;
            } else {
                if (String.valueOf(resourcePacks.charAt(h - 1)).equals("\"")) {
                    while (h < resourcePacks.length()) {
                        currentRP = currentRP + resourcePacks.charAt(h);
                        h++;
                        if (String.valueOf(resourcePacks.charAt(h)).equals("\"")) {
                            if (currentRP.startsWith("file/")) {
                                currentRP = currentRP.substring(5);
                                configCreate("resourcepacks/" + currentRP);
                            }
                            h = h + 3;
                            currentRP = "";
                        }
                    }
                }
            }
        }
    }

    public static void configCreate(String filePath) {
        FileSystem zip = null;
        if (filePath.endsWith(".zip")) {
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

            if (filePath.endsWith(".zip")) {
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
                            CITConfig.propertiesToJson(p);
                        } else if (currentFolder.endsWith("/cem/")) {
                            String fileName = propertiesFile.getFileName().toString();
                            if (Arrays.stream(CEMList.models).toList().contains(fileName.substring(0, propertiesFile.getFileName().toString().length() - 4))) {
                                CEMConfig.startPropToJsonModels(filePath);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public static void configDeleter(String directoryName) {
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
