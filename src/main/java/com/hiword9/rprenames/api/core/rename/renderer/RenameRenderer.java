package com.hiword9.rprenames.api.core.rename.renderer;

import com.hiword9.rprenames.api.core.rename.Rename;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface RenameRenderer extends GuiEventListener {
    void onRender(GuiGraphicsExtractor context, int mouseX, int mouseY);
    void onRenderTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY);

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
