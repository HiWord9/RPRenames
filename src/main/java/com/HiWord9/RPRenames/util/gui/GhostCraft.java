package com.HiWord9.RPRenames.util.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
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

    public void render(MatrixStack matrices, int mouseX, int mouseY) {
        if (doRender) {
            renderSlots(matrices);
            renderTooltip(matrices, mouseX, mouseY);
        }
    }

    public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        assert client.currentScreen != null;
        if (mouseY >= eachSlotY && mouseY <= eachSlotY + slotSize) {
            if (!slot1.isEmpty() && (mouseX >= slot1x && mouseX <= slot1x + slotSize)) {
                client.currentScreen.renderTooltip(matrices, client.currentScreen.getTooltipFromItem(slot1), mouseX, mouseY);
            } else if (!slot2.isEmpty() && (mouseX >= slot2x && mouseX <= slot2x + slotSize)) {
                client.currentScreen.renderTooltip(matrices, client.currentScreen.getTooltipFromItem(slot2), mouseX, mouseY);
            } else if (!slot3.isEmpty() && (mouseX >= slot3x && mouseX <= slot3x + slotSize)) {
                client.currentScreen.renderTooltip(matrices, client.currentScreen.getTooltipFromItem(slot3), mouseX, mouseY);
            }
        }
    }

    public void renderSlots(MatrixStack matrices) {
        if (!slot1.isEmpty()) {
            Graphics.renderStack(slot1, slot1x + 1, eachSlotY + 1);
        }
        if (!slot1.isEmpty() || forceRenderBG1) {
            DrawableHelper.fill(matrices, slot1x, eachSlotY, slot1x + slotSize, eachSlotY + slotSize, forceRenderBG1 ? secondaryHighlightColor : highlightColor);
        }
        if (!slot2.isEmpty()) {
            Graphics.renderStack(slot2, slot2x + 1, eachSlotY + 1);
        }
        if (!slot2.isEmpty() || forceRenderBG2) {
            DrawableHelper.fill(matrices, slot2x, eachSlotY, slot2x + slotSize, eachSlotY + slotSize, forceRenderBG2 ? secondaryHighlightColor : highlightColor);
        }
        if (!slot3.isEmpty()) {
            Graphics.renderStack(slot3, slot3x + 1, eachSlotY + 1);
        }
        if (!slot3.isEmpty() || forceRenderBG3) {
            DrawableHelper.fill(matrices, slot3x, eachSlotY, slot3x + slotSize, eachSlotY + slotSize, forceRenderBG3 ? secondaryHighlightColor : highlightColor);
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
