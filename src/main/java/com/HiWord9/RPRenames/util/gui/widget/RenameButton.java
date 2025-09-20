package com.HiWord9.RPRenames.util.gui.widget;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.RPRInteractableScreen;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.rename.renderer.RenameRenderer;
import com.HiWord9.RPRenames.util.rename.renderer.builder.AcceptsFavoriteSupplier;
import com.HiWord9.RPRenames.util.rename.renderer.builder.AcceptsRPRWidget;
import com.HiWord9.RPRenames.util.rename.type.Rename;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

import java.util.List;

public class RenameButton extends ClickableWidget {
    private static final ModConfig config = ModConfig.INSTANCE;
    final int highlightColor = config.getSlotHighlightRGBA();

    private static final Identifier TEXTURE = Identifier.of(RPRenames.MOD_ID, "textures/gui/button.png");

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 25;
    public static final int BUTTON_HEIGHT = 25;

    static final int TEXTURE_WIDTH = BUTTON_WIDTH * 2;
    static final int TEXTURE_HEIGHT = BUTTON_HEIGHT * 2;

    static final int FOCUSED_OFFSET_V = BUTTON_WIDTH;
    static final int FAVORITE_OFFSET_U = BUTTON_HEIGHT;

    public boolean selected = false;
    public boolean favorite;

    final public RenameRenderer renameRenderer;
    final public Rename rename;

    public RenameButton(
            RPRWidget instance,
            Rename rename,
            int x, int y,
            boolean favorite
    ) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;
        this.favorite = favorite;
        this.rename = rename;

        var builder = rename.getNewRendererBuilder();
        if (builder instanceof AcceptsRPRWidget b) b.setRPRWidget(rprWidget);
        if (builder instanceof AcceptsFavoriteSupplier b) b.setFavoriteSupplier(() -> this.favorite);
        renameRenderer = builder.build();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int u = favorite ? FAVORITE_OFFSET_U : 0;
        int v = hovered || (selected && config.highlightSelected) ? FOCUSED_OFFSET_V : 0;
        context.drawTexture(
                RenderLayer::getGuiTextured,
                TEXTURE,
                getX(), getY(),
                u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
        renameRenderer.onRender(
                context,
                mouseX, mouseY,
                getX(), getY(),
                getWidth() - 1,
                getHeight() - 1 // -1 cause of shadow
        );
    }

    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        if (!rprWidget.getCurrentTab().forCraftItemOnly
                && config.slotHighlightColorALPHA > 0
                && config.highlightSlot
        ) {
            highlightSlots(context, rprWidget.screen, highlightColor);
        }
        renameRenderer.onRenderTooltip(
                context,
                mouseX, mouseY,
                getX(), getY(),
                getWidth() - 1,
                getHeight() - 1
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        if (button == 1) {
            List<Item> items;

            if (rprWidget.getCurrentTab().forCraftItemOnly) items = List.of(rprWidget.getCraftItem());
            else items = List.copyOf(rename.getItems());

            rprWidget.addOrRemoveFavorite(!favorite, items, rename.getName());
        } else {
            rprWidget.doRename(rename);
        }

        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    private <H extends ScreenHandler> void highlightSlots(
            DrawContext context, RPRInteractableScreen screen, int highlightColor
    ) {
        if (!(screen instanceof HandledScreen<?> handledScreen)) return;
        var s = (HandledScreen<H> & RPRInteractableScreen) handledScreen;
        Graphics.highlightAvailableSlots(rename.getItems(), context, s, highlightColor);
    }
}
