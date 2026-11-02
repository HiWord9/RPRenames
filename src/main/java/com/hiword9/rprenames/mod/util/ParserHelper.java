package com.hiword9.rprenames.mod.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.Properties;

public class ParserHelper {
    private static final FileToIdConverter ITEM_ASSETS = FileToIdConverter.json("items");

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

    public static @Nullable String getPackName(@Nullable PackLocationInfo pack) {
        return pack == null ? null : validatePackName(pack.id());
    }

    public static Identifier getItemAssetFile(Item item) {
        return ITEM_ASSETS.idToFile(BuiltInRegistries.ITEM.getKey(item));
    }
}
