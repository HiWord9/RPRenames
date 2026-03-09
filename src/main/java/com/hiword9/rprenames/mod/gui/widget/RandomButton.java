package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.RPRenames;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.hiword9.rprenames.mod.util.Util.*;

public class RandomButton extends ClickableWidget implements OffsetableWidget {
    public static final Identifier TEXTURE = Identifier.of(RPRenames.MOD_ID, "textures/gui/dice.png");
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
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!active) return;
        int u = 0;
        int v = V_OFFSET * side;
        context.drawTexture(RenderLayer::getGuiTextured, TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        if (!hovered) return;
        context.drawTooltip(textRenderer(), Text.translatable(TOOLTIP_KEY), mouseX, mouseY);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            int randomNumber = randomNumber();

            setSide(randomNumber % SIDES);

            if (rprWidget.filteredRenames.isEmpty()) return true;

            int renameIndex = randomNumber % rprWidget.filteredRenames.size();

            rprWidget.openPage(renameIndex / RPRWidget.BUTTONS_ON_PAGE);
            rprWidget.doRename(rprWidget.filteredRenames.get(renameIndex));

            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void setSide(int side) {
        this.side = side;
    }
}
