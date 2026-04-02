package com.hiword9.rprenames.mod.gui.tooltip_component.preview;

import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ItemPreviewTooltipComponent implements ClientTooltipComponent {
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
    public int getHeight(Font font) {
        return height;
    }

    @Override
    public int getWidth(Font font) {
        return width;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        Graphics.extractItemStack(graphics, stack, x + stackRenderStartX, y + stackRenderStartY, size);
    }
}
