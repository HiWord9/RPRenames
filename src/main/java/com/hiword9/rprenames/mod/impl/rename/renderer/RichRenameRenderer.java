package com.hiword9.rprenames.mod.impl.rename.renderer;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.core.rename.renderer.RenameRenderer;
import com.hiword9.rprenames.api.core.rename.renderer.SimpleRenameRenderer;
import com.hiword9.rprenames.api.ext.rename.renderer.PreviewTooltipPositioner;
import com.hiword9.rprenames.api.ext.rename.renderer.Previewable;
import com.hiword9.rprenames.mod.gui.Graphics;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.ItemPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.PlayerPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget;
import com.hiword9.rprenames.api.ext.rename.HasResourcePack;
import com.hiword9.rprenames.api.ext.rename.renderer.PreviewTooltipPositioner.PreviewPos;
import com.hiword9.rprenames.api.ext.rename.renderer.builder.AcceptsFavoriteSupplier;
import com.hiword9.rprenames.api.ext.rename.renderer.builder.AcceptsRPRWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Supplier;

import static com.hiword9.rprenames.mod.util.RenameRendererHelper.multiItemTooltipComponent;
import static com.hiword9.rprenames.mod.util.RenameRendererHelper.packNameTooltipComponent;
import static com.hiword9.rprenames.mod.util.Util.player;
import static com.hiword9.rprenames.mod.util.Util.textRenderer;
import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class RichRenameRenderer<R extends Rename>
        extends SimpleRenameRenderer<R>
        implements Previewable
{
    protected RPRWidget rprWidget;
    protected Supplier<Boolean> favoriteSupplier;

    protected ItemPreviewTooltipComponent itemPreviewTooltipComponent;
    protected PlayerPreviewTooltipComponent playerPreviewTooltipComponent;

    protected int cycleSlotsGlfwKey = GLFW.GLFW_KEY_F;

    protected RichRenameRenderer(R rename, RenderArea renderArea, RPRWidget rprWidget, Supplier<Boolean> favoriteSupplier) {
        super(rename, renderArea);
        this.rprWidget = rprWidget;
        this.favoriteSupplier = favoriteSupplier;
        itemPreviewTooltipComponent = getItemPreviewTooltip();
        playerPreviewTooltipComponent = getPlayerPreviewTooltip();
    }

    protected ItemPreviewTooltipComponent getItemPreviewTooltip() {
        final int width = Graphics.DEFAULT_PREVIEW_WIDTH;
        final int height = Graphics.DEFAULT_PREVIEW_HEIGHT;
        return new ItemPreviewTooltipComponent(
                stack,
                width / 2,
                height / 2,
                Graphics.DEFAULT_PREVIEW_SIZE_ITEM
        );
    }

    protected PlayerPreviewTooltipComponent getPlayerPreviewTooltip() {
        final int width = Graphics.DEFAULT_PREVIEW_WIDTH;
        final int height = Graphics.DEFAULT_PREVIEW_HEIGHT;
        final int playerSize = Graphics.DEFAULT_PREVIEW_SIZE_ENTITY;
        return new PlayerPreviewTooltipComponent(
                player(), stack,
                (int) (width + playerSize * player().getWidth() - 1),
                (int) (height + playerSize * player().getHeight() - 1),
                playerSize,
                false,
                false
        );
    }

    @Override
    protected void addTooltips() {
        super.addTooltips();
        addTopTooltips();
        addMiddleTooltips();
        addBottomTooltips();
    }

    protected void addTopTooltips() {
        addMultiItemTooltip();
    }

    protected void addMiddleTooltips() {}

    protected void addBottomTooltips() {
        addPackNameTooltip();
    }

    protected void addMultiItemTooltip() {
        if (rprWidget.getCurrentTab().forCraftItemOnly) return;
        tooltipComponents.add(multiItemTooltipComponent(rprWidget, rename));
    }

    protected void addPackNameTooltip() {
        if (!(rename instanceof HasResourcePack rpRename)) return;

        if (rpRename.getPackName() == null) return;
        tooltipComponents.add(packNameTooltipComponent(rpRename.getPackName()));
    }

    @Override
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY) {
        super.onRenderTooltip(context, mouseX, mouseY);
        drawPreview(context, mouseX, mouseY, tooltipComponents);
    }

    @Override
    public void drawPreview(DrawContext context, int mouseX, int mouseY, List<TooltipComponent> mainTooltip) {
        var positioner = new PreviewTooltipPositioner(getPreviewPositionerPos(), mainTooltip);

        if (shouldPreviewPlayer()) {
            playerPreview(context, mouseX, mouseY, positioner);
        } else itemPreview(context, mouseX, mouseY, positioner);
    }

    protected boolean shouldPreviewPlayer() {
        return hasShiftDown();
    }

    protected PreviewPos getPreviewPositionerPos() {
        return PreviewPos.LEFT;
    }

    protected void playerPreview(DrawContext context, int mouseX, int mouseY, TooltipPositioner positioner) {
        Graphics.drawTooltipWithFixedBorders(
                context,
                textRenderer(),
                playerPreviewTooltipComponent,
                mouseX, mouseY,
                positioner,
                favoriteSupplier.get()
        );
    }

    protected void itemPreview(DrawContext context, int mouseX, int mouseY, TooltipPositioner positioner) {
        Graphics.drawTooltipWithFixedBorders(
                context,
                textRenderer(),
                itemPreviewTooltipComponent,
                mouseX, mouseY,
                positioner,
                favoriteSupplier.get()
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == cycleSlotsGlfwKey && isFocused()) {
            playerPreviewTooltipComponent.cycleSlots();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static class Builder<R extends Rename>
            extends RenameRenderer.Builder<R>
            implements AcceptsRPRWidget, AcceptsFavoriteSupplier
    {
        protected Supplier<Boolean> favoriteSupplier = () -> false;
        protected RPRWidget rprWidget = null;

        public Builder(R rename, RenderArea renderArea) {
            super(rename, renderArea);
        }

        @Override
        public void setFavoriteSupplier(Supplier<Boolean> favoriteSupplier) {
            this.favoriteSupplier = favoriteSupplier;
        }

        @Override
        public void setRPRWidget(RPRWidget rprWidget) {
            this.rprWidget = rprWidget;
        }

        @Override
        public RichRenameRenderer<R> build() {
            var renderer = getNewRenderer();
            renderer.addTooltips();
            return renderer;
        }

        protected RichRenameRenderer<R> getNewRenderer() {
            return new RichRenameRenderer<>(rename, renderArea, rprWidget, favoriteSupplier);
        }
    }
}
