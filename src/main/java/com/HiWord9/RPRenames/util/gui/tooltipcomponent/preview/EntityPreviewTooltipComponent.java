package com.HiWord9.RPRenames.util.gui.tooltipcomponent.preview;

import com.HiWord9.RPRenames.util.gui.Graphics;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.Entity;

public class EntityPreviewTooltipComponent implements TooltipComponent {
    final int width;
    final int height;
    final int size;
    final boolean spin;
    public final Entity entity;

    public EntityPreviewTooltipComponent(Entity entity, int width, int height, int size, boolean spin) {
        this.width = width;
        this.height = height;
        this.size = size;
        this.spin = spin;
        this.entity = entity;
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
        Graphics.renderEntityInBox(context,
                new ScreenRect(x - 2, y - 2, width + 4, height + 2),
                size, entity, spin);
    }
}
