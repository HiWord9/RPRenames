package com.HiWord9.RPRenames.util.gui.widget.button;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.Tab;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class TabButton extends ClickableWidget {
    private static final Identifier TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/tabs.png");
    private static final String TRANSLATION_PREFIX = "rprenames.gui.tabs.tooltip.";

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 33;
    public static final int BUTTON_HEIGHT = 26;
    static final int TEXTURE_WIDTH = 66;
    static final int TEXTURE_HEIGHT = 104;
    static final int SELECTED_OFFSET_U = 33;
    static final int TYPE_OFFSET_V = 26;

    final Tab tab;
    private final int index;

    public TabButton(RPRWidget instance, int x, int y, Tab tab) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;

        this.tab = tab;
        index = Arrays.stream(Tab.values()).toList().indexOf(tab);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Tab currentTab = rprWidget.getCurrentTab();
        int u = currentTab == tab ? SELECTED_OFFSET_U : 0;
        int v = index * TYPE_OFFSET_V;
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);

        if (isMouseOver(mouseX, mouseY)) {
            Graphics.drawTooltip(
                    context,
                    MinecraftClient.getInstance().textRenderer,
                    List.of(TooltipComponent.of(Text.translatable(TRANSLATION_PREFIX + tab.toString()).asOrderedText())),
                    mouseX, mouseY,
                    HoveredTooltipPositioner.INSTANCE
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            rprWidget.setTab(tab);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
