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
        profiler.push("rprenames:collecting_cem_renames");

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

                    Optional<Resource> optionalResourceJpm = resourceManager.getResource(new Identifier(Identifier.DEFAULT_NAMESPACE, CEM_PATH + "/" + jpmFileName));
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

        profiler.pop();
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
        ArrayList<String> list = new ArrayList<>();
        int j = string.length() - param.length();
        for (int i = 0; i < j; i++) {
            if (!string.startsWith(param + "=", i) || (i != 0 && String.valueOf(string.charAt(i - 1)).matches("[a-zA-Z]"))) continue;
            int start = i + param.length() + 1;
            if (!string.contains(",")) continue;
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
}
