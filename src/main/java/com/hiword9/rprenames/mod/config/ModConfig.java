package com.hiword9.rprenames.mod.config;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.item_group.RPRenamesItemGroup;
import com.hiword9.rprenames.mod.gui.widget.external.FavoriteButton;
import com.hiword9.rprenames.api.ext.rename.renderer.PreviewTooltipPositioner;

import java.io.FileReader;
import java.io.FileWriter;

import static com.hiword9.rprenames.mod.util.Util.*;

public class ModConfig {
    public boolean ignoreCEM = false;
    public void setIgnoreCEM(boolean ignoreCEM) {
        if (this.shouldUpdateRenamesList == null || !shouldUpdateRenamesList) {
            this.shouldUpdateRenamesList = ignoreCEM != this.ignoreCEM;
        }
        this.ignoreCEM = ignoreCEM;
    }

    public boolean ignoreCIT = true;

    public boolean enableAnvilModification = true;

    public FavoriteButton.Position favoriteButtonPosition = FavoriteButton.Position.TOP_RIGHT;

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

    public boolean offsetMenu = true;

    public boolean showPackName = true;
    public boolean showExtraProperties = true;

    public boolean highlightSelected = true;
    public boolean recolorFavoriteTooltip = true;
    public boolean renderStarInFavoriteTooltip = false;

    public boolean highlightSlot = true;

    public int slotHighlightColorALPHA = 50;
    public int slotHighlightColorRGB = 8454143;

    public boolean highlightTooltipSlotWrong = true;
    public boolean highlightTooltipSlotSelected = false;

    public boolean enablePreview = true;
    public PreviewTooltipPositioner.PreviewPos previewPos = PreviewTooltipPositioner.PreviewPos.LEFT;

    public boolean playerPreviewByDefault = false;

    public boolean spinMobPreview = true;
    public boolean spinPlayerPreview = false;

    public boolean disableSnowGolemPumpkin = false;

    public double scaleFactorItem = 2.0;
    public double scaleFactorEntity = 1.0;

    public boolean alwaysAllowPlayerPreviewHead = false;

    public boolean disableTooltipHints = false;
    public boolean disablePageArrowsHints = false;

    public boolean updateConfig = true;
    public boolean showNamePattern = false;
    public boolean showOriginalProperties = false;
    public boolean fixDelayedPacketsChangingTab = false;

    public boolean showDescription = true; // todo ? add to config screen

    public int getSlotHighlightRGBA() {
        int a = (int) ((float) slotHighlightColorALPHA / 100 * 255);
        return (a << 24) | slotHighlightColorRGB;
    }

    public static final ModConfig INSTANCE = ModConfig.read();

    public static ModConfig read() {
        if (!RPRenames.MOD_CONFIG_FILE.exists())
            return new ModConfig().write();

        try (var reader = new FileReader(RPRenames.MOD_CONFIG_FILE)) {
            return GSON.fromJson(reader, ModConfig.class);
        } catch (Exception e) {
            RPRenames.LOGGER.error("Could not read Config file", e);
            throw new RuntimeException(e);
        }
    }

    protected Boolean shouldUpdateItemGroup = null;
    protected Boolean shouldUpdateRenamesList = null;

    public ModConfig write() {
        if (shouldUpdateRenamesList != null && shouldUpdateRenamesList) {
            RPRenames.updatableRenamesManager.updateRenames(); // todo custom instance ?
            shouldUpdateRenamesList = null;
        } else if (shouldUpdateItemGroup != null && shouldUpdateItemGroup) {
            RPRenamesItemGroup.update();
        }
        shouldUpdateItemGroup = null;

        try (var writer = GSON.newJsonWriter(new FileWriter(RPRenames.MOD_CONFIG_FILE))) {
            writer.setIndent("    ");
            GSON.toJson(GSON.toJsonTree(this, ModConfig.class), writer);
        } catch (Exception e) {
            RPRenames.LOGGER.error("Could not write Config file", e);
            throw new RuntimeException(e);
        }
        return this;
    }
}
