package com.HiWord9.RPRenames.util.gui.widget;

import com.HiWord9.RPRenames.util.gui.Graphics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

import static com.HiWord9.RPRenames.util.gui.Graphics.HIGHLIGHT_COLOR_WRONG;
import static com.HiWord9.RPRenames.util.gui.Graphics.SLOT_SIZE;

public class GhostCraft implements Drawable, Element {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public GhostSlot slot1;
    public GhostSlot slot2;
    public GhostSlot slot3;

    public boolean doRender = false;

    public GhostCraft(GhostSlot slot1, GhostSlot slot2, GhostSlot slot3) {
        this.slot1 = slot1;
        this.slot2 = slot2;
        this.slot3 = slot3;
    }

    public void setStacks(ItemStack stack1, ItemStack stack2, ItemStack stack3) {
        this.slot1.setContent(stack1);
        this.slot2.setContent(stack2);
        this.slot3.setContent(stack3);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!doRender) return;
        renderSlots(context, mouseX, mouseY, delta);
    }

    private void renderSlots(DrawContext context, int mouseX, int mouseY, float delta) {
        slot1.render(context, mouseX, mouseY, delta);
        slot2.render(context, mouseX, mouseY, delta);
        slot3.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!slot1.isMouseOver(mouseX, mouseY) &&
            !slot2.isMouseOver(mouseX, mouseY) &&
            !slot3.isMouseOver(mouseX, mouseY)) return false;
        reset();
        return true;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {return false;}

    public void setRender(boolean doRender) {
        this.doRender = doRender;
    }

    public void setSpecialHighlight(Boolean highlightSlot1, Boolean highlightSlot2, Boolean highlightSlot3) {
        if (highlightSlot1 != null) slot1.setForceHighlight(highlightSlot1);
        if (highlightSlot2 != null) slot2.setForceHighlight(highlightSlot2);
        if (highlightSlot3 != null) slot3.setForceHighlight(highlightSlot3);
    }

    public ItemStack getStackInFirstSlot() {
        return slot1.content;
    }

    public void clearSlots() {
        setStacks(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public void reset() {
        setRender(false);
        clearSlots();
        slot1.setForceHighlight(false);
        slot2.setForceHighlight(false);
        slot3.setForceHighlight(false);
    }

    public void offsetX(int x) {
        this.slot1.x += x;
        this.slot2.x += x;
        this.slot3.x += x;
    }

    public static class GhostSlot implements Drawable {
        int x;
        int y;
        ItemStack content = ItemStack.EMPTY;
        boolean forceHighlight = false;

        public GhostSlot(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (!content.isEmpty()) {
                Graphics.renderStack(context, content, x + 1, y + 1);
                if (isMouseOver(mouseX, mouseY)) {
                    context.drawTooltip(client.textRenderer, Screen.getTooltipFromItem(client, content), mouseX, mouseY);
                }
            }
            int color;
            if (forceHighlight) {
                color = Graphics.HIGHLIGHT_COLOR_SECOND;
            } else if (!content.isEmpty()) {
                color = HIGHLIGHT_COLOR_WRONG;
            } else {
                return;
            }
            context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, color);
        }

        boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX > x && mouseX < x + SLOT_SIZE && mouseY > y && mouseY < y + SLOT_SIZE;
        }

        void setForceHighlight(boolean forceHighlight) {
            this.forceHighlight = forceHighlight;
        }

        void setContent(ItemStack content) {
            this.content = content;
        }
    }
}
