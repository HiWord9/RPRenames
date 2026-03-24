package com.hiword9.rprenames.mod.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RenameInfoHelper {
    public static List<Component> getProperties(Properties properties) {
        var lines = new ArrayList<Component>();
        for (String s : properties.stringPropertyNames()) {
            lines.add(
                    Component.nullToEmpty(s).copy().withStyle(ChatFormatting.GOLD)
                    .append(Component.nullToEmpty("=").copy().withStyle(ChatFormatting.GRAY))
                    .append(Component.nullToEmpty(properties.getProperty(s)).copy().withStyle(ChatFormatting.GREEN))
            );
        }
        return lines;
    }

    public static List<Component> getRPPath(String packName, String path) {
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
