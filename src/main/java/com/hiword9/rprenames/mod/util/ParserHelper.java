package com.hiword9.rprenames.mod.util;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import java.io.IOException;
import java.util.Properties;

public class ParserHelper {

    public static Properties getPropFromResource(Resource resource) throws IOException {
        Properties prop = new Properties();
        prop.load(resource.open());
        return prop;
    }

    public static String getFullPathFromIdentifier(String packName, Identifier identifier) {
        return validatePackName(packName) + "/assets/" + identifier.getNamespace() + "/" + identifier.getPath();
    }

    public static String validatePackName(String packName) {
        return packName.startsWith("file/") ? packName.substring(5) : packName;
    }
}
