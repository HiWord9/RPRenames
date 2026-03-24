package com.hiword9.rprenames.api.ext.rename.renderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import java.util.List;

public interface Previewable {
    void drawPreview(GuiGraphics context, int mouseX, int mouseY, List<ClientTooltipComponent> mainTooltip);
}
