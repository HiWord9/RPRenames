package com.hiword9.rprenames.mod.gui.widget.external;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.Graphics;
import com.hiword9.rprenames.mod.gui.widget.OffsetableWidget;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import java.util.List;

public class FavoriteButton extends AbstractWidget implements OffsetableWidget {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "textures/gui/favorite_button.png");

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 9;
    public static final int BUTTON_HEIGHT = 9;

    public static final int TEXTURE_WIDTH = 9;
    public static final int TEXTURE_HEIGHT = 18;
    static final int V_OFFSET = BUTTON_HEIGHT;

    public boolean favorite = false;

    public FavoriteButton(RPRWidget instance, int x, int y, Position offset) {
        this(instance, x + offset.getX(), y + offset.getY());
    }

    public FavoriteButton(RPRWidget instance, int x, int y) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (!active) return;

        int u = 0;
        int v = favorite ? 0 : V_OFFSET;
        Graphics.renderGuiTexture(
                context,
                TEXTURE,
                getX(), getY(),
                u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.isMouseOver(click.x(), click.y())) {
            var item = rprWidget.getCraftItem();
            if (item == Items.AIR) return true;

            rprWidget.addOrRemoveFavorite(!favorite, List.of(item), rprWidget.getNameText());
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public enum Position {
        TOP_RIGHT(159, 8),
        LEFT_FROM_NAMEFIELD(47, 23),
        RIGHT_FROM_RESULT_SLOT(156, 50);

        final int x;
        final int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
