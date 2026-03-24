package com.hiword9.rprenames.mod.config;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.widget.external.FavoriteButton;
import com.hiword9.rprenames.api.ext.rename.renderer.PreviewTooltipPositioner;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import me.shedaniel.math.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class ModConfigScreenFactory {
    public static Screen create(Screen parent) {
        ModConfig currentConfig = ModConfig.INSTANCE, defaultConfig = new ModConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setDefaultBackgroundTexture(Identifier.fromNamespaceAndPath("minecraft", "textures/block/bookshelf.png"))
                .setTitle(Component.translatable("rprenames.config.title"))
                .setSavingRunnable(currentConfig::write);

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("rprenames.config.general"));
        ConfigCategory gui = builder.getOrCreateCategory(Component.translatable("rprenames.config.gui"));
        ConfigCategory debug = builder.getOrCreateCategory(Component.translatable("rprenames.config.debug"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        AbstractConfigListEntry<Boolean> ignoreCEM = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.general.ignoreCEM"), currentConfig.ignoreCEM)
                .setTooltip(Component.translatable("rprenames.config.general.ignoreCEM.tooltip"))
                .setSaveConsumer(currentConfig::setIgnoreCEM)
                .setDefaultValue(defaultConfig.ignoreCEM)
                .build();

        AbstractConfigListEntry<Boolean> enableAnvilModification = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.general.enableAnvilModification"), currentConfig.enableAnvilModification)
                .setTooltip(Component.translatable("rprenames.config.general.enableAnvilModification.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.enableAnvilModification = newConfig)
                .setDefaultValue(defaultConfig.enableAnvilModification)
                .build();

        AbstractConfigListEntry<Boolean> compareItemGroupRenames = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.general.creativeTabCategory.compareItemGroupRenames"), currentConfig.compareItemGroupRenames)
                .setTooltip(Component.translatable("rprenames.config.general.creativeTabCategory.compareItemGroupRenames.tooltip"))
                .setSaveConsumer(currentConfig::setCompareItemGroupRenames)
                .setDefaultValue(defaultConfig.compareItemGroupRenames)
                .build();

        AbstractConfigListEntry<Boolean> generateSpawnEggsInItemGroup = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.general.creativeTabCategory.generateSpawnEggsInItemGroup"), currentConfig.generateSpawnEggsInItemGroup)
                .setTooltip(Component.translatable("rprenames.config.general.creativeTabCategory.generateSpawnEggsInItemGroup.tooltip"))
                .setSaveConsumer(currentConfig::setGenerateSpawnEggsInItemGroup)
                .setDefaultValue(defaultConfig.generateSpawnEggsInItemGroup)
                .build();

        AbstractConfigListEntry<Boolean> loadModBuiltinResources = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.general.loadModBuiltinResources"), currentConfig.loadModBuiltinResources)
                .setTooltip(Component.translatable("rprenames.config.general.loadModBuiltinResources.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.loadModBuiltinResources = newConfig)
                .setDefaultValue(defaultConfig.loadModBuiltinResources)
                .requireRestart()
                .build();

        AbstractConfigListEntry<Boolean> openByDefault = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.openByDefault"), currentConfig.openByDefault)
                .setTooltip(Component.translatable("rprenames.config.gui.openByDefault.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.openByDefault = newConfig)
                .setDefaultValue(defaultConfig.openByDefault)
                .build();

        AbstractConfigListEntry<Boolean> offsetMenu = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.offsetMenu"), currentConfig.offsetMenu)
                .setTooltip(Component.translatable("rprenames.config.gui.offsetMenu.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.offsetMenu = newConfig)
                .setDefaultValue(defaultConfig.offsetMenu)
                .build();

        AbstractConfigListEntry<FavoriteButton.Position> favoriteButtonPosition = entryBuilder
                .startEnumSelector(Component.translatable("rprenames.config.gui.favoriteButtonPosition"), FavoriteButton.Position.class, currentConfig.favoriteButtonPosition)
                .setTooltip(Component.translatable("rprenames.config.gui.favoriteButtonPosition.tooltip"))
                .setEnumNameProvider(value -> Component.translatable("rprenames.config.gui.favoriteButtonPosition." + value.name()))
                .setSaveConsumer(newConfig -> currentConfig.favoriteButtonPosition = newConfig)
                .setDefaultValue(defaultConfig.favoriteButtonPosition)
                .build();

        AbstractConfigListEntry<Boolean> showPackName = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.tooltipCategory.showPackName"), currentConfig.showPackName)
                .setTooltip(Component.translatable("rprenames.config.gui.tooltipCategory.showPackName.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showPackName = newConfig)
                .setDefaultValue(defaultConfig.showPackName)
                .build();

        AbstractConfigListEntry<Boolean> showExtraProperties = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.tooltipCategory.showExtraProperties"), currentConfig.showExtraProperties)
                .setTooltip(Component.translatable("rprenames.config.gui.tooltipCategory.showExtraProperties.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showExtraProperties = newConfig)
                .setDefaultValue(defaultConfig.showExtraProperties)
                .build();

        AbstractConfigListEntry<Boolean> highlightTooltipSlotWrong = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.tooltipCategory.slotCategory.highlightTooltipSlotWrong"), currentConfig.highlightTooltipSlotWrong)
                .setTooltip(Component.translatable("rprenames.config.gui.tooltipCategory.slotCategory.highlightTooltipSlotWrong.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightTooltipSlotWrong = newConfig)
                .setDefaultValue(defaultConfig.highlightTooltipSlotWrong)
                .build();

        AbstractConfigListEntry<Boolean> highlightTooltipSlotSelected = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.tooltipCategory.slotCategory.highlightTooltipSlotSelected"), currentConfig.highlightTooltipSlotSelected)
                .setTooltip(Component.translatable("rprenames.config.gui.tooltipCategory.slotCategory.highlightTooltipSlotSelected.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightTooltipSlotSelected = newConfig)
                .setDefaultValue(defaultConfig.highlightTooltipSlotSelected)
                .build();

        AbstractConfigListEntry<Boolean> highlightSelected = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.renderCategory.highlightSelected"), currentConfig.highlightSelected)
                .setTooltip(Component.translatable("rprenames.config.gui.renderCategory.highlightSelected.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightSelected = newConfig)
                .setDefaultValue(defaultConfig.highlightSelected)
                .build();

        AbstractConfigListEntry<Boolean> recolorFavoriteTooltip = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.renderCategory.recolorFavoriteTooltip"), currentConfig.recolorFavoriteTooltip)
                .setTooltip(Component.translatable("rprenames.config.gui.renderCategory.recolorFavoriteTooltip.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.recolorFavoriteTooltip = newConfig)
                .setDefaultValue(defaultConfig.recolorFavoriteTooltip)
                .build();

        AbstractConfigListEntry<Boolean> renderStarInFavoriteTooltip = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.renderCategory.renderStarInFavoriteTooltip"), currentConfig.renderStarInFavoriteTooltip)
                .setTooltip(Component.translatable("rprenames.config.gui.renderCategory.renderStarInFavoriteTooltip.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.renderStarInFavoriteTooltip = newConfig)
                .setDefaultValue(defaultConfig.renderStarInFavoriteTooltip)
                .build();

        AbstractConfigListEntry<Boolean> highlightSlot = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.renderCategory.highlightSlot"), currentConfig.highlightSlot)
                .setTooltip(Component.translatable("rprenames.config.gui.renderCategory.highlightSlot.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightSlot = newConfig)
                .setDefaultValue(defaultConfig.highlightSlot)
                .build();

        AbstractConfigListEntry<Integer> slotHighlightColor = entryBuilder
                .startColorField(Component.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightColor"), Color.ofTransparent(currentConfig.slotHighlightColorRGB))
                .setTooltip(Component.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightColor.tooltip"))
                .setDefaultValue(defaultConfig.slotHighlightColorRGB)
                .setSaveConsumer(newConfig -> currentConfig.slotHighlightColorRGB = newConfig)
                .build();

        AbstractConfigListEntry<Integer> slotHighlightALPHA = entryBuilder
                .startIntSlider(Component.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightALPHA"), currentConfig.slotHighlightColorALPHA, 0, 100)
                .setTooltip(Component.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightALPHA.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.slotHighlightColorALPHA = newConfig)
                .setDefaultValue(defaultConfig.slotHighlightColorALPHA)
                .setTextGetter(percent -> {
                    if (percent == 0) {
                        return Component.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory.slotHighlightALPHA.off").withStyle(ChatFormatting.RED);
                    }
                    return Component.nullToEmpty(percent.toString());
                })
                .build();

        AbstractConfigListEntry<Boolean> enablePreview = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.previewCategory.enablePreview"), currentConfig.enablePreview)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.enablePreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.enablePreview = newConfig)
                .setDefaultValue(defaultConfig.enablePreview)
                .build();

        AbstractConfigListEntry<PreviewTooltipPositioner.PreviewPos> previewPos = entryBuilder
                .startEnumSelector(Component.translatable("rprenames.config.gui.previewCategory.previewPos"), PreviewTooltipPositioner.PreviewPos.class, currentConfig.previewPos)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.previewPos.tooltip"))
                .setEnumNameProvider(value -> Component.translatable("rprenames.config.gui.previewCategory.previewPos." + value.name()))
                .setSaveConsumer(newConfig -> currentConfig.previewPos = newConfig)
                .setDefaultValue(defaultConfig.previewPos)
                .build();

        AbstractConfigListEntry<Double> scaleFactorItem = entryBuilder
                .startDoubleField(Component.translatable("rprenames.config.gui.previewCategory.scaleCategory.scaleFactorItem"), currentConfig.scaleFactorItem)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.scaleCategory.scaleFactorItem.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.scaleFactorItem = newConfig)
                .setDefaultValue(defaultConfig.scaleFactorItem)
                .build();

        AbstractConfigListEntry<Double> scaleFactorEntity = entryBuilder
                .startDoubleField(Component.translatable("rprenames.config.gui.previewCategory.scaleCategory.scaleFactorEntity"), currentConfig.scaleFactorEntity)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.scaleCategory.scaleFactorEntity.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.scaleFactorEntity = newConfig)
                .setDefaultValue(defaultConfig.scaleFactorEntity)
                .build();

        AbstractConfigListEntry<Boolean> playerPreviewByDefault = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.previewCategory.playerCategory.playerPreviewByDefault"), currentConfig.playerPreviewByDefault)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.playerCategory.playerPreviewByDefault.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.playerPreviewByDefault = newConfig)
                .setDefaultValue(defaultConfig.playerPreviewByDefault)
                .build();

        AbstractConfigListEntry<Boolean> spinPlayerPreview = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.previewCategory.playerCategory.spinPlayerPreview"), currentConfig.spinPlayerPreview)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.playerCategory.spinPlayerPreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.spinPlayerPreview = newConfig)
                .setDefaultValue(defaultConfig.spinPlayerPreview)
                .build();

        AbstractConfigListEntry<Boolean> alwaysAllowPlayerPreviewHead = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.previewCategory.playerCategory.alwaysAllowPlayerPreviewHead"), currentConfig.alwaysAllowPlayerPreviewHead)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.playerCategory.alwaysAllowPlayerPreviewHead.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.alwaysAllowPlayerPreviewHead = newConfig)
                .setDefaultValue(defaultConfig.alwaysAllowPlayerPreviewHead)
                .build();

        AbstractConfigListEntry<Boolean> spinMobPreview = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.previewCategory.entityCategory.spinMobPreview"), currentConfig.spinMobPreview)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.entityCategory.spinMobPreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.spinMobPreview = newConfig)
                .setDefaultValue(defaultConfig.spinMobPreview)
                .build();

        AbstractConfigListEntry<Boolean> disableSnowGolemPumpkin = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.previewCategory.entityCategory.disableSnowGolemPumpkin"), currentConfig.disableSnowGolemPumpkin)
                .setTooltip(Component.translatable("rprenames.config.gui.previewCategory.entityCategory.disableSnowGolemPumpkin.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disableSnowGolemPumpkin = newConfig)
                .setDefaultValue(defaultConfig.disableSnowGolemPumpkin)
                .build();

        AbstractConfigListEntry<Boolean> disableTooltipHints = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.hintsCategory.disableTooltipHints"), currentConfig.disableTooltipHints)
                .setTooltip(Component.translatable("rprenames.config.gui.hintsCategory.disableTooltipHints.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disableTooltipHints = newConfig)
                .setDefaultValue(defaultConfig.disableTooltipHints)
                .build();

        AbstractConfigListEntry<Boolean> disablePageArrowsHints = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.gui.hintsCategory.disablePageArrowsHints"), currentConfig.disablePageArrowsHints)
                .setTooltip(Component.translatable("rprenames.config.gui.hintsCategory.disablePageArrowsHints.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disablePageArrowsHints = newConfig)
                .setDefaultValue(defaultConfig.disablePageArrowsHints)
                .build();

        AbstractConfigListEntry<Boolean> updateConfig = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.debug.updateConfig"), currentConfig.updateConfig)
                .setTooltip(Component.translatable("rprenames.config.debug.updateConfig.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.updateConfig = newConfig)
                .setDefaultValue(defaultConfig.updateConfig)
                .build();

        class PrevToggle { boolean bl = false; }
        final PrevToggle prevToggleRecreateConfig = new PrevToggle();
        AbstractConfigListEntry<Boolean> recreateConfig = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.debug.recreateConfig"), false)
                .setTooltip(Component.translatable("rprenames.config.debug.recreateConfig.tooltip"))
                .setYesNoTextSupplier(bl -> {
                    if (bl != prevToggleRecreateConfig.bl) {
                        RPRenames.LOGGER.info("Recreating config manually");
                        RPRenames.updatableRenamesManager.updateRenames();
                        prevToggleRecreateConfig.bl = bl;
                    }
                    return Component.translatable("rprenames.config.debug.recreateConfig.title").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
                })
                .setSaveConsumer(newConfig -> currentConfig.shouldUpdateItemGroup = true)
                .build();

        final PrevToggle prevToggleClearConfig = new PrevToggle();
        AbstractConfigListEntry<Boolean> clearConfig = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.debug.clearConfig"), false)
                .setTooltip(Component.translatable("rprenames.config.debug.clearConfig.tooltip"))
                .setYesNoTextSupplier(bl -> {
                    if (bl != prevToggleClearConfig.bl) {
                        RPRenames.LOGGER.info("Deleting config manually");
                        RPRenames.updatableRenamesManager.clearRenames();
                        prevToggleClearConfig.bl = bl;
                    }
                    return Component.translatable("rprenames.config.debug.clearConfig.title").withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                })
                .setSaveConsumer(newConfig -> currentConfig.shouldUpdateItemGroup = true)
                .build();

        AbstractConfigListEntry<Boolean> showNbtDisplayName = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.debug.showNbtDisplayName"), currentConfig.showNamePattern)
                .setTooltip(Component.translatable("rprenames.config.debug.showNbtDisplayName.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showNamePattern = newConfig)
                .setDefaultValue(defaultConfig.showNamePattern)
                .build();

        AbstractConfigListEntry<Boolean> showOriginalProperties = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.debug.showOriginalProperties"), currentConfig.showOriginalProperties)
                .setTooltip(Component.translatable("rprenames.config.debug.showOriginalProperties.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showOriginalProperties = newConfig)
                .setDefaultValue(defaultConfig.showOriginalProperties)
                .build();

        AbstractConfigListEntry<Boolean> fixDelayedPacketsChangingTab = entryBuilder
                .startBooleanToggle(Component.translatable("rprenames.config.debug.fixDelayedPacketsChangingTab"), currentConfig.fixDelayedPacketsChangingTab)
                .setTooltip(Component.translatable("rprenames.config.debug.fixDelayedPacketsChangingTab.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.fixDelayedPacketsChangingTab = newConfig)
                .setDefaultValue(defaultConfig.fixDelayedPacketsChangingTab)
                .build();

        general.addEntry(ignoreCEM);
        general.addEntry(enableAnvilModification);

        SubCategoryBuilder creativeTabCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.general.creativeTabCategory"));
        creativeTabCategory.add(0, compareItemGroupRenames);
        creativeTabCategory.add(1, generateSpawnEggsInItemGroup);

        general.addEntry(creativeTabCategory.build());

        general.addEntry(loadModBuiltinResources);

        gui.addEntry(openByDefault);
        gui.addEntry(offsetMenu);
        gui.addEntry(favoriteButtonPosition);

        SubCategoryBuilder tooltipSlotCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.tooltipCategory.tooltipSlotCategory"));
        tooltipSlotCategory.add(0, highlightTooltipSlotWrong);
        tooltipSlotCategory.add(1, highlightTooltipSlotSelected);

        SubCategoryBuilder tooltipCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.tooltipCategory"));
        tooltipCategory.add(0, showPackName);
        tooltipCategory.add(1, showExtraProperties);
        tooltipCategory.add(2, tooltipSlotCategory.build());

        gui.addEntry(tooltipCategory.build());

        SubCategoryBuilder slotHighlightColorCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.renderCategory.slotHighlightColorCategory"));
        slotHighlightColorCategory.add(0, slotHighlightColor);
        slotHighlightColorCategory.add(1, slotHighlightALPHA);

        SubCategoryBuilder renderCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.renderCategory"));
        renderCategory.add(0, highlightSelected);
        renderCategory.add(1, recolorFavoriteTooltip);
        renderCategory.add(2, renderStarInFavoriteTooltip);
        renderCategory.add(3, highlightSlot);
        renderCategory.add(4, slotHighlightColorCategory.build());

        gui.addEntry(renderCategory.build());

        SubCategoryBuilder previewScaleCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.previewCategory.previewScaleCategory"));
        previewScaleCategory.add(0, scaleFactorItem);
        previewScaleCategory.add(1, scaleFactorEntity);

        SubCategoryBuilder playerCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.previewCategory.playerCategory"));
        playerCategory.add(0, playerPreviewByDefault);
        playerCategory.add(1, spinPlayerPreview);
        playerCategory.add(2, alwaysAllowPlayerPreviewHead);

        SubCategoryBuilder entityCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.previewCategory.entityCategory"));
        entityCategory.add(0, spinMobPreview);
        entityCategory.add(1, disableSnowGolemPumpkin);

        SubCategoryBuilder previewCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.previewCategory"));
        previewCategory.add(0, enablePreview);
        previewCategory.add(1, previewPos);
        previewCategory.add(2, previewScaleCategory.build());
        previewCategory.add(3, playerCategory.build());
        previewCategory.add(4, entityCategory.build());

        gui.addEntry(previewCategory.build());

        SubCategoryBuilder hintsCategory = entryBuilder.startSubCategory(Component.translatable("rprenames.config.gui.hintsCategory"));
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
