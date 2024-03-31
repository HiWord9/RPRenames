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

    static final int BUTTON_WIDTH = 22;
    static final int BUTTON_HEIGHT = 22;

    static final int TEXTURE_WIDTH = 22;
    static final int TEXTURE_HEIGHT = 88;
    static final int FOCUSED_OFFSET_V = 22;
    static final int OPENED_OFFSET_V = 44;

    boolean open = false;

    public OpenerButton(int x, int y) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int u = 0;
        int v = 0;
        v += open ? OPENED_OFFSET_V : 0;
        v += hovered ? FOCUSED_OFFSET_V : 0;
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
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
