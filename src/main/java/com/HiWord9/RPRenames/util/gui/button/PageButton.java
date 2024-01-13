package com.HiWord9.RPRenames.util.gui.button;

import com.HiWord9.RPRenames.AnvilScreenMixinAccessor;
import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class PageButton extends ClickableWidget {
    private static final ModConfig config = ModConfig.INSTANCE;

    private static final Identifier TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/page_arrows.png");

    public static final int buttonWidth = 30;
    static final int buttonHeight = 16;

    static final int textureWidth = 60;
    static final int textureHeight = 48;
    static final int upOffsetU = 30;
    static final int disabledOffsetV = 32;
    static final int focusedOffsetV = 16;

    Type type;

    public PageButton(int x, int y, Type type) {
        super(x, y, buttonWidth, buttonHeight, null);
        this.type = type;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean focused = isMouseOver(mouseX, mouseY);
        int u = type == Type.DOWN ? 0 : upOffsetU;
        int v = !active ? disabledOffsetV : focused ? focusedOffsetV : 0;
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), textureWidth, textureHeight);
        MinecraftClient client = MinecraftClient.getInstance();
        if (!config.disablePageArrowsTips && hasShiftDown() && active && focused) {
            String key = "rprenames.gui.page" + (type == Type.DOWN ? "Down.toFirst" : "Up.toLast") + ".tooltip";
            context.drawTooltip(client.textRenderer, Text.translatable(key).copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)), mouseX, mouseY);
        }
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            AnvilScreen screen = ((AnvilScreen) MinecraftClient.getInstance().currentScreen);
            if (screen instanceof AnvilScreenMixinAccessor anvilScreenMixinAccessor) {
                if (type == Type.DOWN) {
                    anvilScreenMixinAccessor.onPageDown();
                } else {
                    anvilScreenMixinAccessor.onPageUp();
                }
                return true;
            }
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public enum Type {
        DOWN,
        UP
    }
}
