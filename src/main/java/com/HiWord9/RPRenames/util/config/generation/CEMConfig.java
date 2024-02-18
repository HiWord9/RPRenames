package com.HiWord9.RPRenames.util.config.generation;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

import static com.HiWord9.RPRenames.util.config.ConfigManager.getPropFromPath;

public class CEMConfig {

    public static final String DEFAULT_MOB_ITEM = "name_tag";

    private static final String CEM_PATH = "/assets/minecraft/optifine/cem/";
    private static final String RANDOM_ENTITY_PATH = "/assets/minecraft/optifine/random/entity/";

    private static final String PROP_EXTENSION = ".properties";

    public static void propertiesToRenameMob(Properties p, String packName, String path, String fileName) {
        ArrayList<String> skins = new ArrayList<>();
        for (String s : p.stringPropertyNames()) {
            if (!s.startsWith("name.")) continue;

            String skin = p.getProperty("skins." + s.substring(5));
            if (skins.contains(skin)) continue;
            skins.add(skin);

            String name = ConfigManager.getFirstName(p.getProperty(s));
            if (name == null) continue;

            ArrayList<Rename> alreadyExist = ConfigManager.getRenames(packName.equals("server") ? RPRenames.renamesServer : RPRenames.renames, DEFAULT_MOB_ITEM);

            Rename rename;
            Rename renameNameOnly = new Rename(name, DEFAULT_MOB_ITEM);
            Rename.Mob mob = new Rename.Mob(
                    fileName,
                    ConfigManager.getIdAndPath(CEMList.iconFromName(fileName)),
                    p,
                    path.replaceAll("\\\\", "/")
            );

            if (renameNameOnly.isContainedIn(alreadyExist, true)) {
                Rename renameForItem = alreadyExist.get(renameNameOnly.indexIn(alreadyExist, true));
                alreadyExist.remove(renameNameOnly.indexIn(alreadyExist, true));
                rename = new Rename(
                        renameForItem.getName(),
                        renameForItem.getItems(),
                        renameForItem.getPackName(),
                        renameForItem.getPath(),
                        renameForItem.getStackSize(),
                        renameForItem.getDamage(),
                        renameForItem.getEnchantment(),
                        renameForItem.getEnchantmentLevel(),
                        renameForItem.getProperties(),
                        renameForItem.getDescription(),
                        mob
                );
            } else {
                rename = new Rename(name, packName, mob);
            }

            if (!rename.isContainedIn(alreadyExist)) {
                ArrayList<Rename> newConfig = new ArrayList<>(alreadyExist);
                newConfig.add(rename);

                if (packName.equals("server")) {
                    RPRenames.renamesServer.put(DEFAULT_MOB_ITEM, newConfig);
                } else {
                    RPRenames.renames.put(DEFAULT_MOB_ITEM, newConfig);
                }
            }
        }
    }

    public static void startPropToRenameMob(String packName, String rpPath) {
        if (new File(rpPath).isFile()) {
            propToRenameMobZIP(packName, rpPath);
        } else {
            propToRenameMobDir(packName, rpPath);
        }
    }

    private static void propToRenameMobZIP(String packName, String rpPath) {
        ArrayList<String> checked = new ArrayList<>();
        try {
            FileSystem zip = FileSystems.newFileSystem(Paths.get(rpPath), (ClassLoader) null);
            Path currentPath = zip.getPath("/assets/minecraft/optifine/");

            for (int c = 0; c < CEMList.models.length; c++) {
                int finalC = c;
                Files.walk(currentPath, new FileVisitOption[0])
                        .filter(path -> path.toString().equals(CEM_PATH + CEMList.models[finalC] + ".jem"))
                        .map(jemFile -> getObjFromBF(jemFile).toString())
                        .map(obj -> getParamListFromObj(obj, "model="))
                        .flatMap(Collection::stream)
                        .filter(jpmFileName -> jpmFileName != null && jpmFileName.endsWith(".jpm"))
                        .forEach(jpmFileName -> {
                            try {
                                Files.walk(currentPath, new FileVisitOption[0])
                                        .filter(path -> path.toString().equals(CEM_PATH + jpmFileName))
                                        .map(jpmFile -> getObjFromBF(jpmFile).toString())
                                        .map(jpmObj -> getPropPathInRandom(Objects.requireNonNull(getParamListFromObj(jpmObj, "texture=").get(0))))
                                        .forEach(textureName -> {
                                            try {
                                                Files.walk(currentPath, new FileVisitOption[0])
                                                        .filter(path -> path.toString().equals(RANDOM_ENTITY_PATH + textureName + PROP_EXTENSION))
                                                        .forEach(propFile -> {
                                                            checked.add(propFile.toString());
                                                            propertiesToRenameMob(
                                                                    getPropFromPath(propFile),
                                                                    packName,
                                                                    propFile.toString(),
                                                                    CEMList.mobs[finalC].getUntranslatedName()
                                                            );
                                                });
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
            for (int c = 0; c < CEMList.textures.length; c++) {
                int finalC = c;
                Files.walk(currentPath, new FileVisitOption[0])
                        .filter(path -> (
                                path.toString().equals(RANDOM_ENTITY_PATH + CEMList.textures[finalC] + PROP_EXTENSION)
                                || path.toString().equals(RANDOM_ENTITY_PATH + getLastPathPart(CEMList.textures[finalC]) + PROP_EXTENSION)
                        ))
                        .filter(propFile -> !checked.contains(propFile.toString()))
                        .forEach(propFile -> propertiesToRenameMob(
                                getPropFromPath(propFile),
                                packName,
                                propFile.toString(),
                                CEMList.mobs[finalC].getUntranslatedName()
                        ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void propToRenameMobDir(String packName, String rpPath) {
        ArrayList<String> checked = new ArrayList<>();

        for (int c = 0; c < CEMList.models.length; c++) {
            File currentJem = new File(rpPath + CEM_PATH + CEMList.models[c] + ".jem");
            if (!currentJem.exists()) continue;

            String obj = getObjFromBF(currentJem.toPath()).toString();
            ArrayList<String> jpmList = getParamListFromObj(obj, "model=");

            for (String jpmFileName : jpmList) {
                if (jpmFileName == null || !jpmFileName.endsWith(".jpm")) continue;

                String jpmObj = getObjFromBF(new File(rpPath + CEM_PATH + jpmFileName).toPath()).toString();
                String textureName = getPropPathInRandom(Objects.requireNonNull(getParamListFromObj(jpmObj, "texture=").get(0)));
                File propertiesFile = new File(rpPath + RANDOM_ENTITY_PATH + textureName + PROP_EXTENSION);
                if (!propertiesFile.exists()) continue;

                checked.add(propertiesFile.getPath());
                propertiesToRenameMob(
                        getPropFromPath(propertiesFile.toPath()),
                        packName,
                        propertiesFile.toString(),
                        CEMList.mobs[c].getUntranslatedName()
                );
            }
        }

        for (int c = 0; c < CEMList.textures.length; c++) {
            File propertiesFile = new File(rpPath + RANDOM_ENTITY_PATH + CEMList.textures[c] + PROP_EXTENSION);
            if (!propertiesFile.exists()) {
                propertiesFile = new File(rpPath + RANDOM_ENTITY_PATH + getLastPathPart(CEMList.textures[c]) + PROP_EXTENSION);
                if (!propertiesFile.exists()) continue;
            }
            if (checked.contains(propertiesFile.getPath())) continue;

            propertiesToRenameMob(
                    getPropFromPath(propertiesFile.toPath()),
                    packName,
                    propertiesFile.toString(),
                    CEMList.mobs[c].getUntranslatedName()
            );
        }
    }

    private static ArrayList<String> getParamListFromObj(String obj, String parName) {
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

    private static String getLastPathPart(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private static String getPropPathInRandom(String texturePath) {
        if (texturePath.endsWith(".png")) {
            texturePath = texturePath.substring(0, texturePath.length() - 4);
        }
        if (texturePath.startsWith("textures/entity/")) {
            texturePath = texturePath.substring(16);
        }
        return texturePath;
    }

    private static Object getObjFromBF(Path pathToFile) {
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
