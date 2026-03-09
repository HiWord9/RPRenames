package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.RPRInteractableScreen;
import com.hiword9.rprenames.mod.gui.Graphics;
import com.hiword9.rprenames.api.core.rename.renderer.RenameRenderer;
import com.hiword9.rprenames.mod.impl.rename.renderer.builder.AcceptsFavoriteSupplier;
import com.hiword9.rprenames.mod.impl.rename.renderer.builder.AcceptsRPRWidget;
import com.hiword9.rprenames.api.core.rename.Rename;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

import java.util.List;

import static com.hiword9.rprenames.mod.util.Util.*;

public class RenameButton extends ClickableWidget implements OffsetableWidget {
    final int highlightColor = config().getSlotHighlightRGBA();

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

        var builder = rename.getNewRendererBuilder(new RenameRenderer.RenderArea() {
            public int getX() { return RenameButton.this.getX(); }
            public int getY() { return RenameButton.this.getY(); }
            public int getWidth() { return RenameButton.this.getWidth() - 1; }
            public int getHeight() { return RenameButton.this.getHeight() - 1; }
        });
        if (builder instanceof AcceptsRPRWidget b) b.setRPRWidget(rprWidget);
        if (builder instanceof AcceptsFavoriteSupplier b) b.setFavoriteSupplier(() -> this.favorite);
        renameRenderer = builder.build();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int u = favorite ? FAVORITE_OFFSET_U : 0;
        int v = hovered || (selected && config().highlightSelected) ? FOCUSED_OFFSET_V : 0;
        context.drawTexture(
                RenderLayer::getGuiTextured,
                TEXTURE,
                getX(), getY(),
                u, v,
                getWidth(), getHeight(),
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
        renameRenderer.onRender(context, mouseX, mouseY);
    }

    public void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        if (!rprWidget.getCurrentTab().forCraftItemOnly
                && config().slotHighlightColorALPHA > 0
                && config().highlightSlot
        ) {
            highlightSlots(context, rprWidget.screen, highlightColor);
        }
        renameRenderer.onRenderTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) return false;

        if (button == 1) {
            List<Item> items;

            if (rprWidget.getCurrentTab().forCraftItemOnly) items = List.of(rprWidget.getCraftItem());
            else items = List.copyOf(rename.getItems());

            rprWidget.addOrRemoveFavorite(!favorite, items, rename.getName().getString());
        } else {
            rprWidget.doRename(rename);
        }

        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        renameRenderer.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (renameRenderer.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (renameRenderer.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (renameRenderer.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (renameRenderer.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
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
