package com.HiWord9.RPRenames.mod.impl.rename.renderer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.List;

public interface Previewable {
    void drawPreview(DrawContext context, int mouseX, int mouseY, List<TooltipComponent> mainTooltip);
}
