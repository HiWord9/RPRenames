package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.Arrays;
import java.util.List;

import static com.hiword9.rprenames.mod.util.Util.*;

public class TabButton extends AbstractWidget implements OffsetableWidget {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "textures/gui/tabs.png");
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        int u = rprWidget.getCurrentTab() == tab ? SELECTED_OFFSET_U : 0;
        int v = index * TYPE_OFFSET_V;
        Graphics.renderGuiTexture(
                graphics,
                TEXTURE,
                getX(), getY(), u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        if (isMouseOver(mouseX, mouseY)) {
            Graphics.drawTooltip(
                    graphics,
                    font(),
                    List.of(Graphics.tooltipOf(Component.translatable(TRANSLATION_PREFIX + tab.toString()))),
                    mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE
            );
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.isMouseOver(click.x(), click.y())) {
            if (rprWidget.getCurrentTab() != tab) rprWidget.openTab(tab);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
