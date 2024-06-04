package com.HiWord9.RPRenames.util.gui;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

import static com.HiWord9.RPRenames.util.gui.Graphics.*;

public class MultiItemTooltipComponent implements TooltipComponent {
    private static final ModConfig config = ModConfig.INSTANCE;

    static final Identifier SLOT = new Identifier(RPRenames.MOD_ID, "textures/gui/slot.png");

    public final ArrayList<TooltipItem> items;

    public MultiItemTooltipComponent(ArrayList<TooltipItem> items) {
        this.items = items;
    }

    @Override
    public int getHeight() {
        return SLOT_SIZE * Math.min(2, 1 + (items.size() - 1) / 4) + 3;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        int size = items.size();
        if (size <= 4) {
            return size * SLOT_SIZE;
        }
        return SLOT_SIZE * Math.min(4, 3 + (size - 4) / 3);
    }

    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        int i = 0;
        int size = items.size();
        ArrayList<TooltipItem> sorted = sort(items);
        for (TooltipItem item : sorted) {
            int xOffset;
            int yOffset = 0;
            if (size <= 4) {
                xOffset = i;
            } else if (size <= 6) {
                xOffset = (i % 3);
                yOffset = (i / 3);
            } else {
                xOffset = (i % 4);
                yOffset = (i / 4);
            }
            int j = x + SLOT_SIZE * xOffset;
            int k = y + SLOT_SIZE * yOffset;

            context.drawTexture(SLOT, j, k, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);

            if (i == 7 && size > 8) {
                Graphics.renderText(context, Text.of("+" + (size - 7)), j + SLOT_SIZE / 2, k + 5, true, true);
            } else {
                if (item.isInInventory != null) {
                    if (!item.isInInventory && config.highlightTooltipSlotWrong) {
                        context.fill(j, k, j + SLOT_SIZE, k + SLOT_SIZE, HIGHLIGHT_COLOR_WRONG);
                    }
                    if (item.isInInventory && i == 0 && config.highlightTooltipSlotSelected) {
                        context.fill(j, k, j + SLOT_SIZE, k + SLOT_SIZE, HIGHLIGHT_COLOR_SECOND);
                    }
                }
                Graphics.renderStack(context, item.stack, j + 1, k + 1);
            }

            if (i == 7) break;
            i++;
        }
    }

    private static ArrayList<TooltipItem> sort(ArrayList<TooltipItem> list) {
        ArrayList<TooltipItem> sorted = new ArrayList<>();
        int i = 0;
        int j = 0;

        for (TooltipItem tooltipItem : list) {
            if (tooltipItem.isInInventory == null) {
                sorted.add(tooltipItem);
                continue;
            }
            if (tooltipItem.isInInventory) {
                sorted.add(i, tooltipItem);
                i++;
            } else {
                sorted.add(i + j, tooltipItem);
                j++;
            }
        }

        return sorted;
    }

    public static class TooltipItem {
        private final ItemStack stack;
        private Boolean isInInventory;

        public TooltipItem(ItemStack stack, Boolean isInInventory) {
            this.stack = stack;
            this.isInInventory = isInInventory;
        }

        public void setIsInInventory(Boolean isInInventory) {
            this.isInInventory = isInInventory;
        }
    }
}
