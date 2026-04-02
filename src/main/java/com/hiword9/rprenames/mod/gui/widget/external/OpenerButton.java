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

public class OpenerButton extends AbstractWidget implements OffsetableWidget {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "textures/gui/opener.png");

    RPRWidget rprWidget;

    static final int BUTTON_WIDTH = 22;
    static final int BUTTON_HEIGHT = 22;

    static final int TEXTURE_WIDTH = 22;
    static final int TEXTURE_HEIGHT = 88;
    static final int FOCUSED_OFFSET_V = 22;
    static final int OPENED_OFFSET_V = 44;

    public OpenerButton(RPRWidget instance, int x, int y) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int u = 0;
        int v = 0;
        v += rprWidget.isOpen() ? OPENED_OFFSET_V : 0;
        v += isHovered ? FOCUSED_OFFSET_V : 0;
        Graphics.extractGuiTexture(
                graphics,
                TEXTURE,
                getX(), getY(), u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.isMouseOver(click.x(), click.y())) {
            execute();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}

    public void execute() {
        rprWidget.toggleOpen();
    }
}
