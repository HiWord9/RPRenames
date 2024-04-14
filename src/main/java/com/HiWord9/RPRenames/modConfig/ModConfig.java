package com.HiWord9.RPRenames.modConfig;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.gui.RenameButtonHolder;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.math.Color;
import org.apache.commons.compress.utils.IOUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;

@Config(name = RPRenames.MOD_ID)
public class ModConfig implements ConfigData {
    public boolean ignoreCEM = false;

    public boolean enableAnvilModification = true;

    public int favoritePosX = 71;
    public int favoritePosY = -75;

    public boolean compareItemGroupRenames = true;
    public void setCompareItemGroupRenames(boolean compareItemGroupRenames) {
        if (shouldUpdateItemGroup == null || !shouldUpdateItemGroup) {
            this.shouldUpdateItemGroup = compareItemGroupRenames != this.compareItemGroupRenames;
        }
        this.compareItemGroupRenames = compareItemGroupRenames;
    }

    public boolean generateSpawnEggsInItemGroup = true;
    public void setGenerateSpawnEggsInItemGroup(boolean generateSpawnEggsInItemGroup) {
        if (shouldUpdateItemGroup == null || !shouldUpdateItemGroup) {
            this.shouldUpdateItemGroup = generateSpawnEggsInItemGroup != this.generateSpawnEggsInItemGroup;
        }
        this.generateSpawnEggsInItemGroup = generateSpawnEggsInItemGroup;
    }

    public boolean loadModBuiltinResources = true;

    public boolean openByDefault = false;
    public RenameButtonHolder.ViewMode viewMode = RenameButtonHolder.ViewMode.GRID;

    public boolean offsetMenu = true;

    public boolean showPackName = true;
    public boolean showExtraProperties = true;

    public boolean renderMobRenamesAsEntities = true;
    public boolean highlightSelected = true;

    public boolean highlightSlot = true;

    public int slotHighlightColorALPHA = 50;
    public int slotHighlightColorRGB = 8454143;

    public boolean highlightTooltipSlotWrong = true;
    public boolean highlightTooltipSlotSelected = false;

    public boolean enablePreview = true;
    public RenameButtonHolder.PreviewPos previewPos = RenameButtonHolder.PreviewPos.LEFT;

    public boolean playerPreviewByDefault = false;

    public boolean spinMobPreview = true;
    public boolean spinPlayerPreview = false;

    public boolean disableSnowGolemPumpkin = false;

    public double scaleFactorItem = 2.0;
    public double scaleFactorEntity = 1.0;

    public boolean alwaysAllowPlayerPreviewHead = false;

    public boolean disablePageArrowsTips = false;
    public boolean disablePlayerPreviewTips = false;

    public boolean updateConfig = true;
    public boolean showNbtDisplayName = false;
    public boolean showOriginalProperties = false;

    public boolean showDescription = true;

    public int getSlotHighlightRGBA() {
        int r = Color.ofTransparent(slotHighlightColorRGB).getRed();
        int g = Color.ofTransparent(slotHighlightColorRGB).getGreen();
        int b = Color.ofTransparent(slotHighlightColorRGB).getBlue();
        return Color.ofRGBA(r, g, b, (int) ((float) slotHighlightColorALPHA / 100 * 255)).getColor();
    }

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

    private Boolean shouldUpdateItemGroup = null;

    public ModConfig write() {
        if (shouldUpdateItemGroup != null && shouldUpdateItemGroup) ConfigManager.updateItemGroup();
        shouldUpdateItemGroup = null;

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
}
