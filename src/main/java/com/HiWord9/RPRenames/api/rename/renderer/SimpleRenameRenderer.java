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
    protected List<TooltipComponent> tooltipComponents = new ArrayList<>();

    public SimpleRenameRenderer(T rename) {
        this.rename = rename;
        this.stack = rename.toStack();

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
                textRenderer(),
                tooltipComponents,
                mouseX, mouseY,
                HoveredTooltipPositioner.INSTANCE
        );
    }
}
