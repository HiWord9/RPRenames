package com.hiword9.rprenames.mod.gui.tooltip_component;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

import static com.hiword9.rprenames.mod.gui.Graphics.*;
import static com.hiword9.rprenames.mod.util.Util.config;

public class MultiItemTooltipComponent implements ClientTooltipComponent {
    static final Identifier SLOT = Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "textures/gui/slot.png");

    public final ArrayList<TooltipItem> items = new ArrayList<>();

    public MultiItemTooltipComponent(List<TooltipItem> items) {
        this.items.addAll(items);
    }

    @Override
    public int getHeight(Font textRenderer) {
        return SLOT_SIZE * Math.min(2, 1 + (items.size() - 1) / 4) + 3;
    }

    @Override
    public int getWidth(Font textRenderer) {
        int size = items.size();
        if (size <= 4) {
            return size * SLOT_SIZE;
        }
        return SLOT_SIZE * Math.min(4, 3 + (size - 4) / 3);
    }

    public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor context) {
        int i = 0;
        int size = items.size();
        var sorted = sort(items);
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

            Graphics.renderGuiTexture(
                    context,
                    SLOT,
                    j, k, 0, 0,
                    SLOT_SIZE, SLOT_SIZE,
                    SLOT_SIZE, SLOT_SIZE
            );

            if (i == 7 && size > 8) {
                Graphics.renderText(context, Component.nullToEmpty("+" + (size - 7)), j + SLOT_SIZE / 2, k + 5, true, true);
            } else {
                if (item.isInInventory != null) {
                    if (!item.isInInventory && config().highlightTooltipSlotWrong) {
                        context.fill(j, k, j + SLOT_SIZE, k + SLOT_SIZE, HIGHLIGHT_COLOR_WRONG);
                    }
                    if (item.isInInventory && i == 0 && config().highlightTooltipSlotSelected) {
                        context.fill(j, k, j + SLOT_SIZE, k + SLOT_SIZE, HIGHLIGHT_COLOR_SECOND);
                    }
                }
                Graphics.renderStack(context, item.stack, j + 1, k + 1);
            }

            if (i == 7) break;
            i++;
        }
    }

    private static List<TooltipItem> sort(List<TooltipItem> list) {
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
        public final ItemStack stack;
        public Boolean isInInventory;

        public TooltipItem(ItemStack stack, Boolean isInInventory) {
            this.stack = stack;
            this.isInInventory = isInInventory;
        }
    }
}
