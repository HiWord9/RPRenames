package com.hiword9.rprenames.mod.util;

import com.google.gson.JsonElement;
import com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.item_model.properties.RenameProperties;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.PackLocationInfo;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RenameInfoHelper {
    public static List<Component> getProperties(Properties properties) {
        var lines = new ArrayList<Component>();
        for (String s : properties.stringPropertyNames()) {
            lines.add(getProperty(s, properties.getProperty(s)));
        }
        return lines;
    }

    public static List<Component> getProperties(RenameProperties properties) {
        var lines = new ArrayList<Component>();
        RenameProperties.CODEC.encodeStart(JsonOps.INSTANCE, properties).resultOrPartial()
                .filter(JsonElement::isJsonObject)
                .ifPresent(json -> json.getAsJsonObject().entrySet().forEach(entry ->
                        lines.add(getProperty(entry.getKey(), asString(entry.getValue())))
                ));
        return lines;
    }

    protected static String asString(JsonElement json) {
        return json.isJsonPrimitive() ? json.getAsString() : json.toString();
    }

    public static Component getProperty(String key, String value) {
        return Component.nullToEmpty(key).copy().withStyle(ChatFormatting.GOLD)
                .append(Component.nullToEmpty("=").copy().withStyle(ChatFormatting.GRAY))
                .append(Component.nullToEmpty(value).copy().withStyle(ChatFormatting.GREEN));
    }

    public static List<Component> getRPPath(@Nullable PackLocationInfo pack, String path) {
        var packName = ParserHelper.getPackName(pack);
        var lines = new ArrayList<Component>();
        if (packName != null && path != null) {
            lines.add(getPath(path, packName));
        }

        if (path == null) {
            lines.add(
                    Component.translatable("rprenames.command.info.noPath")
                            .withStyle(ChatFormatting.RED)
            );
        }

        if (packName == null) {
            lines.add(
                    Component.translatable("rprenames.command.info.noRpName")
                            .withStyle(ChatFormatting.RED)
            );
        } else {
            lines.add(
                    Component.translatable("rprenames.command.info.rpName")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.nullToEmpty(" = ").copy().withStyle(ChatFormatting.GRAY))
                            .append(Component.nullToEmpty(packName).copy().withStyle(ChatFormatting.BLUE))
            );
        }
        return lines;
    }

    public static Component getPath(String path, String packName) {
        String dirPath = path.substring(0, path.lastIndexOf("/"));
        return Component.translatable(
                "rprenames.command.info.located",
                Component.nullToEmpty(path).copy()
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.YELLOW)
                                .withUnderlined(true)
                                .withClickEvent(new ClickEvent.OpenFile(
                                        packName.equals("server") ? "server-resource-packs/" : "resourcepacks/"
                                                + (packName.endsWith(".zip") ? packName : dirPath)
                                ))
                        )
        );
    }
}
