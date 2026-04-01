package com.hiword9.rprenames.api.ext.rename.renderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import java.util.List;

public interface Previewable {
    void drawPreview(GuiGraphicsExtractor context, int mouseX, int mouseY, List<ClientTooltipComponent> mainTooltip);
}
