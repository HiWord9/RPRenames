package com.HiWord9.RPRenames.mod.util;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.mod.impl.rename.HasProperties;
import com.HiWord9.RPRenames.mod.impl.rename.ResourcePackRename;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RenameInfoHelper {
    public static List<Text> getRenameInfo(Rename rename) {
        var lines = new ArrayList<Text>();
        Properties props = null;
        if (rename instanceof HasProperties hasProperties) props = hasProperties.getProperties();

        if (props != null) lines.addAll(getProperties(props));

        if (rename instanceof ResourcePackRename rpRename) {
            lines.addAll(getRPPath(rpRename.getPackName(), rpRename.getPath()));
        }
        return lines;
    }

    public static List<Text> getProperties(Properties properties) {
        var lines = new ArrayList<Text>();
        for (String s : properties.stringPropertyNames()) {
            lines.add(
                    Text.of(s).copy().formatted(Formatting.GOLD)
                    .append(Text.of("=").copy().formatted(Formatting.GRAY))
                    .append(Text.of(properties.getProperty(s)).copy().formatted(Formatting.GREEN))
            );
        }
        return lines;
    }

    public static List<Text> getRPPath(String packName, String path) {
        var lines = new ArrayList<Text>();
        if (packName != null && path != null) {
            lines.add(getPath(path, packName));
        }

        if (path == null) {
            lines.add(
                    Text.translatable("rprenames.command.info.noPath")
                            .formatted(Formatting.RED)
            );
        }

        if (packName == null) {
            lines.add(
                    Text.translatable("rprenames.command.info.noRpName")
                            .formatted(Formatting.RED)
            );
        } else {
            lines.add(
                    Text.translatable("rprenames.command.info.rpName")
                            .formatted(Formatting.GOLD)
                            .append(Text.of(" = ").copy().formatted(Formatting.GRAY))
                            .append(Text.of(packName).copy().formatted(Formatting.BLUE))
            );
        }
        return lines;
    }

    public static Text getPath(String path, String packName) {
        String dirPath = path.substring(0, path.lastIndexOf("/"));
        return Text.translatable(
                "rprenames.command.info.located",
                Text.of(path).copy()
                        .fillStyle(Style.EMPTY
                                .withColor(Formatting.YELLOW)
                                .withUnderline(true)
                                .withClickEvent(new ClickEvent.OpenFile(
                                        packName.equals("server") ? "server-resource-packs/" : "resourcepacks/"
                                                + (packName.endsWith(".zip") ? packName : dirPath)
                                ))
                        )
        );
    }
}
