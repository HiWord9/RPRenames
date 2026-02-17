package com.HiWord9.RPRenames.api.rename.renderer;

import com.HiWord9.RPRenames.api.rename.Rename;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;

public interface RenameRenderer extends Element {
    void onRender(DrawContext context, int mouseX, int mouseY);
    void onRenderTooltip(DrawContext context, int mouseX, int mouseY);

    @Override
    default void setFocused(boolean focused) {}

    @Override
    default boolean isFocused() {
        return false;
    }

    interface RenderArea {
        int getX();
        int getY();
        int getWidth();
        int getHeight();

        default boolean contains(double x, double y) {
            return (x >= getX() && x <= getX() + getWidth())
                    && (y >= getY() && y <= getY() + getHeight());
        }
    }

    abstract class Builder<R extends Rename> {
        protected final R rename;
        protected final RenderArea renderArea;

        public Builder(R rename, RenderArea renderArea) {
            this.rename = rename;
            this.renderArea = renderArea;
        }

        public abstract RenameRenderer build();
    }
}
