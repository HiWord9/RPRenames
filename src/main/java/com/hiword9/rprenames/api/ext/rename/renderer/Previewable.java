package com.hiword9.rprenames.api.ext.rename.renderer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.List;

public interface Previewable {
    void drawPreview(DrawContext context, int mouseX, int mouseY, List<TooltipComponent> mainTooltip);
}
