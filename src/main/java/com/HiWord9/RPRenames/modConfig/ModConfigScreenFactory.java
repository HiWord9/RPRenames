package com.HiWord9.RPRenames.modConfig;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.gui.RenameButtonHolder;
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
                .setDefaultBackgroundTexture(new Identifier("minecraft", "textures/block/bookshelf.png"))
                .setTitle(Text.translatable("rprenames.config.title"))
                .setSavingRunnable(currentConfig::write);

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("rprenames.config.general"));
        ConfigCategory gui = builder.getOrCreateCategory(Text.translatable("rprenames.config.gui"));
        ConfigCategory debug = builder.getOrCreateCategory(Text.translatable("rprenames.config.debug"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        AbstractConfigListEntry<Boolean> ignoreCEM = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.general.ignoreCEM"), currentConfig.ignoreCEM)
                .setTooltip(Text.translatable("rprenames.config.general.ignoreCEM.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.ignoreCEM = newConfig)
                .setDefaultValue(defaultConfig.ignoreCEM)
                .build();

        AbstractConfigListEntry<Boolean> enableAnvilModification = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.general.enableAnvilModification"), currentConfig.enableAnvilModification)
                .setTooltip(Text.translatable("rprenames.config.general.enableAnvilModification.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.enableAnvilModification = newConfig)
                .setDefaultValue(defaultConfig.enableAnvilModification)
                .build();

        AbstractConfigListEntry<Integer> favoriteButtonX = entryBuilder.startIntField(Text.translatable("rprenames.config.general.favoriteButtonX"), currentConfig.favoritePosX)
                .setTooltip(Text.translatable("rprenames.config.general.favoriteButtonX.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.favoritePosX = newConfig)
                .setDefaultValue(defaultConfig.favoritePosX)
                .build();

        AbstractConfigListEntry<Integer> favoriteButtonY = entryBuilder.startIntField(Text.translatable("rprenames.config.general.favoriteButtonY"), currentConfig.favoritePosY)
                .setTooltip(Text.translatable("rprenames.config.general.favoriteButtonY.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.favoritePosY = newConfig)
                .setDefaultValue(defaultConfig.favoritePosY)
                .build();

        AbstractConfigListEntry<Boolean> compareItemGroupRenames = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.compareItemGroupRenames"), currentConfig.compareItemGroupRenames)
                .setTooltip(Text.translatable("rprenames.config.gui.compareItemGroupRenames.tooltip"))
                .setSaveConsumer(currentConfig::setCompareItemGroupRenames)
                .setDefaultValue(defaultConfig.compareItemGroupRenames)
                .build();

        AbstractConfigListEntry<Boolean> generateSpawnEggsInItemGroup = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.generateSpawnEggsInItemGroup"), currentConfig.generateSpawnEggsInItemGroup)
                .setTooltip(Text.translatable("rprenames.config.gui.generateSpawnEggsInItemGroup.tooltip"))
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

        AbstractConfigListEntry<RenameButtonHolder.ViewMode> viewMode = entryBuilder.startEnumSelector(Text.translatable("rprenames.config.gui.viewMode"), RenameButtonHolder.ViewMode.class, currentConfig.viewMode)
                .setTooltip(Text.translatable("rprenames.config.gui.viewMode.tooltip"))
                .setEnumNameProvider(value -> {
                    if (value == RenameButtonHolder.ViewMode.LIST) {
                        return Text.translatable("rprenames.config.gui.viewMode.list");
                    } else {
                        return Text.translatable("rprenames.config.gui.viewMode.grid");
                    }
                })
                .setSaveConsumer(newConfig -> currentConfig.viewMode = newConfig)
                .setDefaultValue(defaultConfig.viewMode)
                .build();

        AbstractConfigListEntry<Boolean> showPackName = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.showPackName"), currentConfig.showPackName)
                .setTooltip(Text.translatable("rprenames.config.gui.showPackName.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showPackName = newConfig)
                .setDefaultValue(defaultConfig.showPackName)
                .build();

        AbstractConfigListEntry<Boolean> showExtraProperties = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.showExtraProperties"), currentConfig.showExtraProperties)
                .setTooltip(Text.translatable("rprenames.config.gui.showExtraProperties.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showExtraProperties = newConfig)
                .setDefaultValue(defaultConfig.showExtraProperties)
                .build();

        AbstractConfigListEntry<Boolean> highlightSlot = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.highlightSlot"), currentConfig.highlightSlot)
                .setTooltip(Text.translatable("rprenames.config.gui.highlightSlot.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightSlot = newConfig)
                .setDefaultValue(defaultConfig.highlightSlot)
                .build();

        AbstractConfigListEntry<Boolean> highlightTooltipSlotWrong = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.highlightTooltipSlotWrong"), currentConfig.highlightTooltipSlotWrong)
                .setTooltip(Text.translatable("rprenames.config.gui.highlightTooltipSlotWrong.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightTooltipSlotWrong = newConfig)
                .setDefaultValue(defaultConfig.highlightTooltipSlotWrong)
                .build();

        AbstractConfigListEntry<Boolean> highlightTooltipSlotSelected = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.highlightTooltipSlotSelected"), currentConfig.highlightTooltipSlotSelected)
                .setTooltip(Text.translatable("rprenames.config.gui.highlightTooltipSlotSelected.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.highlightTooltipSlotSelected = newConfig)
                .setDefaultValue(defaultConfig.highlightTooltipSlotSelected)
                .build();

        AbstractConfigListEntry<Integer> slotHighlightColor = entryBuilder.startColorField(Text.translatable("rprenames.config.gui.slotHighlightColor"), Color.ofTransparent(currentConfig.slotHighlightColorRGB))
                .setTooltip(Text.translatable("rprenames.config.gui.slotHighlightColor.tooltip"))
                .setDefaultValue(defaultConfig.slotHighlightColorRGB)
                .setSaveConsumer(newConfig -> currentConfig.slotHighlightColorRGB = newConfig)
                .build();

        AbstractConfigListEntry<Integer> slotHighlightALPHA = entryBuilder.startIntSlider(Text.translatable("rprenames.config.gui.slotHighlightALPHA"), currentConfig.slotHighlightColorALPHA, 0, 100)
                .setTooltip(Text.translatable("rprenames.config.gui.slotHighlightALPHA.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.slotHighlightColorALPHA = newConfig)
                .setDefaultValue(defaultConfig.slotHighlightColorALPHA)
                .setTextGetter(percent -> {
                    if (percent == 0) {
                        return Text.translatable("rprenames.config.gui.slotHighlightALPHA.off").formatted(Formatting.RED);
                    }
                    return Text.of(percent.toString());
                })
                .build();

        AbstractConfigListEntry<Boolean> enablePreview = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.enablePreview"), currentConfig.enablePreview)
                .setTooltip(Text.translatable("rprenames.config.gui.enablePreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.enablePreview = newConfig)
                .setDefaultValue(defaultConfig.enablePreview)
                .build();

        AbstractConfigListEntry<RenameButtonHolder.PreviewPos> previewPos = entryBuilder.startEnumSelector(Text.translatable("rprenames.config.gui.previewPos"), RenameButtonHolder.PreviewPos.class, currentConfig.previewPos)
                .setTooltip(Text.translatable("rprenames.config.gui.previewPos.tooltip"))
                .setEnumNameProvider(value -> {
                    if (value == RenameButtonHolder.PreviewPos.BOTTOM) {
                        return Text.translatable("rprenames.config.gui.previewPos.bottom");
                    } else if (value == RenameButtonHolder.PreviewPos.LEFT) {
                        return Text.translatable("rprenames.config.gui.previewPos.left");
                    } else {
                        return Text.translatable("rprenames.config.gui.previewPos.top");
                    }
                })
                .setSaveConsumer(newConfig -> currentConfig.previewPos = newConfig)
                .setDefaultValue(defaultConfig.previewPos)
                .build();

        AbstractConfigListEntry<Boolean> playerPreviewByDefault = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.playerPreviewByDefault"), currentConfig.playerPreviewByDefault)
                .setTooltip(Text.translatable("rprenames.config.gui.playerPreviewByDefault.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.playerPreviewByDefault = newConfig)
                .setDefaultValue(defaultConfig.playerPreviewByDefault)
                .build();

        AbstractConfigListEntry<Boolean> spinMobPreview = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.spinMobPreview"), currentConfig.spinMobPreview)
                .setTooltip(Text.translatable("rprenames.config.gui.spinMobPreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.spinMobPreview = newConfig)
                .setDefaultValue(defaultConfig.spinMobPreview)
                .build();

        AbstractConfigListEntry<Boolean> spinPlayerPreview = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.spinPlayerPreview"), currentConfig.spinPlayerPreview)
                .setTooltip(Text.translatable("rprenames.config.gui.spinPlayerPreview.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.spinPlayerPreview = newConfig)
                .setDefaultValue(defaultConfig.spinPlayerPreview)
                .build();

        AbstractConfigListEntry<Boolean> disableSnowGolemPumpkin = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.disableSnowGolemPumpkin"), currentConfig.disableSnowGolemPumpkin)
                .setTooltip(Text.translatable("rprenames.config.gui.disableSnowGolemPumpkin.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disableSnowGolemPumpkin = newConfig)
                .setDefaultValue(defaultConfig.disableSnowGolemPumpkin)
                .build();

        AbstractConfigListEntry<Double> scaleFactorItem = entryBuilder.startDoubleField(Text.translatable("rprenames.config.gui.scaleFactorItem"), currentConfig.scaleFactorItem)
                .setTooltip(Text.translatable("rprenames.config.gui.scaleFactorItem.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.scaleFactorItem = newConfig)
                .setDefaultValue(defaultConfig.scaleFactorItem)
                .build();

        AbstractConfigListEntry<Double> scaleFactorEntity = entryBuilder.startDoubleField(Text.translatable("rprenames.config.gui.scaleFactorEntity"), currentConfig.scaleFactorEntity)
                .setTooltip(Text.translatable("rprenames.config.gui.scaleFactorEntity.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.scaleFactorEntity = newConfig)
                .setDefaultValue(defaultConfig.scaleFactorEntity)
                .build();

        AbstractConfigListEntry<Boolean> alwaysAllowPlayerPreviewHead = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.alwaysAllowPlayerPreviewHead"), currentConfig.alwaysAllowPlayerPreviewHead)
                .setTooltip(Text.translatable("rprenames.config.gui.alwaysAllowPlayerPreviewHead.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.alwaysAllowPlayerPreviewHead = newConfig)
                .setDefaultValue(defaultConfig.alwaysAllowPlayerPreviewHead)
                .build();

        AbstractConfigListEntry<Boolean> disablePageArrowsTips = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.disablePageArrowsTips"), currentConfig.disablePageArrowsTips)
                .setTooltip(Text.translatable("rprenames.config.gui.disablePageArrowsTips.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disablePageArrowsTips = newConfig)
                .setDefaultValue(defaultConfig.disablePageArrowsTips)
                .build();

        AbstractConfigListEntry<Boolean> disablePlayerPreviewTips = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.gui.disablePlayerPreviewTips"), currentConfig.disablePlayerPreviewTips)
                .setTooltip(Text.translatable("rprenames.config.gui.disablePlayerPreviewTips.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.disablePlayerPreviewTips = newConfig)
                .setDefaultValue(defaultConfig.disablePlayerPreviewTips)
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
                        ConfigManager.parseRenames();
                        prevToggleRecreateConfig.bl = bl;
                    }
                    return Text.translatable("rprenames.config.debug.recreateConfig.title").fillStyle(Style.EMPTY.withColor(Formatting.GOLD));
                })
                .build();

        final PrevToggle prevToggleClearConfig = new PrevToggle();
        AbstractConfigListEntry<Boolean> clearConfig = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.clearConfig"), false)
                .setTooltip(Text.translatable("rprenames.config.debug.clearConfig.tooltip"))
                .setYesNoTextSupplier((bl) -> {
                    if (bl != prevToggleClearConfig.bl) {
                        RPRenames.LOGGER.info("Deleting config manually");
                        ConfigManager.configClear();
                        prevToggleClearConfig.bl = bl;
                    }
                    return Text.translatable("rprenames.config.debug.clearConfig.title").fillStyle(Style.EMPTY.withColor(Formatting.RED));
                })
                .build();

        AbstractConfigListEntry<Boolean> showNbtDisplayName = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.showNbtDisplayName"), currentConfig.showNbtDisplayName)
                .setTooltip(Text.translatable("rprenames.config.debug.showNbtDisplayName.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showNbtDisplayName = newConfig)
                .setDefaultValue(defaultConfig.showNbtDisplayName)
                .build();

        AbstractConfigListEntry<Boolean> showOriginalProperties = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.debug.showOriginalProperties"), currentConfig.showOriginalProperties)
                .setTooltip(Text.translatable("rprenames.config.debug.showOriginalProperties.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showOriginalProperties = newConfig)
                .setDefaultValue(defaultConfig.showOriginalProperties)
                .build();

        SubCategoryBuilder favoriteButtonPosition = entryBuilder.startSubCategory(Text.translatable("rprenames.config.general.subCategory.favoriteButtonPosition"));
        favoriteButtonPosition.add(0, favoriteButtonX);
        favoriteButtonPosition.add(1, favoriteButtonY);

        SubCategoryBuilder slotHighlightColorSettings = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.subCategory.slotHighlightColorSettings"));
        slotHighlightColorSettings.add(0, slotHighlightColor);
        slotHighlightColorSettings.add(1, slotHighlightALPHA);

        SubCategoryBuilder tooltipSlotHighlightSettings = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.subCategory.tooltipSlotHighlightSettings"));
        tooltipSlotHighlightSettings.add(0, highlightTooltipSlotWrong);
        tooltipSlotHighlightSettings.add(1, highlightTooltipSlotSelected);

        SubCategoryBuilder previewScale = entryBuilder.startSubCategory(Text.translatable("rprenames.config.gui.subCategory.previewScale"));
        previewScale.add(0, scaleFactorItem);
        previewScale.add(1, scaleFactorEntity);

        general.addEntry(ignoreCEM);
        general.addEntry(enableAnvilModification);
        general.addEntry(favoriteButtonPosition.build());
        general.addEntry(compareItemGroupRenames);
        general.addEntry(generateSpawnEggsInItemGroup);
        general.addEntry(loadModBuiltinResources);
        gui.addEntry(openByDefault);
        gui.addEntry(viewMode);
        gui.addEntry(showPackName);
        gui.addEntry(showExtraProperties);
        gui.addEntry(highlightSlot);
        gui.addEntry(slotHighlightColorSettings.build());
        gui.addEntry(tooltipSlotHighlightSettings.build());
        gui.addEntry(enablePreview);
        gui.addEntry(previewPos);
        gui.addEntry(playerPreviewByDefault);
        gui.addEntry(spinMobPreview);
        gui.addEntry(spinPlayerPreview);
        gui.addEntry(disableSnowGolemPumpkin);
        gui.addEntry(previewScale.build());
        gui.addEntry(alwaysAllowPlayerPreviewHead);
        gui.addEntry(disablePageArrowsTips);
        gui.addEntry(disablePlayerPreviewTips);
        debug.addEntry(updateConfig);
        debug.addEntry(recreateConfig);
        debug.addEntry(clearConfig);
        debug.addEntry(showNbtDisplayName);
        debug.addEntry(showOriginalProperties);

        return builder.build();
    }
}
