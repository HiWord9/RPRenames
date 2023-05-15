package com.HiWord9.RPRenames.modConfig;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModConfigScreenFactory {
    public static Screen create(Screen parent) {
        ModConfig currentConfig = ModConfig.INSTANCE, defaultConfig = new ModConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setDefaultBackgroundTexture(new Identifier("minecraft", "textures/block/bookshelf.png"))
                .setTitle(Text.translatable("rprenames.config.title"))
                .setSavingRunnable(currentConfig::executesOnExit);

        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("rprenames.config.category.main"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        AbstractConfigListEntry<Boolean> createConfigToggle = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.createConfigToggle"), currentConfig.createConfig)
                .setTooltip(Text.translatable("rprenames.config.createConfigToggle.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.createConfig = newConfig)
                .setDefaultValue(defaultConfig.createConfig)
                .build();

        AbstractConfigListEntry<Boolean> createConfigServerToggle = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.createConfigServerToggle"), currentConfig.createConfigServer)
                .setTooltip(Text.translatable("rprenames.config.createConfigServerToggle.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.createConfigServer = newConfig)
                .setDefaultValue(defaultConfig.createConfigServer)
                .build();

        AbstractConfigListEntry<Boolean> enableAnvilModification = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.enableAnvilModification"), currentConfig.enableAnvilModification)
                .setTooltip(Text.translatable("rprenames.config.enableAnvilModification.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.enableAnvilModification = newConfig)
                .setDefaultValue(defaultConfig.enableAnvilModification)
                .build();

        AbstractConfigListEntry<Boolean> showCreateConfigCheckbox = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.showCreateConfigCheckbox"), currentConfig.showCreateConfigCheckbox)
                .setTooltip(Text.translatable("rprenames.config.showCreateConfigCheckbox.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.showCreateConfigCheckbox = newConfig)
                .setDefaultValue(defaultConfig.showCreateConfigCheckbox)
                .build();

        AbstractConfigListEntry<Integer> packCheckboxX = entryBuilder.startIntField(Text.translatable("rprenames.config.packCheckboxX"), currentConfig.createConfigCheckboxPosX)
                .setTooltip(Text.translatable("rprenames.config.packCheckboxX.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.createConfigCheckboxPosX = newConfig)
                .setDefaultValue(defaultConfig.createConfigCheckboxPosX)
                .build();

        AbstractConfigListEntry<Integer> packCheckboxY = entryBuilder.startIntField(Text.translatable("rprenames.config.packCheckboxY"), currentConfig.createConfigCheckboxPosY)
                .setTooltip(Text.translatable("rprenames.config.packCheckboxY.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.createConfigCheckboxPosY = newConfig)
                .setDefaultValue(defaultConfig.createConfigCheckboxPosY)
                .build();

        AbstractConfigListEntry<Integer> favoriteButtonX = entryBuilder.startIntField(Text.translatable("rprenames.config.favoriteButtonX"), currentConfig.favoritePosX)
                .setTooltip(Text.translatable("rprenames.config.favoriteButtonX.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.favoritePosX = newConfig)
                .setDefaultValue(defaultConfig.favoritePosX)
                .build();

        AbstractConfigListEntry<Integer> favoriteButtonY = entryBuilder.startIntField(Text.translatable("rprenames.config.favoriteButtonY"), currentConfig.favoritePosY)
                .setTooltip(Text.translatable("rprenames.config.favoriteButtonY.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.favoritePosY = newConfig)
                .setDefaultValue(defaultConfig.favoritePosY)
                .build();

        AbstractConfigListEntry<Boolean> recreateConfig = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.recreateConfig"), currentConfig.recreateConfig)
                .setTooltip(Text.translatable("rprenames.config.recreateConfig.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.recreateConfig = newConfig)
                .setDefaultValue(defaultConfig.recreateConfig)
                .build();

        AbstractConfigListEntry<Boolean> loadModBuiltinResources = entryBuilder.startBooleanToggle(Text.translatable("rprenames.config.loadModBuiltinResources"), currentConfig.loadModBuiltinResources)
                .setTooltip(Text.translatable("rprenames.config.loadModBuiltinResources.tooltip"))
                .setSaveConsumer(newConfig -> currentConfig.loadModBuiltinResources = newConfig)
                .setDefaultValue(defaultConfig.loadModBuiltinResources)
                .requireRestart()
                .build();

        SubCategoryBuilder createConfigCheckboxPosition = entryBuilder.startSubCategory(Text.translatable("rprenames.config.createConfigCheckboxPosition"));
        createConfigCheckboxPosition.add(0, packCheckboxX);
        createConfigCheckboxPosition.add(1, packCheckboxY);

        SubCategoryBuilder favoriteButtonPosition = entryBuilder.startSubCategory(Text.translatable("rprenames.config.favoriteButtonPosition"));
        favoriteButtonPosition.add(0, favoriteButtonX);
        favoriteButtonPosition.add(1, favoriteButtonY);

        category.addEntry(createConfigToggle);
        category.addEntry(createConfigServerToggle);
        category.addEntry(enableAnvilModification);
        category.addEntry(showCreateConfigCheckbox);
        category.addEntry(createConfigCheckboxPosition.build());
        category.addEntry(favoriteButtonPosition.build());
        category.addEntry(recreateConfig);
        category.addEntry(loadModBuiltinResources);

        return builder.build();
    }
}
