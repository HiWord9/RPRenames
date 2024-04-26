package com.HiWord9.RPRenames.util.gui.button;

import com.HiWord9.RPRenames.AnvilScreenMixinAccessor;
import com.HiWord9.RPRenames.RPRenames;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Identifier;

public class FavoriteButton extends ClickableWidget {
    public static final Identifier TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/favorite_button.png");

    public static final int BUTTON_WIDTH = 9;
    public static final int BUTTON_HEIGHT = 9;

    public static final int TEXTURE_WIDTH = 9;
    public static final int TEXTURE_HEIGHT = 18;
    static final int V_OFFSET = BUTTON_HEIGHT;

    boolean favorite = false;

    public FavoriteButton(int x, int y) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
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
            AnvilScreen screen = ((AnvilScreen) MinecraftClient.getInstance().currentScreen);
            if (screen instanceof AnvilScreenMixinAccessor anvilScreenMixinAccessor) {
                anvilScreenMixinAccessor.addOrRemoveFavorite(!favorite);
                return true;
            }
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
