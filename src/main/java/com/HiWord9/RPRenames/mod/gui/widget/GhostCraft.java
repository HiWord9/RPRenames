package com.HiWord9.RPRenames.mod.gui.widget;

import com.HiWord9.RPRenames.mod.gui.Graphics;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

import static com.HiWord9.RPRenames.mod.gui.Graphics.*;
import static com.HiWord9.RPRenames.mod.util.Util.*;

public class GhostCraft implements Drawable, Element {
    GhostSlot[] slots;
    public final int length;

    private boolean doRender = false;

    public GhostCraft(GhostSlot... slots) {
        this.slots = slots;
        this.length = this.slots.length;
    }

    public void setStacks(ItemStack... stacks) {
        for (int i = 0; i < slots.length; i++) {
            slots[i].setContent(stacks.length <= i ? null : stacks[i]);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!doRender) return;
        renderSlots(context, mouseX, mouseY, delta);
    }

    private void renderSlots(DrawContext context, int mouseX, int mouseY, float delta) {
        for (GhostSlot slot : slots) slot.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!doRender) return false;

        for (GhostSlot slot : slots) {
            if (slot.isMouseOver(mouseX, mouseY)) {
                reset();
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {return false;}

    public void setRender(boolean doRender) {
        this.doRender = doRender;
    }

    public void setSpecialHighlight(Boolean... forceHighlights) {
        for (int i = 0; i < slots.length; i++) {
            if (
                    i < forceHighlights.length
                    && forceHighlights[i] != null
            ) slots[i].setForceHighlight(forceHighlights[i]);
        }
    }

    public void resetSpecialHighlight() {
        for (GhostSlot slot : slots) slot.setForceHighlight(false);
    }

    public ItemStack getStackInFirstSlot() {
        return slots[0].content;
    }

    public void clearSlots() {
        setStacks();
    }

    public void reset() {
        setRender(false);
        clearSlots();
        resetSpecialHighlight();
    }

    public void offsetX(int x) {
        for (GhostSlot slot : slots) slot.x += x;
    }

    public static class GhostSlot implements Drawable {
        int x;
        int y;
        ItemStack content;
        boolean forceHighlight = false;

        public GhostSlot(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (content != null && !content.isEmpty()) {
                Graphics.renderStack(context, content, x + 1, y + 1);
                if (isMouseOver(mouseX, mouseY)) {
                    context.drawTooltip(textRenderer(), Screen.getTooltipFromItem(client(), content), mouseX, mouseY);
                }
            }
            int color;
            if (forceHighlight) {
                color = HIGHLIGHT_COLOR_SECOND;
            } else if (content != null) {
                color = HIGHLIGHT_COLOR_WRONG;
            } else {
                return;
            }
            context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, color);
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX > x && mouseX < x + SLOT_SIZE && mouseY > y && mouseY < y + SLOT_SIZE;
        }

        public void setForceHighlight(boolean forceHighlight) {
            this.forceHighlight = forceHighlight;
        }

        public void setContent(ItemStack content) {
            this.content = content;
        }
    }
}
