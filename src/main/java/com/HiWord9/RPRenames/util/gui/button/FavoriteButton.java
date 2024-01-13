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
    private static final Identifier TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/favorite_button.png");

    static final int buttonWidth = 9;
    static final int buttonHeight = 9;

    static final int textureWidth = 9;
    static final int textureHeight = 18;
    static final int vOffset = buttonHeight;

    boolean favorite = false;

    public FavoriteButton(int x, int y) {
        super(x, y, buttonWidth, buttonHeight, null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!active) return;
        int u = 0;
        int v = favorite ? 0 : vOffset;
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), textureWidth, textureHeight);
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

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
