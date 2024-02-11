package com.HiWord9.RPRenames.util.gui.button;

import com.HiWord9.RPRenames.AnvilScreenMixinAccessor;
import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.util.Tabs;
import com.HiWord9.RPRenames.util.gui.Graphics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
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

    public static final int buttonWidth = 33;
    public static final int buttonHeight = 26;
    static final int textureWidth = 66;
    static final int textureHeight = 104;
    static final int selectedOffsetU = 33;
    static final int typeOffsetV = 26;

    Tabs tab;
    private final int index;

    public TabButton(int x, int y, Tabs tab) {
        super(x, y, buttonWidth, buttonHeight, null);
        this.tab = tab;
        index = Arrays.stream(Tabs.values()).toList().indexOf(tab);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        AnvilScreen screen = ((AnvilScreen) MinecraftClient.getInstance().currentScreen);
        if (screen instanceof AnvilScreenMixinAccessor anvilScreenMixinAccessor) {
            Tabs currentTab = anvilScreenMixinAccessor.getCurrentTab();
            int u = currentTab == tab ? selectedOffsetU : 0;
            int v = index * typeOffsetV;
            context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), textureWidth, textureHeight);

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
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            AnvilScreen screen = ((AnvilScreen) MinecraftClient.getInstance().currentScreen);
            if (screen instanceof AnvilScreenMixinAccessor anvilScreenMixinAccessor) {
                anvilScreenMixinAccessor.setTab(tab);
                return true;
            }
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
