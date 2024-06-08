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

    PreviewTooltipPositioner(ArrayList<TooltipComponent> mainTooltipsComponents) {
        this.tooltipComponents = mainTooltipsComponents;
    }

    @Override
    public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
        if (config.previewPos == PreviewPos.LEFT) {
            x -= (12 + width);
            y -= 12;

            if (y + height + 4 > screenHeight) {
                y = screenHeight - (height + 4);
            }
        } else {
            x += 12;
            int yOffset = -12;
            int tooltipHeight = tooltipComponents.size() == 1 ? -2 : 0;
            for (TooltipComponent component : tooltipComponents) {
                tooltipHeight += component.getHeight();
            }
            yOffset += tooltipHeight + 10;

            int firstTopPoint = y - (height + 22);
            int lastBottomPoint = y + yOffset + height + 4;
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
