package com.HiWord9.RPRenames.util.rename.renderer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.ArrayList;

public interface RenameRenderer {
    void onRender(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight);
    void onRenderTooltip(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight);

    interface Preview {
        void drawPreview(DrawContext context, int mouseX, int mouseY, ArrayList<TooltipComponent> tooltipComponents);
    }
}
