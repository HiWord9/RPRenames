package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.util.Graphics;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import static com.hiword9.rprenames.util.Util.*;

public class PageButton extends AbstractWidget implements OffsetableWidget {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "textures/gui/page_arrows.png");

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
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int u = type == Type.DOWN ? 0 : UP_OFFSET_U;
        int v = !active ? DISABLED_OFFSET_V : isHovered ? FOCUSED_OFFSET_V : 0;
        Graphics.extractGuiTexture(
                graphics,
                TEXTURE,
                getX(), getY(), u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
        if (!config().disablePageArrowsHints && hasShiftDown() && active && isHovered) {
            String key = "rprenames.gui.page" + (type == Type.DOWN ? "Down.toFirst" : "Up.toLast") + ".tooltip";
            graphics.setTooltipForNextFrame(font(), Component.translatable(key).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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

    protected void updateWidgetNarration(NarrationElementOutput builder) {}

    public enum Type {
        DOWN,
        UP
    }
}
