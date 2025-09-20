package com.HiWord9.RPRenames.mod.util;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Properties;

public class ParserHelper {
    private static final String MINECRAFT_COLON = Identifier.DEFAULT_NAMESPACE + Identifier.NAMESPACE_SEPARATOR;

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

    public static Item itemFromId(String id) {
        return Registries.ITEM.get(Identifier.of(id));
    }

    public static String idFromItem(Item item) {
        String id = Registries.ITEM.getId(item).toString();
        if (id.startsWith(MINECRAFT_COLON))
            return id.substring(MINECRAFT_COLON.length());
        return id;
    }
}
