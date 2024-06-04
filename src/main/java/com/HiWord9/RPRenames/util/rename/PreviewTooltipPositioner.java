package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;

public class PreviewTooltipPositioner implements TooltipPositioner {
    private static final ModConfig config = ModConfig.INSTANCE;
    ArrayList<TooltipComponent> tooltipComponents;

    PreviewTooltipPositioner(ArrayList<TooltipComponent> tooltipComponents) {
        this.tooltipComponents = tooltipComponents;
    }

    @Override
    public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
        if (config.previewPos == PreviewPos.LEFT) {
            x -= (5 + width);
            y -= 16;

            if (y + height > screenHeight) {
                y = screenHeight - height;
            }
        } else {
            x += 8;
            int yOffset = -14;
            int tooltipHeight = tooltipComponents.size() > 1 ? 8 : 6;
            for (TooltipComponent component : tooltipComponents) {
                tooltipHeight += component.getHeight();
            }
            yOffset += tooltipHeight;

            int firstTopPoint = y - (height + 18);
            int lastBottomPoint = y + yOffset + height;
            int breakPoint = height / 2;
            if (config.previewPos == PreviewPos.BOTTOM ?
                    ((firstTopPoint) > -breakPoint) && lastBottomPoint > screenHeight :
                    !(firstTopPoint < 0 && (lastBottomPoint - screenHeight < breakPoint))) {
                y = firstTopPoint;
            } else {
                y += yOffset;
            }
        }
        return new Vector2i(x, y);
    }

    public enum PreviewPos {
        BOTTOM,
        LEFT,
        TOP
    }
}
