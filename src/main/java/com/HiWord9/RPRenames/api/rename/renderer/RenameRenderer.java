package com.HiWord9.RPRenames.api.rename.renderer;

import com.HiWord9.RPRenames.api.rename.Rename;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;

public interface RenameRenderer extends Element {
    void onRender(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight);
    void onRenderTooltip(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight);

    @Override
    default void setFocused(boolean focused) {}

    @Override
    default boolean isFocused() {
        return false;
    }

    abstract class Builder<R extends Rename> {
        protected final R rename;

        public Builder(R rename) {
            this.rename = rename;
        }

        public abstract RenameRenderer build();
    }
}
