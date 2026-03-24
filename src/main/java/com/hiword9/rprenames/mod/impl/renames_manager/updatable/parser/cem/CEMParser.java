package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.cem;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.util.PropertiesHelper;
import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.mod.impl.rename.CEMRename;
import com.hiword9.rprenames.api.core.renames_manager.RenamesManager;
import com.hiword9.rprenames.api.ext.renames_manager.parser.Parser;
import com.hiword9.rprenames.mod.util.ParserHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import java.io.*;
import java.util.*;

import static com.hiword9.rprenames.mod.util.Util.*;

public class CEMParser implements Parser {
    private static final String CEM_PATH = "optifine/cem";
    private static final String RANDOM_ENTITY_PATH = "optifine/random/entity/";
    private static final String MOB_PATH = "optifine/mob/";

    private static final String PROP_EXTENSION = ".properties";

    private final ArrayList<String> checked = new ArrayList<>();

    public RenamesManager<Rename> renamesManager;

    public CEMParser(RenamesManager<Rename> renamesManager) {
        this.renamesManager = renamesManager;
    }

    // if in any circumstances it is needed to always show cem renames this boolean can be set to true;
    public static boolean ignoreSkip = false;

    public void parse(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("rprenames:collecting_cem_renames");

        if (shouldSkipCemRenames()) {
            profiler.pop();
            return;
        }

        checked.clear();
        for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(CEM_PATH,
                s -> {
                    String path = s.getPath();
                    try {
                        String fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
                        if (!path.endsWith(".jem")) return false;
                        return (CEMModels.modelExists(fileName));
                    } catch (Exception e) {
                        return false;
                    }
                }
        ).entrySet()) {
            try {
                for (String jpmFileName : pullFieldListFromJsonInputStream(entry.getValue().open(), "model")) {
                    if (jpmFileName == null || !jpmFileName.endsWith(".jpm")) continue;
                    String path = entry.getKey().getPath();
                    parseTextureSourceFile(resourceManager, jpmFileName, path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(".")), RANDOM_ENTITY_PATH);
                }
            } catch (Exception e) {
                RPRenames.LOGGER.error("Something went wrong while parsing CEM Renames", e);
            }
        }

        for (Map.Entry<Identifier, Resource> entry : resourceManager.listResources(CEM_PATH,
                s -> {
                    String path = s.getPath();
                    try {
                        String fileName = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
                        if (!path.endsWith(PROP_EXTENSION)) return false;
                        return (CEMModels.modelExists(fileName));
                    } catch (Exception e) {
                        return false;
                    }
                }
        ).entrySet()) {
            try {
                Properties p = new Properties();
                p.load(entry.getValue().open());
                var numbers = getModelNumsFromProp(p);
                String path = entry.getKey().getPath();
                String pathInCem = path.substring(CEM_PATH.length() + 1, path.lastIndexOf("."));
                for (String n : numbers) {
                    if (n.equals("1")) n = "";
                    String jem = pathInCem + n + ".jem";
                    parseTextureSourceFile(resourceManager, jem, pathInCem.substring(pathInCem.lastIndexOf("/") + 1), MOB_PATH);
                }
            } catch (Exception e) {
                RPRenames.LOGGER.error("Something went wrong while parsing CEM Renames", e);
            }
        }

        for (CEMModels.ModelData modelData : CEMModels.data) {
            for (String texture : modelData.textures()) {
                try {
                    parseRawPropertyFile(resourceManager, RANDOM_ENTITY_PATH, texture, modelData.mob());
                    parseRawPropertyFile(resourceManager, MOB_PATH, texture, modelData.mob());
                } catch (Exception e) {
                    RPRenames.LOGGER.error("Something went wrong while parsing CEM Renames", e);
                }
            }
        }
        checked.clear();

        profiler.pop();
    }

    private void parseRawPropertyFile(ResourceManager resourceManager, String texturePath, String texture, EntityType<?> entityType) throws IOException {
        Identifier identifier = Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, texturePath + texture + PROP_EXTENSION);
        Optional<Resource> optionalResource = resourceManager.getResource(identifier);
        if (optionalResource.isEmpty()) {
            identifier = Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, texturePath + getLastPathPart(texture) + PROP_EXTENSION);
            optionalResource = resourceManager.getResource(identifier);
            if (optionalResource.isEmpty()) return;
        }
        Resource resource = optionalResource.get();

        String packName = ParserHelper.validatePackName(resource.source().packId());
        String path = ParserHelper.getFullPathFromIdentifier(packName, identifier);
        if (checked.contains(path)) return;

        propertiesToRenameMob(
                ParserHelper.getPropFromResource(resource),
                packName,
                path,
                entityType
        );
    }

    private void parseTextureSourceFile(ResourceManager resourceManager, String fileWithTextureName, String fileName, String texturePath) throws IOException {
        Optional<Resource> optionalResourceJpm = resourceManager.getResource(Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, CEM_PATH + "/" + fileWithTextureName));
        if (optionalResourceJpm.isEmpty()) return;

        Resource resourceJpm = optionalResourceJpm.get();
        var textures = pullFieldListFromJsonInputStream(resourceJpm.open(), "texture");
        if (textures.isEmpty()) return;

        String textureName = prepareTexturePath(textures.getFirst());

        Identifier propId = Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, texturePath + textureName + PROP_EXTENSION);
        Optional<Resource> optionalResourceProp = resourceManager.getResource(propId);
        if (optionalResourceProp.isEmpty()) return;

        Resource resourceProp = optionalResourceProp.get();

        String packName = ParserHelper.validatePackName(resourceProp.source().packId());
        String path = ParserHelper.getFullPathFromIdentifier(packName, propId);
        checked.add(path);

        CEMModels.ModelData modelData = CEMModels.find(fileName);
        if (modelData == null) return;

        propertiesToRenameMob(
                ParserHelper.getPropFromResource(resourceProp),
                packName,
                path,
                modelData.mob()
        );
    }

    private void propertiesToRenameMob(Properties p, String packName, String path, EntityType<?> entityType) {
        ArrayList<String> skins = new ArrayList<>();
        for (String s : p.stringPropertyNames()) {
            if (!s.startsWith("name.")) continue;

            String nameIndex = s.substring(5);

            String skin = p.getProperty("skins." + nameIndex);
            if (skins.contains(skin)) continue;
            skins.add(skin);

            String name = PropertiesHelper.getFirstName(p.getProperty(s), path);
            if (name == null) continue;

            String namePattern = findPropName(p, nameIndex);
            path = path.replaceAll("\\\\", "/");

            Rename itemRename = null;
            var alreadyExist = renamesManager.getRenames(CEMRename.DEFAULT_MOB_ITEM);

            var renameNameOnly = new Rename(Component.nullToEmpty(name), CEMRename.DEFAULT_MOB_ITEM);
            for (var r : alreadyExist) {
                if (r.baseEquals(renameNameOnly)) {
                    itemRename = r;
                    break;
                }
            }

            var rename = new CEMRename(
                    name,
                    packName,
                    path,
                    namePattern,
                    entityType,
                    p,
                    itemRename
            );

            boolean contained = false;
            for (Rename r : alreadyExist) {
                if (r instanceof CEMRename cemRename
                        && Objects.equals(name, cemRename.getName().getString())
                        && Objects.equals(entityType, cemRename.getEntity())
                ) contained = true;
            }
            if (!contained) {
                if (itemRename != null) {
                    renamesManager.removeRename(CEMRename.DEFAULT_MOB_ITEM, itemRename);
                }
                renamesManager.addRename(rename);
            }
        }
    }

    private static String findPropName(Properties properties, String nameIndex) {
        if (properties == null || nameIndex == null) return null;
        Set<String> propertyNames = properties.stringPropertyNames();
        for (String s : propertyNames) {
            if (s.startsWith("name." + nameIndex)) {
                return properties.getProperty(s);
            }
        }
        return null;
    }


    private static List<String> getModelNumsFromProp(Properties models) {
        ArrayList<String> numbers = new ArrayList<>();
        try {
            for (String p : models.stringPropertyNames()) {
                if (!p.startsWith("models.")) continue;
                String num = models.getProperty(p);
                if (!numbers.contains(num)) numbers.addAll(List.of(num.split(" ")));
            }
        } catch (Exception e) {
            RPRenames.LOGGER.error("Something went wrong while parsing CEM Renames", e);
        }
        return numbers;
    }

    private static List<String> pullFieldListFromJsonInputStream(InputStream inputStream, String field) {
        var obj = objFromInputStream(inputStream);
        if (obj == null) return List.of();

        var resultList = new ArrayList<String>();
        pullFieldListFromObj(obj, field, resultList);
        return resultList;
    }

    private static void pullFieldListFromObj(Object node, String field, List<String> resultList) {
        if (node instanceof Map<?,?> map) {
            var v = map.get(field);
            if (v != null) {
                resultList.add(v.toString());
                return;
            }

            for (Object value : map.values()) {
                pullFieldListFromObj(value, field, resultList);
            }
        } else if (node instanceof List<?> list) {
            for (Object value : list) {
                pullFieldListFromObj(value, field, resultList);
            }
        }
    }

    private static Object objFromInputStream(InputStream inputStream) {
        try (var reader = new InputStreamReader(inputStream)) {
            return GSON.fromJson(reader, Object.class);
        } catch (Exception e) {
            RPRenames.LOGGER.error("Something went wrong while parsing CEM Renames", e);
        }
        return null;
    }

    private static String getLastPathPart(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private static String prepareTexturePath(String texturePath) {
        if (texturePath.endsWith(".png")) {
            texturePath = texturePath.substring(0, texturePath.length() - 4);
        }
        if (texturePath.startsWith("textures/entity/")) {
            texturePath = texturePath.substring(16);
        }
        return texturePath;
    }

    private static boolean shouldSkipCemRenames() {
        if (ignoreSkip) return false;
        return config().ignoreCEM;
    }
}
