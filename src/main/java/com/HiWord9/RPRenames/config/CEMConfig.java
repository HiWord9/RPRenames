package com.HiWord9.RPRenames.config;

import com.HiWord9.RPRenames.Rename;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;

public class CEMConfig {

    public static void propertiesToJsonModels(Properties p, String fileName) {
        File currentFile = new File(ConfigManager.configPathModels + fileName + ".json");
        if (currentFile.exists()) {
            List<String> namesValues = p.stringPropertyNames().stream().toList();
            ArrayList<String> skins = new ArrayList<>();
            for (String s : namesValues) {
                if (s.startsWith("name.")) {
                    if (!skins.contains(p.getProperty("skins." + s.substring(5)))) {
                        skins.add(p.getProperty("skins." + s.substring(5)));
                        String name = ConfigManager.getFirstName(p.getProperty(s));
                        if (currentFile.exists() && name != null) {
                            Rename alreadyExist = ConfigManager.configRead(currentFile);
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
                        String name = ConfigManager.getFirstName(p.getProperty(s));
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
                    System.out.println("[RPR] Created new file for config: " + ConfigManager.configPathModels + fileName + ".json");
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
                while (c < CEMList.models.length) {
                    int fc = c;
                    Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().equals(cemPath + CEMList.models[fc] + ".jem")).forEach(jemFile -> {
                        String obj = getObjFromBF(jemFile).toString();
                        ArrayList<String> jpmList = getParamListFromObj(obj, "model=");
                        for (String jpmFileName : jpmList) {
                            if (jpmFileName != null && jpmFileName.endsWith(".jpm")) {
                                try {
                                    Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().equals(cemPath + jpmFileName)).forEach(jpmFile -> {
                                        String jpmObj = getObjFromBF(jpmFile).toString();
                                        String textureName = getPropPathInRandom(Objects.requireNonNull(getParamListFromObj(jpmObj, "texture=").get(0)));
                                        try {
                                            Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().equals(randomEntityPath + textureName + ".properties")).forEach(propFile -> {
                                                try {
                                                    checked.add(String.valueOf(propFile));
                                                    InputStream inputStream = Files.newInputStream(propFile);
                                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                                    Properties p = new Properties();
                                                    p.load(reader);
                                                    propertiesToJsonModels(p, CEMList.mobsNames[fc]);
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
                while (c < CEMList.textures.length) {
                    int fc = c;
                    Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().equals(randomEntityPath + CEMList.textures[fc] + ".properties")).forEach(propFile -> {
                        if (!checked.contains(String.valueOf(propFile))) {
                            try {
                                InputStream inputStream = Files.newInputStream(propFile);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                Properties p = new Properties();
                                p.load(reader);
                                propertiesToJsonModels(p, CEMList.mobsNames[fc]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().equals(randomEntityPath + getLastPathPart(CEMList.textures[fc]) + ".properties")).forEach(propFile -> {
                        if (!checked.contains(String.valueOf(propFile))) {
                            try {
                                InputStream inputStream = Files.newInputStream(propFile);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                Properties p = new Properties();
                                p.load(reader);
                                propertiesToJsonModels(p, CEMList.mobsNames[fc]);
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
            while (c < CEMList.models.length) {
                File currentJem = new File(rpPath + cemPath + CEMList.models[c] + ".jem");
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
                                    propertiesToJsonModels(p, CEMList.mobsNames[c]);
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
            while (c < CEMList.textures.length) {
                File propertiesFile = null;
                if (new File(rpPath + randomEntityPath + CEMList.textures[c] + ".properties").exists()) {
                    propertiesFile = new File(rpPath + randomEntityPath + CEMList.textures[c] + ".properties");
                } else if (new File(rpPath + randomEntityPath + getLastPathPart(CEMList.textures[c]) + ".properties").exists()) {
                    propertiesFile = new File(rpPath + randomEntityPath + getLastPathPart(CEMList.textures[c]) + ".properties");
                }
                if (propertiesFile != null && !checked.contains(propertiesFile.getPath())) {
                    try {
                        InputStream inputStream = Files.newInputStream(propertiesFile.toPath());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        Properties p = new Properties();
                        p.load(reader);
                        propertiesToJsonModels(p, CEMList.mobsNames[c]);
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
