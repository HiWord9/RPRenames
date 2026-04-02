package com.hiword9.rprenames.mod.impl.rename.renderer;

import com.hiword9.rprenames.api.ext.rename.renderer.PreviewTooltipPositioner;
import com.hiword9.rprenames.mod.gui.Graphics;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.ItemPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.PlayerPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget.Tab;
import com.hiword9.rprenames.mod.impl.rename.CITRename;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.hiword9.rprenames.mod.gui.Graphics.tooltipOf;
import static com.hiword9.rprenames.mod.util.RenameRendererHelper.*;
import static com.hiword9.rprenames.mod.util.Util.*;

public class CITRenameRenderer extends RichRenameRenderer<CITRename> {
    protected static final MutableComponent playerPreviewHintShift = Component.translatable(
            "rprenames.gui.tooltipHint.playerPreview.holdShift",
            Component.translatable("rprenames.key.shift").withStyle(ChatFormatting.GRAY)
    ).withStyle(ChatFormatting.DARK_GRAY);

    protected static final MutableComponent playerPreviewHintF = Component.translatable(
            "rprenames.gui.tooltipHint.playerPreview.pressF",
            Component.translatable("rprenames.key.f").withStyle(ChatFormatting.GRAY)
    ).withStyle(ChatFormatting.DARK_GRAY);

    protected static final MutableComponent favoriteHintAdd = Component.translatable(
            "rprenames.gui.tooltipHint.favorite.add",
            Component.translatable("rprenames.key.rmb").withStyle(ChatFormatting.GRAY)
    ).withStyle(ChatFormatting.DARK_GRAY);

    protected static final MutableComponent favoriteHintRemove = Component.translatable(
            "rprenames.gui.tooltipHint.favorite.remove",
            Component.translatable("rprenames.key.rmb").withStyle(ChatFormatting.GRAY)
    ).withStyle(ChatFormatting.DARK_GRAY);

    protected static final MutableComponent disableHint = Component.translatable(
            "rprenames.gui.tooltipHint.disable",
            Component.translatable("rprenames.gui.tooltipHint.disable.command").withStyle(ChatFormatting.RED)
    ).withStyle(ChatFormatting.DARK_RED);

    protected CITRenameRenderer(
            CITRename rename, RenderArea renderArea,
            RPRWidget rprWidget, Supplier<Boolean> favoriteSupplier
    ) {
        super(rename, renderArea, rprWidget, favoriteSupplier);
    }

    @Override
    protected PlayerPreviewTooltipComponent getPlayerPreviewTooltip() {
        int playerSize = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * config().scaleFactorEntity);
        int playerWidth = (int) (Graphics.DEFAULT_PREVIEW_WIDTH + playerSize * player().getBbWidth() - 1);
        int playerHeight = (int) (Graphics.DEFAULT_PREVIEW_HEIGHT + playerSize * player().getBbHeight() - 1);

        return new PlayerPreviewTooltipComponent(
                player(), stack,
                playerWidth, playerHeight,
                playerSize,
                config().spinPlayerPreview,
                config().alwaysAllowPlayerPreviewHead
        );
    }

    @Override
    protected ItemPreviewTooltipComponent getItemPreviewTooltip() {
        double scaleFactorItem = config().scaleFactorItem;
        int itemSize = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ITEM * scaleFactorItem);
        int itemWidth = (int) ((double) Graphics.DEFAULT_PREVIEW_WIDTH / 2 * scaleFactorItem);
        int itemHeight = (int) ((double) Graphics.DEFAULT_PREVIEW_HEIGHT / 2 * scaleFactorItem);

        return new ItemPreviewTooltipComponent(
                stack,
                itemWidth, itemHeight,
                itemSize
        );
    }

    @Override
    protected void addTopTooltips() {
        if (config().showDescription) {
            var description = descriptionTooltipsComponentsList(rename);
            tooltipComponents.addAll(description);
        }
        super.addTopTooltips();
    }

    @Override
    protected void addMiddleTooltips() {
        super.addMiddleTooltips();
        if (config().showExtraProperties) {
            var extraProperties = extraPropertiesTooltipComponentsList(rprWidget, rename, config().showOriginalProperties);
            tooltipComponents.addAll(extraProperties);
        }
    }

    @Override
    protected void addBottomTooltips() {
        super.addBottomTooltips();
        if (config().showNamePattern && rprWidget.getCurrentTab() != Tab.FAVORITE) {
            ClientTooltipComponent pattern = namePatternTooltipComponent(rename.getOriginalNamePattern());
            if (pattern != null) tooltipComponents.add(pattern);
        }
    }

    @Override
    protected void addPackNameTooltip() {
        if (!config().showPackName) return;
        super.addPackNameTooltip();
    }

    protected static List<ClientTooltipComponent> extraPropertiesTooltipComponentsList(RPRWidget rprWidget, CITRename citRename, boolean asOriginal) {
        ArrayList<Component> extraProperties = new ArrayList<>();

        var stack = rprWidget.pickItemStackForRename(citRename);
        if (stack == null) stack = rprWidget.getActiveItemStack();

        var craftMatcher = new CITRename.CraftMatcher(citRename, stack);

        if (asOriginal) {
            if (citRename.getStackSize() > 1) {
                extraProperties.add(rawPropertyText(
                        "stackSize",
                        citRename.getOriginalStackSize(),
                        craftMatcher.enoughStackSize()
                ));
            }
            if (citRename.getDamage() != null && citRename.getDamage().damage > 0) {
                extraProperties.add(rawPropertyText(
                        "damage",
                        citRename.getOriginalDamage(),
                        craftMatcher.enoughDamage()
                ));
            }
            if (citRename.getEnchantment() != null) {
                extraProperties.add(rawPropertyText(
                        "enchantmentIDs",
                        citRename.getOriginalEnchantment(),
                        craftMatcher.hasEnchant()
                ));
                if (citRename.getOriginalEnchantmentLevel() != null) {
                    extraProperties.add(rawPropertyText(
                            "enchantmentLevels",
                            citRename.getOriginalEnchantmentLevel(),
                            craftMatcher.hasEnchant()
                    ));
                }
            }
        } else {
            if (citRename.getStackSize() > 1) {
                extraProperties.add(styledCondition(
                        Component.translatable("rprenames.gui.tooltipHint.stackSize")
                                .append(" " + citRename.getStackSize()),
                        craftMatcher.enoughStackSize(),
                        ChatFormatting.GRAY
                ));
            }
            if (citRename.getDamage() != null && citRename.getDamage().damage > 0) {
                extraProperties.add(styledCondition(
                        Component.translatable("rprenames.gui.tooltipHint.damage")
                                .append(" %s%s".formatted(
                                        citRename.getDamage().damage,
                                        citRename.getDamage().percent ? "%" : ""
                                )),
                        craftMatcher.enoughDamage(),
                        ChatFormatting.GRAY
                ));
            }
            if (citRename.getEnchantment() != null) {
                Identifier enchant = citRename.getEnchantment();
                extraProperties.add(styledCondition(
                        Component.translatable("rprenames.gui.tooltipHint.enchantment")
                                .append(Component.nullToEmpty(" ")).append(Component.translatable(
                                        "enchantment." + enchant.getNamespace() + "." + enchant.getPath()
                                ))
                                .append(Component.nullToEmpty(" ")).append(Component.translatable(
                                        "enchantment.level." + citRename.getEnchantmentLevel()
                                )),
                        craftMatcher.hasEnchant() && craftMatcher.hasEnoughLevels(),
                        ChatFormatting.GRAY
                ));
            }
        }

        ArrayList<ClientTooltipComponent> propertiesComponents = new ArrayList<>();
        for (Component line : extraProperties) propertiesComponents.add(tooltipOf(line));
        return propertiesComponents;
    }

    protected static MutableComponent rawPropertyText(String propertyName, String propertyValue, boolean isGood) {
        return Component
                .literal(propertyName).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))
                .append(Component.literal("=").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))
                .append(styledCondition(Component.literal(propertyValue), isGood, ChatFormatting.GREEN));
    }

    protected static MutableComponent styledCondition(MutableComponent text, boolean isGood, ChatFormatting goodColor) {
        return text.withStyle(
                Style.EMPTY.withColor(isGood ? goodColor : ChatFormatting.DARK_RED)
        );
    }

    @Override
    public void onRenderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        ArrayList<ClientTooltipComponent> tooltipAddition = new ArrayList<>();

        if (config().enablePreview) {
            boolean shiftDown = hasShiftDown();

            if (!shiftDown && !config().playerPreviewByDefault) {
                if (!config().disableTooltipHints) tooltipAddition.add(tooltipOf(playerPreviewHintShift));
            } else if (shiftDown != config().playerPreviewByDefault) {
                if (!config().disableTooltipHints) tooltipAddition.add(tooltipOf(playerPreviewHintF));

                if (currentScreen() != null) currentScreen().setFocused(null);
            }
        }

        if (!config().disableTooltipHints) {
            tooltipAddition.add(tooltipOf(favoriteSupplier.get() ? favoriteHintRemove : favoriteHintAdd));
            tooltipAddition.add(tooltipOf(disableHint));
        }

        tooltipComponents.addAll(tooltipAddition);

        super.onRenderTooltip(graphics, mouseX, mouseY);

        tooltipComponents.removeAll(tooltipAddition);
    }

    @Override
    public void drawPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY, List<ClientTooltipComponent> mainTooltip) {
        if (!config().enablePreview) return;
        super.drawPreview(graphics, mouseX, mouseY, mainTooltip);
    }

    @Override
    protected boolean shouldPreviewPlayer() {
        return super.shouldPreviewPlayer() != config().playerPreviewByDefault;
    }

    @Override
    protected PreviewTooltipPositioner.PreviewPos getPreviewPositionerPos() {
        return config().previewPos;
    }

    public static class Builder extends RichRenameRenderer.Builder<CITRename> {
        public Builder(CITRename rename, RenderArea renderArea) {
            super(rename, renderArea);
        }

        @Override
        protected RichRenameRenderer<CITRename> getNewRenderer() {
            return new CITRenameRenderer(rename, renderArea, rprWidget, favoriteSupplier);
        }
    }
}
