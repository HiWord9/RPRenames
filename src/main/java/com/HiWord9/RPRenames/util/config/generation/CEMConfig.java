package com.HiWord9.RPRenames.util.config.generation;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class CEMConfig {
    public static void propertiesToRenameMob(Properties p, String packName, String path, String fileName) {
        List<String> namesValues = p.stringPropertyNames().stream().toList();
        ArrayList<String> skins = new ArrayList<>();
        for (String s : namesValues) {
            
            if (!s.startsWith("name.")) return;
            if (!skins.contains(p.getProperty("skins." + s.substring(5)))) return;

            skins.add(p.getProperty("skins." + s.substring(5)));
            String name = ConfigManager.getFirstName(p.getProperty(s));
            
            if (name == null) return;
    
            ArrayList<Rename> alreadyExist = ConfigManager.renamesGet(packName.equals("server") ? RPRenames.renamesServer : RPRenames.renames, "name_tag");
            Rename rename;
            Rename renameNameOnly = new Rename(name, "name_tag");
            Rename.Mob mob = new Rename.Mob(fileName, ConfigManager.getIdAndPath(CEMList.iconFromName(fileName)), p, path.replaceAll("\\\\", "/"));
            
            if (renameNameOnly.isContainedIn(alreadyExist, true)) {
                Rename renameForItem = alreadyExist.get(renameNameOnly.indexIn(alreadyExist, true));
                alreadyExist.remove(renameNameOnly.indexIn(alreadyExist, true));
                rename = new Rename(renameForItem.getName(), 
                                    renameForItem.getItem(), 
                                    renameForItem.getPackName(), 
                                    renameForItem.getPath(), 
                                    renameForItem.getStackSize(), 
                                    renameForItem.getDamage(), 
                                    renameForItem.getEnchantment(), 
                                    renameForItem.getEnchantmentLevel(), 
                                    renameForItem.getProperties(), mob);
            } else {
                rename = new Rename(name, "name_tag", packName,
                        null, null, null, null, null, null, mob);
            }
            
            if (rename.isContainedIn(alreadyExist)) return;

            ArrayList<Rename> newConfig = new ArrayList<>(alreadyExist);
            newConfig.add(rename);

            if (packName.equals("server")) {
                RPRenames.renamesServer.put("name_tag", newConfig);
            } else {
                RPRenames.renames.put("name_tag", newConfig);
            }
        }
    }

    public static void startPropToRenameMob(String packName, String rpPath) {
        ArrayList<String> checked = new ArrayList<>();
        String cemPath = "/assets/minecraft/optifine/cem/";
        String randomEntityPath = "/assets/minecraft/optifine/random/entity/";
        
        if (new File(rpPath).isFile()) {
            try {
                FileSystem zip = FileSystems.newFileSystem(Paths.get(rpPath), (ClassLoader) null);
                Path currentPath = zip.getPath("/assets/minecraft/optifine/");
                for (int c = 0; c < CEMList.models.length; c++) {
                    int finalC = c;
                    Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().equals(cemPath + CEMList.models[finalC] + ".jem")).forEach(jemFile -> {
                        String obj = getObjFromBF(jemFile).toString();
                        ArrayList<String> jpmList = getParamListFromObj(obj, "model=");
                        for (String jpmFileName : jpmList) {
                            if (!(jpmFileName != null && jpmFileName.endsWith(".jpm"))) return; // По нормальному тут надо еще скобки развернуть но мне лень
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
                                                propertiesToRenameMob(p, packName, propFile.toString(), CEMList.mobs[finalC].getUntranslatedName());
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
                    });
                }
                for (int c = 0; c < CEMList.textures.length; c++) {
                    int finalC = c;
                    Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().equals(randomEntityPath + CEMList.textures[finalC] + ".properties")).forEach(propFile -> {
                        if (!checked.contains(String.valueOf(propFile))) {
                            try {
                                InputStream inputStream = Files.newInputStream(propFile);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                Properties p = new Properties();
                                p.load(reader);
                                propertiesToRenameMob(p, packName, propFile.toString(), CEMList.mobs[finalC].getUntranslatedName());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Files.walk(currentPath, new FileVisitOption[0]).filter(path -> path.toString().equals(randomEntityPath + getLastPathPart(CEMList.textures[finalC]) + ".properties")).forEach(propFile -> {
                        if (!checked.contains(String.valueOf(propFile))) {
                            try {
                                InputStream inputStream = Files.newInputStream(propFile);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                Properties p = new Properties();
                                p.load(reader);
                                propertiesToRenameMob(p, packName, propFile.toString(), CEMList.mobs[finalC].getUntranslatedName());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            for (int c = 0; c < CEMList.models.length; c++) {
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
                                    propertiesToRenameMob(p, packName, propertiesFile.toString(), CEMList.mobs[c].getUntranslatedName());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            for (int c = 0; c < CEMList.textures.length; c++) {
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
                        propertiesToRenameMob(p, packName, propertiesFile.toString(), CEMList.mobs[c].getUntranslatedName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static ArrayList<String> getParamListFromObj(String obj, String parName) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < obj.length() - parName.length(); i++) {
            if (obj.startsWith(parName, i) && !String.valueOf(obj.charAt(i - 1)).equals("b")) {
                int o = i + parName.length();
                while (!String.valueOf(obj.charAt(o)).equals(",")) {
                    o++;
                }
                list.add(obj.substring(i + parName.length(), o));
            }
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
