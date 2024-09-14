package com.HiWord9.RPRenames.util.gui.widget.button.external;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Identifier;

public class FavoriteButton extends ClickableWidget {
    public static final Identifier TEXTURE = Identifier.of(RPRenames.MOD_ID, "textures/gui/favorite_button.png");

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 9;
    public static final int BUTTON_HEIGHT = 9;

    public static final int TEXTURE_WIDTH = 9;
    public static final int TEXTURE_HEIGHT = 18;
    static final int V_OFFSET = BUTTON_HEIGHT;

    boolean favorite = false;

    public FavoriteButton(RPRWidget instance, int x, int y, Position offset) {
        this(instance, x + offset.getX(), y + offset.getY());
    }

    public FavoriteButton(RPRWidget instance, int globalX, int globalY) {
        super(globalX, globalY, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!active) return;
        int u = 0;
        int v = favorite ? 0 : V_OFFSET;
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            rprWidget.addOrRemoveFavorite(!favorite);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
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
