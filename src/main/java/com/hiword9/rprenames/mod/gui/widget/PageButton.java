package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static com.hiword9.rprenames.mod.util.Util.*;

public class PageButton extends ClickableWidget implements OffsetableWidget {
    private static final Identifier TEXTURE = Identifier.of(RPRenames.MOD_ID, "textures/gui/page_arrows.png");

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 30;
    static final int BUTTON_HEIGHT = 16;

    static final int TEXTURE_WIDTH = 60;
    static final int TEXTURE_HEIGHT = 48;
    static final int UP_OFFSET_U = 30;
    static final int DISABLED_OFFSET_V = 32;
    static final int FOCUSED_OFFSET_V = 16;

    final Type type;

    public PageButton(RPRWidget instance, int x, int y, Type type) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;

        this.type = type;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int u = type == Type.DOWN ? 0 : UP_OFFSET_U;
        int v = !active ? DISABLED_OFFSET_V : hovered ? FOCUSED_OFFSET_V : 0;
        Graphics.renderGuiTexture(
                context,
                TEXTURE,
                getX(), getY(), u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
        if (!config().disablePageArrowsHints && hasShiftDown() && active && hovered) {
            String key = "rprenames.gui.page" + (type == Type.DOWN ? "Down.toFirst" : "Up.toLast") + ".tooltip";
            context.drawTooltip(textRenderer(), Text.translatable(key).formatted(Formatting.GRAY), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (this.isMouseOver(click.x(), click.y())) {
            if (type == Type.DOWN) {
                rprWidget.prevPage();
            } else {
                rprWidget.nextPage();
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public enum Type {
        DOWN,
        UP
    }
}
