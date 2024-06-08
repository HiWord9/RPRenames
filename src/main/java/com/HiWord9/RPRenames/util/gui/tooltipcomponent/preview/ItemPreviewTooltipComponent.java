package com.HiWord9.RPRenames.util.gui.tooltipcomponent.preview;

import com.HiWord9.RPRenames.util.gui.Graphics;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;

public class ItemPreviewTooltipComponent implements TooltipComponent {
    final int width;
    final int height;
    final int size;
    public final ItemStack stack;

    private final int stackRenderStartX;
    private final int stackRenderStartY;

    public ItemPreviewTooltipComponent(ItemStack stack, int width, int height, int size) {
        this.width = width;
        this.height = height;
        this.size = size;
        this.stack = stack;

        stackRenderStartX = width / 2 - size / 2;
        stackRenderStartY = height / 2 - size / 2;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        return width;
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        Graphics.renderStack(context, stack, x + stackRenderStartX, y + stackRenderStartY, 400, size);
    }
}
