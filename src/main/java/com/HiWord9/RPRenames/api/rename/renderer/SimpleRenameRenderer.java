package com.HiWord9.RPRenames.api.rename.renderer;

import com.HiWord9.RPRenames.api.rename.Rename;
import com.HiWord9.RPRenames.mod.gui.Graphics;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.HiWord9.RPRenames.mod.util.Util.*;

public class SimpleRenameRenderer<T extends Rename> implements RenameRenderer {
    protected T rename;
    protected ItemStack stack;
    protected RenderArea renderArea;
    protected List<TooltipComponent> tooltipComponents = new ArrayList<>();

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
    public void onRender(DrawContext context, int mouseX, int mouseY) {
        Graphics.renderStack(
                context,
                stack,
                renderArea.getX() + (renderArea.getWidth() - Graphics.STACK_IN_SLOT_SIZE) / 2,
                renderArea.getY() + (renderArea.getHeight() - Graphics.STACK_IN_SLOT_SIZE) / 2
        );
    }

    @Override
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY) {
        Graphics.drawTooltip(
                context,
                textRenderer(),
                tooltipComponents,
                mouseX, mouseY,
                HoveredTooltipPositioner.INSTANCE
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
