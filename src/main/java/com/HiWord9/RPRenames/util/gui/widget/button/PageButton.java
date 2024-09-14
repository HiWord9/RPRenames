package com.HiWord9.RPRenames.util.gui.widget.button;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class PageButton extends ClickableWidget {
    private static final ModConfig config = ModConfig.INSTANCE;

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
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MinecraftClient client = MinecraftClient.getInstance();
        if (!config.disablePageArrowsHints && hasShiftDown() && active && hovered) {
            String key = "rprenames.gui.page" + (type == Type.DOWN ? "Down.toFirst" : "Up.toLast") + ".tooltip";
            context.drawTooltip(client.textRenderer, Text.translatable(key).formatted(Formatting.GRAY), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            if (type == Type.DOWN) {
                rprWidget.prevPage();
            } else {
                rprWidget.nextPage();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public enum Type {
        DOWN,
        UP
    }
}
