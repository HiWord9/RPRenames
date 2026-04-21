package com.hiword9.rprenames.mod;

import java.nio.file.Path;

public class Settings {
    static Path configDir;

    public static Path getConfigDir() {
        if (configDir == null) throw new IllegalStateException();
        return configDir;
    }

    public static void setConfigDir(Path path) {
        if (configDir != null) throw new IllegalStateException();
        configDir = path;
    }
}
