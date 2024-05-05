package com.HiWord9.RPRenames.util.config.generation;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Properties;

public class ParserHelper {

    public static Properties getPropFromResource(Resource resource) throws IOException {
        Properties prop = new Properties();
        prop.load(resource.getInputStream());
        return prop;
    }

    public static String getFullPathFromIdentifier(String packName, Identifier identifier) {
        return validatePackName(packName) + "/assets/" + identifier.getNamespace() + "/" + identifier.getPath();
    }

    public static String validatePackName(String packName) {
        return packName.startsWith("file/") ? packName.substring(5) : packName;
    }

    public static String getIdAndPath(Item item) {
        String idAndPath = Registries.ITEM.getId(item).toString();
        if (idAndPath.startsWith("minecraft:")) {
            return idAndPath.substring(10);
        }
        return idAndPath;
    }

    public static Item itemFromName(String id) {
        return Registries.ITEM.get(new Identifier(id));
    }
}
