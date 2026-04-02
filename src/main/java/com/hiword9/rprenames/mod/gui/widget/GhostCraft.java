package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;

import static com.hiword9.rprenames.mod.gui.Graphics.*;
import static com.hiword9.rprenames.mod.util.Util.*;

public class GhostCraft implements Renderable, GuiEventListener, Offsetable {
    public final GhostSlot[] slots;
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!doRender) return;
        extractSlots(graphics, mouseX, mouseY, delta);
    }

    private void extractSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        for (GhostSlot slot : slots) slot.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!doRender) return false;

        for (GhostSlot slot : slots) {
            if (slot.isMouseOver(click.x(), click.y())) {
                reset();
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() { return false; }

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

    @Override
    public void offset(int x, int y) {
        for (GhostSlot slot : slots) slot.offset(x, y);
    }

    public static class GhostSlot extends AbstractWidget implements OffsetableWidget {
        protected ItemStack content;
        protected boolean forceHighlight = false;

        public GhostSlot(int x, int y) {
            this(x, y, SLOT_SIZE);
        }

        protected GhostSlot(int x, int y, int size) {
            super(x, y, size, size, null);
        }

        public void setForceHighlight(boolean forceHighlight) {
            this.forceHighlight = forceHighlight;
        }

        public void setContent(ItemStack content) {
            this.content = content;
        }

        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            if (content != null && !content.isEmpty()) {
                Graphics.extractItemStack(graphics, content, getX() + 1, getY() + 1);
                if (isMouseOver(mouseX, mouseY)) {
                    graphics.setComponentTooltipForNextFrame(font(), Screen.getTooltipFromItem(client(), content), mouseX, mouseY);
                }
            }
            int color;

            if (forceHighlight) color = getForceHighlightColor();
            else if (content != null) color = getWrongHighlightColor();
            else return;

            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), color);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            return active && visible && isMouseOver(click.x(), click.y());
        }

        protected int getForceHighlightColor() {
            return HIGHLIGHT_COLOR_SECOND;
        }

        protected int getWrongHighlightColor() {
            return HIGHLIGHT_COLOR_WRONG;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {}
    }

    public interface Loader {
        void loadGhostCraft(GhostCraft ghostCraft, ItemStack itemStack);
    }
}
