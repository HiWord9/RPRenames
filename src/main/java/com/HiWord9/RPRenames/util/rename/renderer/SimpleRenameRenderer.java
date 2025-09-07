package com.HiWord9.RPRenames.util.rename.renderer;

import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.rename.type.Rename;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class SimpleRenameRenderer<T extends Rename> implements RenameRenderer {
    T rename;
    ItemStack stack;
    ArrayList<TooltipComponent> tooltipComponents = new ArrayList<>();

    public SimpleRenameRenderer(T rename) {
        this.rename = rename;
        stack = rename.toStack();

        addNameTooltip();
    }

    protected void addNameTooltip() {
        tooltipComponents.add(Graphics.tooltipOf(rename.getName()));
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        Graphics.renderStack(
                context,
                stack,
                buttonX + (buttonWidth - Graphics.STACK_IN_SLOT_SIZE) / 2,
                buttonY + (buttonHeight - Graphics.STACK_IN_SLOT_SIZE) / 2
        );
    }

    @Override
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        Graphics.drawTooltip(
                context,
                MinecraftClient.getInstance().textRenderer,
                tooltipComponents,
                mouseX, mouseY,
                HoveredTooltipPositioner.INSTANCE
        );
    }
}
