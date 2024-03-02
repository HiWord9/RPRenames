package com.HiWord9.RPRenames.util.config.generation;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import com.google.gson.Gson;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

import static com.HiWord9.RPRenames.util.config.ConfigManager.*;

public class CEMConfig {

    public static final String DEFAULT_MOB_ITEM = "name_tag";

    private static final String CEM_PATH = "optifine/cem";
    private static final String RANDOM_ENTITY_PATH = "optifine/random/entity/";

    private static final String PROP_EXTENSION = ".properties";

    public static void parseCEMs(ResourceManager resourceManager, Profiler profiler) {
//        for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources("optifine/cem",
//                s -> {
////                    s.getPath().endsWith(".properties");
//                    String path = s.getPath();
//                    if (!path.endsWith(".jem")) return false;
//                    String fileName = path.substring(path.lastIndexOf("/") + 1);
//                    return (Arrays.stream(CEMList.models).toList().contains(fileName.substring(fileName.length() - 4)));
//                }
//        ).entrySet()) {
//            try {
//                ArrayList<String> models = objToParamList(objFromInputStream(entry.getValue().getInputStream()), "model");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }


//        ArrayList<String> checked = new ArrayList<>();
//        for (int c = 0; c < CEMList.models.length; c++) {
//            int finalC = c;
//            for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources("optifine/cem",
//                    s -> {
//                        String path = s.getPath();
//                        if (!path.endsWith(".jem")) return false;
//                        String fileName = path.substring(path.lastIndexOf("/") + 1);
//                        return (fileName.substring(fileName.length() - 4)).equals(CEMList.models[finalC]);
//                    }
//            ).entrySet()) {
//                try {
//                    ArrayList<String> models = objToParamList(objFromInputStream(entry.getValue().getInputStream()), "model");
//                    models.stream()
//                            .filter(jpmFileName -> jpmFileName != null && jpmFileName.endsWith(".jpm"))
//                            .forEach(jpmFileName -> {
//                                for (Map.Entry<Identifier, Resource> entry2 : resourceManager.findResources("optifine/cem",
//                                        s -> s.getPath().endsWith("/" + jpmFileName)
//                                ).entrySet()) {
//                                    try {
//                                        String textureName = getPropPathInRandom(
//                                                objToParamList(objFromInputStream(entry2.getValue().getInputStream()), "texture").get(0)
//                                        );
//
//                                        for (Map.Entry<Identifier, Resource> entry3 : resourceManager.findResources("optifine/random/entity",
//                                                s -> s.getPath().endsWith("/" + textureName + PROP_EXTENSION)
//                                        ).entrySet()) {
//                                            Properties prop = new Properties();
//                                            prop.load(entry3.getValue().getInputStream());
//
//                                            String packName = entry.getValue().getResourcePackName();
//                                            if (packName.startsWith("/file")) {
//                                                packName = packName.substring(5);
//                                            }
//                                            String path = packName + "/" + entry.getKey().getNamespace() + "/" + entry.getKey().getPath();
//
//                                            checked.add(path);
//                                            propertiesToRenameMob(
//                                                    prop,
//                                                    packName,
//                                                    path,
//                                                    CEMList.mobs[finalC].getUntranslatedName()
//                                            );
//                                        }
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        //todo use profiler
        ArrayList<String> checked = new ArrayList<>();
        for (Map.Entry<Identifier, Resource> entry : resourceManager.findResources(CEM_PATH,
                s -> {
                    String path = s.getPath();
                    if (!path.endsWith(".jem")) return false;
                    String fileName = path.substring(path.lastIndexOf("/") + 1);
                    return (Arrays.stream(CEMList.models).toList().contains(fileName.substring(0, fileName.length() - 4)));
                }
        ).entrySet()) {
            try {
                for (String jpmFileName : objToParamList(objFromInputStream(entry.getValue().getInputStream()), "model")) {
                    if (jpmFileName == null || !jpmFileName.endsWith(".jpm")) continue;

                    Optional<Resource> optionalResourceJpm = resourceManager.getResource(new Identifier(Identifier.DEFAULT_NAMESPACE, CEM_PATH + jpmFileName));
                    if (optionalResourceJpm.isEmpty()) continue;

                    Resource resourceJpm = optionalResourceJpm.get();
                    String textureName = getPropPathInRandom(objToParamList(objFromInputStream(resourceJpm.getInputStream()), "texture").get(0));

                    Identifier propId = new Identifier(Identifier.DEFAULT_NAMESPACE, RANDOM_ENTITY_PATH + textureName + PROP_EXTENSION);
                    Optional<Resource> optionalResourceProp = resourceManager.getResource(propId);
                    if (optionalResourceProp.isEmpty()) continue;

                    Resource resourceProp = optionalResourceProp.get();

                    String packName = validatePackName(resourceProp.getResourcePackName());
                    String path = getFullPathFromIdentifier(packName, propId);
                    checked.add(path);

                    String keyPath = entry.getKey().getPath();
                    String fileName = keyPath.substring(keyPath.lastIndexOf("/") + 1, keyPath.length() - 4);
                    int i = Arrays.stream(CEMList.models).toList().indexOf(fileName);
                    if (i < 0) continue;

                    propertiesToRenameMob(
                            getPropFromResource(resourceProp),
                            packName,
                            path,
                            CEMList.mobs[i].getUntranslatedName()
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (int c = 0; c < CEMList.textures.length; c++) {
            try {
                Identifier identifier = new Identifier(Identifier.DEFAULT_NAMESPACE, RANDOM_ENTITY_PATH + CEMList.textures[c] + PROP_EXTENSION);
                Optional<Resource> optionalResource = resourceManager.getResource(identifier);
                if (optionalResource.isEmpty()) {
                    identifier = new Identifier(Identifier.DEFAULT_NAMESPACE, RANDOM_ENTITY_PATH + getLastPathPart(CEMList.textures[c]) + PROP_EXTENSION);
                    optionalResource = resourceManager.getResource(identifier);
                    if (optionalResource.isEmpty()) continue;
                }
                Resource resource = optionalResource.get();

                String packName = validatePackName(resource.getResourcePackName());
                String path = getFullPathFromIdentifier(packName, identifier);
                if (checked.contains(path)) continue;

                propertiesToRenameMob(
                        getPropFromResource(resource),
                        packName,
                        path,
                        CEMList.mobs[c].getUntranslatedName()
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Object objFromInputStream(InputStream inputStream) {
        Object obj = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            Type type = new com.google.gson.reflect.TypeToken<>() {
            }.getType();
            Gson gson = new Gson();
            obj = gson.fromJson(bufferedReader, type);
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private static ArrayList<String> objToParamList(Object obj, String param) {
        String string = obj.toString();
//        param = param + "=";
        ArrayList<String> list = new ArrayList<>();
        int j = string.length() - param.length();
        for (int i = 0; i < j; i++) {
            if (!string.startsWith(param, i) || string.charAt(i - 1) == 'b') continue;
            int start = i + param.length() + 1;
            list.add(string.substring(start, string.indexOf(',', start)));
        }
        return list;
    }

    private static void propertiesToRenameMob(Properties p, String packName, String path, String fileName) {
        ArrayList<String> skins = new ArrayList<>();
        for (String s : p.stringPropertyNames()) {
            if (!s.startsWith("name.")) continue;

            String skin = p.getProperty("skins." + s.substring(5));
            if (skins.contains(skin)) continue;
            skins.add(skin);

            String name = ConfigManager.getFirstName(p.getProperty(s));
            if (name == null) continue;

            ArrayList<Rename> alreadyExist = ConfigManager.getRenames(DEFAULT_MOB_ITEM);

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
                RPRenames.renames.put(DEFAULT_MOB_ITEM, newConfig);
            }
        }
    }

//    public static void startPropToRenameMob(String packName, String rpPath) {
//        if (new File(rpPath).isFile()) {
//            propToRenameMobZIP(packName, rpPath);
//        } else {
//            propToRenameMobDir(packName, rpPath);
//        }
//    }
//
//    private static void propToRenameMobZIP(String packName, String rpPath) {
//        ArrayList<String> checked = new ArrayList<>();
//        try {
//            FileSystem zip = FileSystems.newFileSystem(Paths.get(rpPath), (ClassLoader) null);
//            Path currentPath = zip.getPath("/assets/minecraft/optifine/");
//
//            for (int c = 0; c < CEMList.models.length; c++) {
//                int finalC = c;
//                Files.walk(currentPath, new FileVisitOption[0])
//                        .filter(path -> path.toString().equals(CEM_PATH + CEMList.models[finalC] + ".jem"))
//                        .map(jemFile -> getObjFromBF(jemFile).toString())
//                        .map(obj -> getParamListFromObj(obj, "model="))
//                        .flatMap(Collection::stream)
//                        .filter(jpmFileName -> jpmFileName != null && jpmFileName.endsWith(".jpm"))
//                        .forEach(jpmFileName -> {
//                            try {
//                                Files.walk(currentPath, new FileVisitOption[0])
//                                        .filter(path -> path.toString().equals(CEM_PATH + jpmFileName))
//                                        .map(jpmFile -> getObjFromBF(jpmFile).toString())
//                                        .map(jpmObj -> getPropPathInRandom(Objects.requireNonNull(getParamListFromObj(jpmObj, "texture=").get(0))))
//                                        .forEach(textureName -> {
//                                            try {
//                                                Files.walk(currentPath, new FileVisitOption[0])
//                                                        .filter(path -> path.toString().equals(RANDOM_ENTITY_PATH + textureName + PROP_EXTENSION))
//                                                        .forEach(propFile -> {
//                                                            checked.add(propFile.toString());
//                                                            propertiesToRenameMob(
//                                                                    getPropFromPath(propFile),
//                                                                    packName,
//                                                                    propFile.toString(),
//                                                                    CEMList.mobs[finalC].getUntranslatedName()
//                                                            );
//                                                        });
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                        });
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        });
//            }
//            for (int c = 0; c < CEMList.textures.length; c++) {
//                int finalC = c;
//                Files.walk(currentPath, new FileVisitOption[0])
//                        .filter(path -> (
//                                path.toString().equals(RANDOM_ENTITY_PATH + CEMList.textures[finalC] + PROP_EXTENSION)
//                                        || path.toString().equals(RANDOM_ENTITY_PATH + getLastPathPart(CEMList.textures[finalC]) + PROP_EXTENSION)
//                        ))
//                        .filter(propFile -> !checked.contains(propFile.toString()))
//                        .forEach(propFile -> propertiesToRenameMob(
//                                getPropFromPath(propFile),
//                                packName,
//                                propFile.toString(),
//                                CEMList.mobs[finalC].getUntranslatedName()
//                        ));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static void propToRenameMobDir(String packName, String rpPath) {
//        ArrayList<String> checked = new ArrayList<>();
//
//        for (int c = 0; c < CEMList.models.length; c++) {
//            File currentJem = new File(rpPath + CEM_PATH + CEMList.models[c] + ".jem");
//            if (!currentJem.exists()) continue;
//
//            String obj = getObjFromBF(currentJem.toPath()).toString();
//            ArrayList<String> jpmList = getParamListFromObj(obj, "model=");
//
//            for (String jpmFileName : jpmList) {
//                if (jpmFileName == null || !jpmFileName.endsWith(".jpm")) continue;
//
//                String jpmObj = getObjFromBF(new File(rpPath + CEM_PATH + jpmFileName).toPath()).toString();
//                String textureName = getPropPathInRandom(Objects.requireNonNull(getParamListFromObj(jpmObj, "texture=").get(0)));
//                File propertiesFile = new File(rpPath + RANDOM_ENTITY_PATH + textureName + PROP_EXTENSION);
//                if (!propertiesFile.exists()) continue;
//
//                checked.add(propertiesFile.getPath());
//                propertiesToRenameMob(
//                        getPropFromPath(propertiesFile.toPath()),
//                        packName,
//                        propertiesFile.toString(),
//                        CEMList.mobs[c].getUntranslatedName()
//                );
//            }
//        }
//
//        for (int c = 0; c < CEMList.textures.length; c++) {
//            File propertiesFile = new File(rpPath + RANDOM_ENTITY_PATH + CEMList.textures[c] + PROP_EXTENSION);
//            if (!propertiesFile.exists()) {
//                propertiesFile = new File(rpPath + RANDOM_ENTITY_PATH + getLastPathPart(CEMList.textures[c]) + PROP_EXTENSION);
//                if (!propertiesFile.exists()) continue;
//            }
//            if (checked.contains(propertiesFile.getPath())) continue;
//
//            propertiesToRenameMob(
//                    getPropFromPath(propertiesFile.toPath()),
//                    packName,
//                    propertiesFile.toString(),
//                    CEMList.mobs[c].getUntranslatedName()
//            );
//        }
//    }

//    private static ArrayList<String> getParamListFromObj(String obj, String parName) {
//        ArrayList<String> list = new ArrayList<>();
//        for (int i = 0; i < obj.length() - parName.length(); i++) {
//            if (obj.startsWith(parName, i) && !String.valueOf(obj.charAt(i - 1)).equals("b")) {
//                int o = i + parName.length();
//                while (!String.valueOf(obj.charAt(o)).equals(",")) {
//                    o++;
//                }
//                list.add(obj.substring(i + parName.length(), o));
//            }
//        }
//        return list;
//    }

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
//    private static Object getObjFromBF(Path pathToFile) {
//        Object obj = null;
//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Files.newInputStream(pathToFile)));
//            try {
//                Type type = new com.google.gson.reflect.TypeToken<>() {
//                }.getType();
//                Gson gson = new Gson();
//                obj = gson.fromJson(bufferedReader, type);
//                bufferedReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return obj;
//    }
}
