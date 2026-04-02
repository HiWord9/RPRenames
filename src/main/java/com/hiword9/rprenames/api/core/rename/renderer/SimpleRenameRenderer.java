package com.hiword9.rprenames.api.core.rename.renderer;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

import static com.hiword9.rprenames.mod.util.Util.*;

public class SimpleRenameRenderer<T extends Rename> implements RenameRenderer {
    protected T rename;
    protected ItemStack stack;
    protected RenderArea renderArea;
    protected List<ClientTooltipComponent> tooltipComponents = new ArrayList<>();

    protected boolean focused = false;

    protected SimpleRenameRenderer(T rename, RenderArea renderArea) {
        this.rename = rename;
        this.stack = rename.toStack();
        this.renderArea = renderArea;
    }

    protected void addTooltips() {
        addNameTooltip();
    }

    protected void addNameTooltip() {
        tooltipComponents.add(Graphics.tooltipOf(rename.getName()));
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Graphics.renderStack(
                graphics,
                stack,
                renderArea.getX() + (renderArea.getWidth() - Graphics.STACK_IN_SLOT_SIZE) / 2,
                renderArea.getY() + (renderArea.getHeight() - Graphics.STACK_IN_SLOT_SIZE) / 2
        );
    }

    @Override
    public void onRenderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Graphics.drawTooltip(
                graphics,
                font(),
                tooltipComponents,
                mouseX, mouseY,
                DefaultTooltipPositioner.INSTANCE
        );
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return renderArea.contains(mouseX, mouseY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        setFocused(isMouseOver(mouseX, mouseY));
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    public static class Builder<R extends Rename> extends RenameRenderer.Builder<R> {
        public Builder(R rename, RenderArea renderArea) {
            super(rename, renderArea);
        }

        @Override
        public SimpleRenameRenderer<R> build() {
            var renderer = new SimpleRenameRenderer<>(rename, renderArea);
            renderer.addTooltips();
            return renderer;
        }
    }
}
