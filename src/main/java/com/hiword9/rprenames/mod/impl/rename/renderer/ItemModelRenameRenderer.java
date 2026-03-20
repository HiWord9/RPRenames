package com.hiword9.rprenames.mod.impl.rename.renderer;

import com.hiword9.rprenames.api.ext.rename.renderer.PreviewTooltipPositioner;
import com.hiword9.rprenames.mod.gui.Graphics;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.ItemPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.PlayerPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget;
import com.hiword9.rprenames.mod.impl.rename.ItemModelRename;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.hiword9.rprenames.mod.gui.Graphics.tooltipOf;
import static com.hiword9.rprenames.mod.util.Util.*;
import static com.hiword9.rprenames.mod.util.Util.currentScreen;

public class ItemModelRenameRenderer extends RichRenameRenderer<ItemModelRename> {
    protected static final MutableText playerPreviewHintShift = Text.translatable(
            "rprenames.gui.tooltipHint.playerPreview.holdShift",
            Text.translatable("rprenames.key.shift").formatted(Formatting.GRAY)
    ).formatted(Formatting.DARK_GRAY);

    protected static final MutableText playerPreviewHintF = Text.translatable(
            "rprenames.gui.tooltipHint.playerPreview.pressF",
            Text.translatable("rprenames.key.f").formatted(Formatting.GRAY)
    ).formatted(Formatting.DARK_GRAY);

    protected static final MutableText favoriteHintAdd = Text.translatable(
            "rprenames.gui.tooltipHint.favorite.add",
            Text.translatable("rprenames.key.rmb").formatted(Formatting.GRAY)
    ).formatted(Formatting.DARK_GRAY);

    protected static final MutableText favoriteHintRemove = Text.translatable(
            "rprenames.gui.tooltipHint.favorite.remove",
            Text.translatable("rprenames.key.rmb").formatted(Formatting.GRAY)
    ).formatted(Formatting.DARK_GRAY);

    protected static final MutableText disableHint = Text.translatable(
            "rprenames.gui.tooltipHint.disable",
            Text.translatable("rprenames.gui.tooltipHint.disable.command").formatted(Formatting.RED)
    ).formatted(Formatting.DARK_RED);

    protected ItemModelRenameRenderer(
            ItemModelRename rename, RenderArea renderArea,
            RPRWidget rprWidget, Supplier<Boolean> favoriteSupplier
    ) {
        super(rename, renderArea, rprWidget, favoriteSupplier);
    }

    @Override
    protected PlayerPreviewTooltipComponent getPlayerPreviewTooltip() {
        int playerSize = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * config().scaleFactorEntity);
        int playerWidth = (int) (Graphics.DEFAULT_PREVIEW_WIDTH + playerSize * player().getWidth() - 1);
        int playerHeight = (int) (Graphics.DEFAULT_PREVIEW_HEIGHT + playerSize * player().getHeight() - 1);

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
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY) {
        var tooltipAddition = new ArrayList<TooltipComponent>();

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

        super.onRenderTooltip(context, mouseX, mouseY);

        tooltipComponents.removeAll(tooltipAddition);
    }

    @Override
    public void drawPreview(DrawContext context, int mouseX, int mouseY, List<TooltipComponent> mainTooltip) {
        if (!config().enablePreview) return;
        super.drawPreview(context, mouseX, mouseY, mainTooltip);
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
