package com.HiWord9.RPRenames.modConfig;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.gui.widget.button.external.FavoriteButton;
import com.HiWord9.RPRenames.util.rename.renderer.PreviewTooltipPositioner;
import com.HiWord9.RPRenames.util.rename.RenamesManager;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.shedaniel.math.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class ModConfigScreenFactory {
    public static Screen create(Screen parent) {
        ModConfig currentConfig = ModConfig.INSTANCE, defaultConfig = new ModConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setDefaultBackgroundTexture(Identifier.of("minecraft", "textures/block/bookshelf.png"))
                .setTitle(Text.translatable("rprenames.config.title"))
                .setSavingRunnable(currentConfig::write);

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("rprenames.config.general"));
        ConfigCategory gui = builder.getOrCreateCategory(Text.translatable("rprenames.config.gui"));
        ConfigCategory debug = builder.getOrCreateCategory(Text.translatable("rprenames.config.debug"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        AbstractConfigListEntry<Boolean> ignoreCEM = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.general.ignoreCEM"), currentConfig.ignoreCEM)
                .setTooltip(Text.translatable("rprenames.config.general.ignoreCEM.tooltip"))
                .setSaveConsumer(currentConfig::setIgnoreCEM)
                .setDefaultValue(defaultConfig.ignoreCEM)
                .build();

        AbstractConfigListEntry<Boolean> enableAnvilModification = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.general.enableAnvilModification"), currentConfig.enableAnvilModification)
                .setTooltip(Text.translatable("rprenames.config.general.enableAnvilModification.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.enableAnvilModification = newConfig)
                .setDefaultValue(defaultConfig.enableAnvilModification)
                .build();

        AbstractConfigListEntry<Boolean> compareItemGroupRenames = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.general.creativeTabCategory.compareItemGroupRenames"), currentConfig.compareItemGroupRenames)
                .setTooltip(Text.translatable("rprenames.config.general.creativeTabCategory.compareItemGroupRenames.tooltip"))
                .setSaveConsumer(currentConfig::setCompareItemGroupRenames)
                .setDefaultValue(defaultConfig.compareItemGroupRenames)
                .build();

        AbstractConfigListEntry<Boolean> generateSpawnEggsInItemGroup = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.general.creativeTabCategory.generateSpawnEggsInItemGroup"), currentConfig.generateSpawnEggsInItemGroup)
                .setTooltip(Text.translatable("rprenames.config.general.creativeTabCategory.generateSpawnEggsInItemGroup.tooltip"))
                .setSaveConsumer(currentConfig::setGenerateSpawnEggsInItemGroup)
                .setDefaultValue(defaultConfig.generateSpawnEggsInItemGroup)
                .build();

        AbstractConfigListEntry<Boolean> loadModBuiltinResources = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.general.loadModBuiltinResources"), currentConfig.loadModBuiltinResources)
                .setTooltip(Text.translatable("rprenames.config.general.loadModBuiltinResources.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.loadModBuiltinResources = newConfig)
                .setDefaultValue(defaultConfig.loadModBuiltinResources)
                .requireRestart()
                .build();

        AbstractConfigListEntry<Boolean> openByDefault = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.openByDefault"), currentConfig.openByDefault)
                .setTooltip(Text.translatable("rprenames.config.gui.openByDefault.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.openByDefault = newConfig)
                .setDefaultValue(defaultConfig.openByDefault)
                .build();

        AbstractConfigListEntry<Boolean> offsetMenu = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.offsetMenu"), currentConfig.offsetMenu)
                .setTooltip(Text.translatable("rprenames.config.gui.offsetMenu.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.offsetMenu = newConfig)
                .setDefaultValue(defaultConfig.offsetMenu)
                .build();

        AbstractConfigListEntry<FavoriteButton.Position> favoriteButtonPosition = entryBuilder.startEnumSelector(Text.translatable("rprenames.config.gui.favoriteButtonPosition"), FavoriteButton.Position.class, currentConfig.favoriteButtonPosition)
                .setTooltip(Text.translatable("rprenames.config.gui.favoriteButtonPosition.tooltip"))
                .setEnumNameProvider(value -> Text.translatable("rprenames.config.gui.favoriteButtonPosition." + value.name()))
                .setSaveConsumer(newConfig -> currentConfig.favoriteButtonPosition = newConfig)
                .setDefaultValue(defaultConfig.favoriteButtonPosition)
                .build();

        AbstractConfigListEntry<Boolean> showPackName = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.tooltipCategory.showPackName"), currentConfig.showPackName)
                .setTooltip(Text.translatable("rprenames.config.gui.tooltipCategory.showPackName.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showPackName = newConfig)
                .setDefaultValue(defaultConfig.showPackName)
                .build();

        AbstractConfigListEntry<Boolean> showExtraProperties = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.tooltipCategory.showExtraProperties"), currentConfig.showExtraProperties)
                .setTooltip(Text.translatable("rprenames.config.gui.tooltipCategory.showExtraProperties.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showExtraProperties = newConfig)
                .setDefaultValue(defaultConfig.showExtraProperties)
                .build();

        AbstractConfigListEntry<Boolean> highlightTooltipSlotWrong = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.tooltipCategory.slotCategory.highlightTooltipSlotWrong"), currentConfig.highlightTooltipSlotWrong)
                .setTooltip(Text.translatable("rprenames.config.gui.tooltipCategory.slotCategory.highlightTooltipSlotWrong.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightTooltipSlotWrong = newConfig)
                .setDefaultValue(defaultConfig.highlightTooltipSlotWrong)
                .build();

        AbstractConfigListEntry<Boolean> highlightTooltipSlotSelected = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.tooltipCategory.slotCategory.highlightTooltipSlotSelected"), currentConfig.highlightTooltipSlotSelected)
                .setTooltip(Text.translatable("rprenames.config.gui.tooltipCategory.slotCategory.highlightTooltipSlotSelected.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightTooltipSlotSelected = newConfig)
                .setDefaultValue(defaultConfig.highlightTooltipSlotSelected)
                .build();

        AbstractConfigListEntry<Boolean> highlightSelected = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.renderCategory.highlightSelected"), currentConfig.highlightSelected)
                .setTooltip(Text.translatable("rprenames.config.gui.renderCategory.highlightSelected.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightSelected = newConfig)
                .setDefaultValue(defaultConfig.highlightSelected)
                .build();

        AbstractConfigListEntry<Boolean> recolorFavoriteTooltip = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.renderCategory.recolorFavoriteTooltip"), currentConfig.recolorFavoriteTooltip)
                .setTooltip(Text.translatable("rprenames.config.gui.renderCategory.recolorFavoriteTooltip.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.recolorFavoriteTooltip = newConfig)
                .setDefaultValue(defaultConfig.recolorFavoriteTooltip)
                .build();

        AbstractConfigListEntry<Boolean> renderStarInFavoriteTooltip = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.renderCategory.renderStarInFavoriteTooltip"), currentConfig.renderStarInFavoriteTooltip)
                .setTooltip(Text.translatable("rprenames.config.gui.renderCategory.renderStarInFavoriteTooltip.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.renderStarInFavoriteTooltip = newConfig)
                .setDefaultValue(defaultConfig.renderStarInFavoriteTooltip)
                .build();

        AbstractConfigListEntry<Boolean> highlightSlot = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.renderCategory.highlightSlot"), currentConfig.highlightSlot)
                .setTooltip(Text.translatable("rprenames.config.gui.renderCategory.highlightSlot.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightSlot = newConfig)
                .setDefaultValue(defaultConfig.highlightSlot)
                .build();

        AbstractConfigListEntry<Integer> slotHighlightColor = entryBuilder.startColorField(Text.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightColor"), Color.ofTransparent(currentConfig.slotHighlightColorRGB))
                .setTooltip(Text.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightColor.tooltip"))
                .setDefaultValue(defaultConfig.slotHighlightColorRGB)
                .setSaveConsumer(newConfig -> currentConfig.slotHighlightColorRGB = newConfig)
                .build();

        AbstractConfigListEntry<Integer> slotHighlightALPHA = entryBuilder.startIntSlider(Text.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightALPHA"), currentConfig.slotHighlightColorALPHA, 0, 100)
                .setTooltip(Text.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightALPHA.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.slotHighlightColorALPHA = newConfig)
                .setDefaultValue(defaultConfig.slotHighlightColorALPHA)
                .setTextGetter(percent -> {
                    if (percent == 0) {
                        return Text.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightALPHA.off").formatted(Formatting.RED);
                    }
                    return Text.of(percent.toString());
                })
                .build();

        AbstractConfigListEntry<Boolean> enablePreview = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.previewCategory.enablePreview"), currentConfig.enablePreview)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.enablePreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.enablePreview = newConfig)
                .setDefaultValue(defaultConfig.enablePreview)
                .build();

        AbstractConfigListEntry<PreviewTooltipPositioner.PreviewPos> previewPos = entryBuilder.startEnumSelector(Text.translatable("rprenames.config.gui.previewCategory.previewPos"), PreviewTooltipPositioner.PreviewPos.class, currentConfig.previewPos)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.previewPos.tooltip"))
                .setEnumNameProvider(value -> Text.translatable("rprenames.config.gui.previewCategory.previewPos." + value.name()))
                .setSaveConsumer(newConfig -> currentConfig.previewPos = newConfig)
                .setDefaultValue(defaultConfig.previewPos)
                .build();

        AbstractConfigListEntry<Double> scaleFactorItem = entryBuilder.startDoubleField(Text.translatable("rprenames.config.gui.previewCategory.scaleCategory.scaleFactorItem"), currentConfig.scaleFactorItem)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.scaleCategory.scaleFactorItem.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.scaleFactorItem = newConfig)
                .setDefaultValue(defaultConfig.scaleFactorItem)
                .build();

        AbstractConfigListEntry<Double> scaleFactorEntity = entryBuilder.startDoubleField(Text.translatable("rprenames.config.gui.previewCategory.scaleCategory.scaleFactorEntity"), currentConfig.scaleFactorEntity)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.scaleCategory.scaleFactorEntity.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.scaleFactorEntity = newConfig)
                .setDefaultValue(defaultConfig.scaleFactorEntity)
                .build();

        AbstractConfigListEntry<Boolean> playerPreviewByDefault = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.previewCategory.playerCategory.playerPreviewByDefault"), currentConfig.playerPreviewByDefault)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.playerCategory.playerPreviewByDefault.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.playerPreviewByDefault = newConfig)
                .setDefaultValue(defaultConfig.playerPreviewByDefault)
                .build();

        AbstractConfigListEntry<Boolean> spinPlayerPreview = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.previewCategory.playerCategory.spinPlayerPreview"), currentConfig.spinPlayerPreview)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.playerCategory.spinPlayerPreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.spinPlayerPreview = newConfig)
                .setDefaultValue(defaultConfig.spinPlayerPreview)
                .build();

        AbstractConfigListEntry<Boolean> alwaysAllowPlayerPreviewHead = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.previewCategory.playerCategory.alwaysAllowPlayerPreviewHead"), currentConfig.alwaysAllowPlayerPreviewHead)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.playerCategory.alwaysAllowPlayerPreviewHead.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.alwaysAllowPlayerPreviewHead = newConfig)
                .setDefaultValue(defaultConfig.alwaysAllowPlayerPreviewHead)
                .build();

        AbstractConfigListEntry<Boolean> spinMobPreview = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.previewCategory.entityCategory.spinMobPreview"), currentConfig.spinMobPreview)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.entityCategory.spinMobPreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.spinMobPreview = newConfig)
                .setDefaultValue(defaultConfig.spinMobPreview)
                .build();

        AbstractConfigListEntry<Boolean> disableSnowGolemPumpkin = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.previewCategory.entityCategory.disableSnowGolemPumpkin"), currentConfig.disableSnowGolemPumpkin)
                .setTooltip(Text.translatable("rprenames.config.gui.previewCategory.entityCategory.disableSnowGolemPumpkin.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disableSnowGolemPumpkin = newConfig)
                .setDefaultValue(defaultConfig.disableSnowGolemPumpkin)
                .build();

        AbstractConfigListEntry<Boolean> disableTooltipHints = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.hintsCategory.disableTooltipHints"), currentConfig.disableTooltipHints)
                .setTooltip(Text.translatable("rprenames.config.gui.hintsCategory.disableTooltipHints.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disableTooltipHints = newConfig)
                .setDefaultValue(defaultConfig.disableTooltipHints)
                .build();

        AbstractConfigListEntry<Boolean> disablePageArrowsHints = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.hintsCategory.disablePageArrowsHints"), currentConfig.disablePageArrowsHints)
                .setTooltip(Text.translatable("rprenames.config.gui.hintsCategory.disablePageArrowsHints.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disablePageArrowsHints = newConfig)
                .setDefaultValue(defaultConfig.disablePageArrowsHints)
                .build();

        AbstractConfigListEntry<Boolean> updateConfig = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.updateConfig"), currentConfig.updateConfig)
                .setTooltip(Text.translatable("rprenames.config.debug.updateConfig.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.updateConfig = newConfig)
                .setDefaultValue(defaultConfig.updateConfig)
                .build();

        class PrevToggle { boolean bl = false; }
        final PrevToggle prevToggleRecreateConfig = new PrevToggle();
        AbstractConfigListEntry<Boolean> recreateConfig = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.recreateConfig"), false)
                .setTooltip(Text.translatable("rprenames.config.debug.recreateConfig.tooltip"))
                .setYesNoTextSupplier((bl) -> {
                    if (bl != prevToggleRecreateConfig.bl) {
                        RPRenames.LOGGER.info("Recreating config manually");
                        RenamesManager.updateRenames();
                        prevToggleRecreateConfig.bl = bl;
                    }
                    return Text.translatable("rprenames.config.debug.recreateConfig.title").fillStyle(Style.EMPTY.withColor(Formatting.GOLD));
                })
                .setSaveConsumer(newConfig -> currentConfig.shouldUpdateItemGroup = true)
                .build();

        final PrevToggle prevToggleClearConfig = new PrevToggle();
        AbstractConfigListEntry<Boolean> clearConfig = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.clearConfig"), false)
                .setTooltip(Text.translatable("rprenames.config.debug.clearConfig.tooltip"))
                .setYesNoTextSupplier((bl) -> {
                    if (bl != prevToggleClearConfig.bl) {
                        RPRenames.LOGGER.info("Deleting config manually");
                        RenamesManager.clearRenames();
                        prevToggleClearConfig.bl = bl;
                    }
                    return Text.translatable("rprenames.config.debug.clearConfig.title").fillStyle(Style.EMPTY.withColor(Formatting.RED));
                })
                .setSaveConsumer(newConfig -> currentConfig.shouldUpdateItemGroup = true)
                .build();

        AbstractConfigListEntry<Boolean> showNbtDisplayName = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.showNbtDisplayName"), currentConfig.showNamePattern)
                .setTooltip(Text.translatable("rprenames.config.debug.showNbtDisplayName.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showNamePattern = newConfig)
                .setDefaultValue(defaultConfig.showNamePattern)
                .build();

        AbstractConfigListEntry<Boolean> showOriginalProperties = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.showOriginalProperties"), currentConfig.showOriginalProperties)
                .setTooltip(Text.translatable("rprenames.config.debug.showOriginalProperties.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showOriginalProperties = newConfig)
                .setDefaultValue(defaultConfig.showOriginalProperties)
                .build();

        AbstractConfigListEntry<Boolean> fixDelayedPacketsChangingTab = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.fixDelayedPacketsChangingTab"), currentConfig.fixDelayedPacketsChangingTab)
                .setTooltip(Text.translatable("rprenames.config.debug.fixDelayedPacketsChangingTab.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.fixDelayedPacketsChangingTab = newConfig)
                .setDefaultValue(defaultConfig.fixDelayedPacketsChangingTab)
                .build();

        general.addEntry(ignoreCEM);
        general.addEntry(enableAnvilModification);

        SubCategoryBuilder creativeTabCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.general.creativeTabCategory"));
        creativeTabCategory.add(0, compareItemGroupRenames);
        creativeTabCategory.add(1, generateSpawnEggsInItemGroup);

        general.addEntry(creativeTabCategory.build());

        general.addEntry(loadModBuiltinResources);

        gui.addEntry(openByDefault);
        gui.addEntry(offsetMenu);
        gui.addEntry(favoriteButtonPosition);

        SubCategoryBuilder tooltipSlotCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.tooltipCategory.tooltipSlotCategory"));
        tooltipSlotCategory.add(0, highlightTooltipSlotWrong);
        tooltipSlotCategory.add(1, highlightTooltipSlotSelected);

        SubCategoryBuilder tooltipCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.tooltipCategory"));
        tooltipCategory.add(0, showPackName);
        tooltipCategory.add(1, showExtraProperties);
        tooltipCategory.add(2, tooltipSlotCategory.build());

        gui.addEntry(tooltipCategory.build());

        SubCategoryBuilder slotHighlightColorCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory"));
        slotHighlightColorCategory.add(0, slotHighlightColor);
        slotHighlightColorCategory.add(1, slotHighlightALPHA);

        SubCategoryBuilder renderCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.renderCategory"));
        renderCategory.add(0, highlightSelected);
        renderCategory.add(1, recolorFavoriteTooltip);
        renderCategory.add(2, renderStarInFavoriteTooltip);
        renderCategory.add(3, highlightSlot);
        renderCategory.add(4, slotHighlightColorCategory.build());

        gui.addEntry(renderCategory.build());

        SubCategoryBuilder previewScaleCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.previewCategory.previewScaleCategory"));
        previewScaleCategory.add(0, scaleFactorItem);
        previewScaleCategory.add(1, scaleFactorEntity);

        SubCategoryBuilder playerCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.previewCategory.playerCategory"));
        playerCategory.add(0, playerPreviewByDefault);
        playerCategory.add(1, spinPlayerPreview);
        playerCategory.add(2, alwaysAllowPlayerPreviewHead);

        SubCategoryBuilder entityCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.previewCategory.entityCategory"));
        entityCategory.add(0, spinMobPreview);
        entityCategory.add(1, disableSnowGolemPumpkin);

        SubCategoryBuilder previewCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.previewCategory"));
        previewCategory.add(0, enablePreview);
        previewCategory.add(1, previewPos);
        previewCategory.add(2, previewScaleCategory.build());
        previewCategory.add(3, playerCategory.build());
        previewCategory.add(4, entityCategory.build());

        gui.addEntry(previewCategory.build());

        SubCategoryBuilder hintsCategory = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.hintsCategory"));
        hintsCategory.add(0, disableTooltipHints);
        hintsCategory.add(1, disablePageArrowsHints);

        gui.addEntry(hintsCategory.build());

        debug.addEntry(updateConfig);
        debug.addEntry(recreateConfig);
        debug.addEntry(clearConfig);
        debug.addEntry(showNbtDisplayName);
        debug.addEntry(showOriginalProperties);
        debug.addEntry(fixDelayedPacketsChangingTab);

        return builder.build();
    }
}
