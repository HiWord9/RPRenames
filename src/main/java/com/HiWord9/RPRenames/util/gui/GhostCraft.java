package com.HiWord9.RPRenames.util.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GhostCraft {
    final int slotSize = 18;
    final int highlightColor = 822018048;
    final int secondaryHighlightColor = 822018303;
    final MinecraftClient client = MinecraftClient.getInstance();

    public ItemStack slot1 = ItemStack.EMPTY;
    public ItemStack slot2 = ItemStack.EMPTY;
    public ItemStack slot3 = ItemStack.EMPTY;
    boolean forceRenderBG1 = false;
    boolean forceRenderBG2 = false;
    boolean forceRenderBG3 = false;

    final int eachSlotY = 46;
    final int slot1x = 26;
    final int slot2x = 75;
    final int slot3x = 133;

    public boolean doRender = false;

    public GhostCraft() {
    }

    public void setSlots(ItemStack slot1, ItemStack slot2, ItemStack slot3) {
        this.slot1 = slot1;
        this.slot2 = slot2;
        this.slot3 = slot3;
    }

    public void setForceRenderBG(@Nullable Boolean forceRenderBG1, @Nullable Boolean forceRenderBG2, @Nullable Boolean forceRenderBG3) {
        if (forceRenderBG1 != null) {
            this.forceRenderBG1 = forceRenderBG1;
        }
        if (forceRenderBG2 != null) {
            this.forceRenderBG2 = forceRenderBG2;
        }
        if (forceRenderBG3 != null) {
            this.forceRenderBG3 = forceRenderBG3;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        if (doRender) {
            renderSlots(context);
            renderTooltip(context, mouseX, mouseY);
        }
    }

    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        if (mouseY >= eachSlotY && mouseY <= eachSlotY + slotSize) {
            if (!slot1.isEmpty() && (mouseX >= slot1x && mouseX <= slot1x + slotSize)) {
                context.drawTooltip(client.textRenderer, Screen.getTooltipFromItem(client, slot1), mouseX, mouseY);
            } else if (!slot2.isEmpty() && (mouseX >= slot2x && mouseX <= slot2x + slotSize)) {
                context.drawTooltip(client.textRenderer, Screen.getTooltipFromItem(client, slot2), mouseX, mouseY);
            } else if (!slot3.isEmpty() && (mouseX >= slot3x && mouseX <= slot3x + slotSize)) {
                context.drawTooltip(client.textRenderer, Screen.getTooltipFromItem(client, slot3), mouseX, mouseY);
            }
        }
    }

    public void renderSlots(DrawContext context) {
        if (!slot1.isEmpty()) {
            Graphics.renderStack(context, slot1, slot1x + 1, eachSlotY + 1);
        }
        if (!slot1.isEmpty() || forceRenderBG1) {
            context.fill(slot1x, eachSlotY, slot1x + slotSize, eachSlotY + slotSize, forceRenderBG1 ? secondaryHighlightColor : highlightColor);
        }
        if (!slot2.isEmpty()) {
            Graphics.renderStack(context, slot2, slot2x + 1, eachSlotY + 1);
        }
        if (!slot2.isEmpty() || forceRenderBG2) {
            context.fill(slot2x, eachSlotY, slot2x + slotSize, eachSlotY + slotSize, forceRenderBG2 ? secondaryHighlightColor : highlightColor);
        }
        if (!slot3.isEmpty()) {
            Graphics.renderStack(context, slot3, slot3x + 1, eachSlotY + 1);
        }
        if (!slot3.isEmpty() || forceRenderBG3) {
            context.fill(slot3x, eachSlotY, slot3x + slotSize, eachSlotY + slotSize, forceRenderBG3 ? secondaryHighlightColor : highlightColor);
        }
    }

    public void setRender(boolean doRender) {
        this.doRender = doRender;
    }

    public void clearSlots() {
        setSlots(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public void reset() {
        setRender(false);
        clearSlots();
        setForceRenderBG(false, false, false);
    }
}
