package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static com.hiword9.rprenames.mod.util.Util.*;

public class RandomButton extends AbstractWidget implements OffsetableWidget {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "textures/gui/dice.png");
    static final String TOOLTIP_KEY = "rprenames.gui.randomButton.tooltip";

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 9;
    public static final int BUTTON_HEIGHT = 9;

    public static final int SIDES = 6;

    public static final int TEXTURE_WIDTH = BUTTON_WIDTH;
    public static final int TEXTURE_HEIGHT = BUTTON_HEIGHT * SIDES;
    static final int V_OFFSET = BUTTON_HEIGHT;

    int side;

    public RandomButton(RPRWidget instance, int x, int y, int side) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;

        this.setSide(side);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!active) return;
        int u = 0;
        int v = V_OFFSET * side;
        Graphics.renderGuiTexture(
                graphics,
                TEXTURE,
                getX(), getY(), u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
        if (!isHovered) return;
        graphics.setTooltipForNextFrame(textRenderer(), Component.translatable(TOOLTIP_KEY), mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.isMouseOver(click.x(), click.y())) {
            int randomNumber = randomNumber();

            setSide(randomNumber % SIDES);

            if (rprWidget.filteredRenames.isEmpty()) return true;

            int renameIndex = randomNumber % rprWidget.filteredRenames.size();

            rprWidget.openPage(renameIndex / RPRWidget.BUTTONS_ON_PAGE);
            rprWidget.doRename(rprWidget.filteredRenames.get(renameIndex));

            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public void setSide(int side) {
        this.side = side;
    }
}
