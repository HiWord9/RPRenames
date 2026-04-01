package com.hiword9.rprenames.mod.gui.tooltip_component.preview;

import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.Entity;

public class EntityPreviewTooltipComponent implements ClientTooltipComponent {
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
    public int getHeight(Font textRenderer) {
        return height;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return width;
    }

    @Override
    public void extractImage(Font textRenderer, int x, int y, int width, int height,  GuiGraphicsExtractor context) {
        Graphics.renderEntityInBox(context,
                new ScreenRectangle(x - 2, y - 2, getWidth(textRenderer) + 4, getHeight(textRenderer) + 2),
                size, entity, spin);
    }
}
