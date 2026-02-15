package com.HiWord9.RPRenames.api.rename.renderer;

import com.HiWord9.RPRenames.api.rename.Rename;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.List;

public interface RenameRenderer {
    void onRender(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight);
    void onRenderTooltip(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight);

    interface Preview {
        void drawPreview(DrawContext context, int mouseX, int mouseY, List<TooltipComponent> mainTooltip);
    }

    abstract class Builder<R extends Rename> {
        protected final R rename;

        public Builder(R rename) {
            this.rename = rename;
        }

        public abstract RenameRenderer build();
    }
}
