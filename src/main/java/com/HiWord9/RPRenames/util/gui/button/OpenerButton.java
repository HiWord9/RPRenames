package com.HiWord9.RPRenames.util.gui.button;

import com.HiWord9.RPRenames.AnvilScreenMixinAccessor;
import com.HiWord9.RPRenames.RPRenames;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Identifier;

public class OpenerButton extends ClickableWidget {
    private static final Identifier TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/opener.png");

    static final int buttonWidth = 22;
    static final int buttonHeight = 22;

    static final int textureWidth = 22;
    static final int textureHeight = 88;
    static final int focusedOffsetV = 22;
    static final int openedOffsetV = 44;

    boolean open = false;

    public OpenerButton(int x, int y) {
        super(x, y, buttonWidth, buttonHeight, null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean focused = isMouseOver(mouseX, mouseY);
        int u = 0;
        int v = 0;
        v += open ? openedOffsetV : 0;
        v += focused ? focusedOffsetV : 0;
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), textureWidth, textureHeight);
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            AnvilScreen screen = ((AnvilScreen) MinecraftClient.getInstance().currentScreen);
            if (screen instanceof AnvilScreenMixinAccessor anvilScreenMixinAccessor) {
                anvilScreenMixinAccessor.switchOpen();
                return true;
            }
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
