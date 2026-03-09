package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

import static com.hiword9.rprenames.mod.util.Util.*;

public class TabButton extends ClickableWidget implements OffsetableWidget {
    private static final Identifier TEXTURE = Identifier.of(RPRenames.MOD_ID, "textures/gui/tabs.png");
    private static final String TRANSLATION_PREFIX = "rprenames.gui.tabs.tooltip.";

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 33;
    public static final int BUTTON_HEIGHT = 26;
    static final int TEXTURE_WIDTH = 66;
    static final int TEXTURE_HEIGHT = 104;
    static final int SELECTED_OFFSET_U = 33;
    static final int TYPE_OFFSET_V = 26;

    final RPRWidget.Tab tab;
    private final int index;

    public TabButton(RPRWidget instance, int x, int y, RPRWidget.Tab tab) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;

        this.tab = tab;
        index = Arrays.stream(RPRWidget.Tab.values()).toList().indexOf(tab);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int u = rprWidget.getCurrentTab() == tab ? SELECTED_OFFSET_U : 0;
        int v = index * TYPE_OFFSET_V;
        Graphics.renderGuiTexture(
                context,
                TEXTURE,
                getX(), getY(), u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        if (isMouseOver(mouseX, mouseY)) {
            Graphics.drawTooltip(
                    context,
                    textRenderer(),
                    List.of(Graphics.tooltipOf(Text.translatable(TRANSLATION_PREFIX + tab.toString()))),
                    mouseX, mouseY,
                    HoveredTooltipPositioner.INSTANCE
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            if (rprWidget.getCurrentTab() != tab) rprWidget.openTab(tab);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
