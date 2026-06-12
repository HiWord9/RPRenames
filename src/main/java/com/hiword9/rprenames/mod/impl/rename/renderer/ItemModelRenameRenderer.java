package com.hiword9.rprenames.mod.impl.rename.renderer;

import com.hiword9.rprenames.api.ext.rename.renderer.PreviewTooltipPositioner;
import com.hiword9.rprenames.util.Graphics;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.ItemPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.PlayerPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget;
import com.hiword9.rprenames.mod.impl.rename.ItemModelRename;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.hiword9.rprenames.util.Graphics.tooltipOf;
import static com.hiword9.rprenames.util.Util.*;
import static com.hiword9.rprenames.util.Util.currentScreen;

public class ItemModelRenameRenderer extends RichRenameRenderer<ItemModelRename> {
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

    protected ItemModelRenameRenderer(
            ItemModelRename rename, RenderArea renderArea,
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
    protected void addPackNameTooltip() {
        if (!config().showPackName) return;
        super.addPackNameTooltip();
    }

    @Override
    public void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        var tooltipAddition = new ArrayList<ClientTooltipComponent>();

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

        super.extractTooltip(graphics, mouseX, mouseY);

        tooltipComponents.removeAll(tooltipAddition);
    }

    @Override
    public void extractPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY, List<ClientTooltipComponent> mainTooltip) {
        if (!config().enablePreview) return;
        super.extractPreview(graphics, mouseX, mouseY, mainTooltip);
    }

    @Override
    protected boolean shouldPreviewPlayer() {
        return super.shouldPreviewPlayer() != config().playerPreviewByDefault;
    }

    @Override
    protected PreviewTooltipPositioner.PreviewPos getPreviewPositionerPos() {
        return config().previewPos;
    }

    public static class Builder extends RichRenameRenderer.Builder<ItemModelRename> {
        public Builder(ItemModelRename rename, RenderArea renderArea) {
            super(rename, renderArea);
        }

        @Override
        protected RichRenameRenderer<ItemModelRename> getNewRenderer() {
            return new ItemModelRenameRenderer(rename, renderArea, rprWidget, favoriteSupplier);
        }
    }
}
