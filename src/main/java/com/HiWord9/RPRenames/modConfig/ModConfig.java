package com.HiWord9.RPRenames.modConfig;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.configGeneration.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;

@Config(name = "rprenames")
public class ModConfig implements ConfigData {
    public boolean createConfig = true;
    public boolean createConfigServer = true;

    public boolean enableAnvilModification = true;
    public boolean showCreateConfigCheckbox = true;

    public int createConfigCheckboxPosX = 158;
    public int createConfigCheckboxPosY = 48;

    public int favoritePosX = 71;
    public int favoritePosY = -75;

    public boolean recreateConfig = false;

    public boolean loadModBuiltinResources = true;

    public static final ModConfig INSTANCE = ModConfig.read();

    public static ModConfig read() {
        if (!RPRenames.MOD_CONFIG_FILE.exists())
            return new ModConfig().write();

        Reader reader = null;
        try {
            return new Gson().fromJson(reader = new FileReader(RPRenames.MOD_CONFIG_FILE), ModConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public ModConfig write() {
        Gson gson = new Gson();
        JsonWriter writer = null;
        try {
            writer = gson.newJsonWriter(new FileWriter(RPRenames.MOD_CONFIG_FILE));
            writer.setIndent("    ");

            gson.toJson(gson.toJsonTree(this, ModConfig.class), writer);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return this;
    }

    public void executesOnExit() {
        if (this.recreateConfig) {
            this.recreateConfig = false;
            System.out.println("[RPR] Executing Recreation of config for renames after closing the ModMenu settings menu.");
            ConfigManager.configUpdate();
        }
        this.write();
    }
}
