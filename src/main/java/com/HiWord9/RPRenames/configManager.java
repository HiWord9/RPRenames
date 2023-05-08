package com.HiWord9.RPRenames;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public class configManager {

    private static ArrayList<Rename> theList;

    public static String configPath = RPRenames.configPath;
    public static File configFolder = RPRenames.configFolder;
    public static String configPathModels = RPRenames.configPathModels;
    public static File configFolderModels = RPRenames.configFolderModels;

    public static void jsonManager() {
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

    public static void configDeleter(String directoryName) {
        File directory = new File(directoryName);
        try {
            FileUtils.deleteDirectory(directory);
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
                Files.walk(currentPath, new java.nio.file.FileVisitOption[0]).filter(path -> path.toString().endsWith(fileType)).forEach(propertiesFile -> {
                    try {
                        if (currentFolder.endsWith("/cit/")) {
                            InputStream inputStream = Files.newInputStream(propertiesFile);
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                            Properties p = new Properties();
                            p.load(bufferedReader);
                            propertiesToJson(p);
                        } else if (currentFolder.endsWith("/cem/")) {
                            String fileName = propertiesFile.getFileName().toString();
                            if (Arrays.stream(CEM.models).toList().contains(fileName.substring(0, propertiesFile.getFileName().toString().length() - 4))) {
                                startPropToJsonModels(filePath);
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

    public static void propertiesToJson(Properties p) {
        String items = p.getProperty("matchItems");
        if (items == null) {
            items = p.getProperty("items");
        }
        if (items != null) {
            while (items.endsWith(" ")) {
                items = items.substring(0, items.length() - 1);
            }
            String item = null;
            boolean finish = false;
            while (!finish) {
                int i = 0;
                while (i < items.length()) {
                    if (String.valueOf(items.charAt(i)).equals(" ")) {
                        item = items.substring(0, i);
                        items = items.substring(i + 1);
                        finish = false;
                        break;
                    }
                    i++;
                    finish = true;
                }
                if (finish) {
                    item = items;
                }

                item = Objects.requireNonNull(item).replace("minecraft:", "");

                File currentFile = new File(configPath + item + ".json");
                if (currentFile.exists() && p.getProperty("nbt.display.Name") != null) {
                    Rename alreadyExist = configRead(currentFile);
                    String[] ae = alreadyExist.getName();
                    if (!Arrays.stream(ae).toList().contains(getFirstName(p.getProperty("nbt.display.Name")))) {
                        int AEsize = ae.length;
                        String[] newConfig = new String[AEsize + 1];
                        int h = 0;
                        while (h < AEsize) {
                            newConfig[h] = ae[h];
                            h++;
                        }
                        newConfig[h] = getFirstName(p.getProperty("nbt.display.Name"));

                        Rename newRename = new Rename(newConfig);
                        ArrayList<Rename> listFiles = new ArrayList<>();
                        listFiles.add(newRename);

                        try {
                            FileWriter fileWriter = new FileWriter(currentFile);
                            Gson gson = new Gson();
                            gson.toJson(listFiles, fileWriter);
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (p.getProperty("nbt.display.Name") != null) {
                        try {
                            System.out.println("[RPR] Created new file for config: " + configPath + item + ".json");
                            ArrayList<Rename> listNames = new ArrayList<>();
                            Rename name1 = new Rename(new String[]{getFirstName(p.getProperty("nbt.display.Name"))});
                            listNames.add(name1);
                            FileWriter fileWriter = new FileWriter(currentFile);
                            Gson gson = new Gson();
                            gson.toJson(listNames, fileWriter);
                            fileWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void propertiesToJsonModels(Properties p, String fileName) {
        File currentFile = new File(configPathModels + fileName + ".json");
        if (currentFile.exists()) {
            List<String> namesValues = p.stringPropertyNames().stream().toList();
            ArrayList<String> skins = new ArrayList<>();
            for (String s : namesValues) {
                if (s.startsWith("name.")) {
                    if (!skins.contains(p.getProperty("skins." + s.substring(5)))) {
                        skins.add(p.getProperty("skins." + s.substring(5)));
                        String name = getFirstName(p.getProperty(s));
                        if (currentFile.exists() && name != null) {
                            Rename alreadyExist = configRead(currentFile);
                            String[] ae = alreadyExist.getName();
                            if (!Arrays.stream(ae).toList().contains(name)) {
                                int AEsize = ae.length;
                                String[] newConfig = new String[AEsize + 1];
                                int h = 0;
                                while (h < AEsize) {
                                    newConfig[h] = ae[h];
                                    h++;
                                }
                                newConfig[h] = name;

                                Rename newRename = new Rename(newConfig);
                                ArrayList<Rename> listFiles = new ArrayList<>();
                                listFiles.add(newRename);

                                try {
                                    FileWriter fileWriter = new FileWriter(currentFile);
                                    Gson gson = new Gson();
                                    gson.toJson(listFiles, fileWriter);
                                    fileWriter.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        } else {
            ArrayList<String> namesArray = new ArrayList<>();
            List<String> namesValues = p.stringPropertyNames().stream().toList();
            ArrayList<String> skins = new ArrayList<>();
            for (String s : namesValues) {
                if (s.startsWith("name.")) {
                    if (!skins.contains(p.getProperty("skins." + s.substring(5)))) {
                        skins.add(p.getProperty("skins." + s.substring(5)));
                        String name = getFirstName(p.getProperty(s));
                        if (!namesArray.contains(name)) {
                            namesArray.add(name);
                        }
                    }
                }
            }
            String[] names = new String[namesArray.size()];
            int i = 0;

            for (String s : namesArray) {
                names[i] = s;
                i++;
            }

            Rename rename = new Rename(names);
            ArrayList<Rename> renameArray = new ArrayList<>();
            renameArray.add(rename);

            if (names.length != 0) {
                try {
                    System.out.println("[RPR] Created new file for config: " + configPathModels + fileName + ".json");
                    FileWriter fileWriter = new FileWriter(currentFile);
                    Gson gson = new Gson();
                    gson.toJson(renameArray, fileWriter);
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void startPropToJsonModels(String rpPath) {
        ArrayList<String> checked = new ArrayList<>();
        String cemPath = "/assets/minecraft/optifine/cem/";
        String randomEntityPath = "/assets/minecraft/optifine/random/entity/";
        if (rpPath.endsWith(".zip")) {
            try {
                FileSystem zip = FileSystems.newFileSystem(Paths.get(rpPath), (ClassLoader) null);
                Path currentPath = zip.getPath("/assets/minecraft/optifine/");
                int c = 0;
                while (c < CEM.models.length) {
                    int fc = c;
                    Files.walk(currentPath, new java.nio.file.FileVisitOption[0]).filter(path -> path.toString().equals(cemPath + CEM.models[fc] + ".jem")).forEach(jemFile -> {
                        String obj = getObjFromBF(jemFile).toString();
                        ArrayList<String> jpmList = getParamListFromObj(obj, "model=");
                        for (String jpmFileName : jpmList) {
                            if (jpmFileName != null && jpmFileName.endsWith(".jpm")) {
                                try {
                                    Files.walk(currentPath, new java.nio.file.FileVisitOption[0]).filter(path -> path.toString().equals(cemPath + jpmFileName)).forEach(jpmFile -> {
                                        String jpmObj = getObjFromBF(jpmFile).toString();
                                        String textureName = getPropPathInRandom(Objects.requireNonNull(getParamListFromObj(jpmObj, "texture=").get(0)));
                                        try {
                                            Files.walk(currentPath, new java.nio.file.FileVisitOption[0]).filter(path -> path.toString().equals(randomEntityPath + textureName + ".properties")).forEach(propFile -> {
                                                try {
                                                    checked.add(String.valueOf(propFile));
                                                    InputStream inputStream = Files.newInputStream(propFile);
                                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                                    Properties p = new Properties();
                                                    p.load(reader);
                                                    propertiesToJsonModels(p, CEM.mobsNames[fc]);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    c++;
                }
                c = 0;
                while (c < CEM.textures.length) {
                    int fc = c;
                    Files.walk(currentPath, new java.nio.file.FileVisitOption[0]).filter(path -> path.toString().equals(randomEntityPath + CEM.textures[fc] + ".properties")).forEach(propFile -> {
                        if (!checked.contains(String.valueOf(propFile))) {
                            try {
                                InputStream inputStream = Files.newInputStream(propFile);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                Properties p = new Properties();
                                p.load(reader);
                                propertiesToJsonModels(p, CEM.mobsNames[fc]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Files.walk(currentPath, new java.nio.file.FileVisitOption[0]).filter(path -> path.toString().equals(randomEntityPath + getLastPathPart(CEM.textures[fc]) + ".properties")).forEach(propFile -> {
                        if (!checked.contains(String.valueOf(propFile))) {
                            try {
                                InputStream inputStream = Files.newInputStream(propFile);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                Properties p = new Properties();
                                p.load(reader);
                                propertiesToJsonModels(p, CEM.mobsNames[fc]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    c++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            int c = 0;
            while (c < CEM.models.length) {
                File currentJem = new File(rpPath + cemPath + CEM.models[c] + ".jem");
                if (currentJem.exists()) {
                    String obj = getObjFromBF(currentJem.toPath()).toString();
                    ArrayList<String> jpmList = getParamListFromObj(obj, "model=");
                    for (String jpmFileName : jpmList) {
                        if (jpmFileName != null && jpmFileName.endsWith(".jpm")) {
                            String jpmObj = getObjFromBF(new File(rpPath + cemPath + jpmFileName).toPath()).toString();
                            String textureName = getPropPathInRandom(Objects.requireNonNull(getParamListFromObj(jpmObj, "texture=").get(0)));
                            File propertiesFile = new File(rpPath + randomEntityPath + textureName + ".properties");
                            if (propertiesFile.exists()) {
                                try {
                                    checked.add(propertiesFile.getPath());
                                    InputStream inputStream = Files.newInputStream(propertiesFile.toPath());
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                    Properties p = new Properties();
                                    p.load(reader);
                                    propertiesToJsonModels(p, CEM.mobsNames[c]);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                c++;
            }
            c = 0;
            while (c < CEM.textures.length) {
                File propertiesFile = null;
                if (new File(rpPath + randomEntityPath + CEM.textures[c] + ".properties").exists()) {
                    propertiesFile = new File(rpPath + randomEntityPath + CEM.textures[c] + ".properties");
                } else if (new File(rpPath + randomEntityPath + getLastPathPart(CEM.textures[c]) + ".properties").exists()) {
                    propertiesFile = new File(rpPath + randomEntityPath + getLastPathPart(CEM.textures[c]) + ".properties");
                }
                if (propertiesFile != null && !checked.contains(propertiesFile.getPath())) {
                    try {
                        InputStream inputStream = Files.newInputStream(propertiesFile.toPath());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        Properties p = new Properties();
                        p.load(reader);
                        propertiesToJsonModels(p, CEM.mobsNames[c]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                c++;
            }
        }
    }

    public static ArrayList<String> getParamListFromObj(String obj, String parName) {
        ArrayList<String> list = new ArrayList<>();
        int i = 0;
        while (i < obj.length() - parName.length()) {
            if (obj.startsWith(parName, i) && !String.valueOf(obj.charAt(i - 1)).equals("b")){
                int o = i + parName.length();
                while (!String.valueOf(obj.charAt(o)).equals(",")) {
                    o++;
                }
                list.add(obj.substring(i + parName.length(), o));
            }
            i++;
        }
        return list;
    }

    public static String getLastPathPart(String path) {
        int i = path.length() - 1;
        while (i >= 0) {
            if (!String.valueOf(path.charAt(i)).equals("/")) {
                i--;
            } else {
                break;
            }
        }
        if (i >= 0) {
            return path.substring(i + 1);
        }
        return path;
    }

    public static String getPropPathInRandom(String texturePath) {
        if (texturePath.endsWith(".png")) {
            texturePath = texturePath.substring(0, texturePath.length() - 4);
        }
        if (texturePath.startsWith("textures/entity/")) {
            texturePath = texturePath.substring(16);
        }
        return texturePath;
    }

    public static Object getObjFromBF(Path pathToFile) {
        Object obj = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Files.newInputStream(pathToFile)));
            try {
                Type type = new com.google.gson.reflect.TypeToken<>() {
                }.getType();
                Gson gson = new Gson();
                obj = gson.fromJson(bufferedReader, type);
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
