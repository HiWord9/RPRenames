package com.HiWord9.RPRenames.mod.impl.rename.renderer;

import com.HiWord9.RPRenames.api.rename.renderer.RenameRenderer;
import com.HiWord9.RPRenames.api.rename.renderer.SimpleRenameRenderer;
import com.HiWord9.RPRenames.mod.gui.tooltip_component.preview.EntityPreviewTooltipComponent;
import com.HiWord9.RPRenames.mod.gui.Graphics;
import com.HiWord9.RPRenames.mod.gui.tooltip_component.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.mod.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.mod.impl.rename.CEMRename;
import com.HiWord9.RPRenames.mod.impl.rename.renderer.builder.AcceptsFavoriteSupplier;
import com.HiWord9.RPRenames.mod.impl.rename.renderer.builder.AcceptsRPRWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Supplier;

import static com.HiWord9.RPRenames.mod.util.RenameRendererHelper.*;
import static com.HiWord9.RPRenames.mod.util.Util.*;

public class CEMRenameRenderer extends SimpleRenameRenderer<CEMRename> implements Previewable {
    protected RPRWidget rprWidget;
    protected Supplier<Boolean> favoriteSupplier;

    protected LivingEntity entity;
    protected EntityPreviewTooltipComponent entityPreviewTooltipComponent;

    protected CEMRenameRenderer(CEMRename rename, RenderArea renderArea, RPRWidget rprWidget, Supplier<Boolean> favoriteSupplier) {
        super(rename, renderArea);
        this.rprWidget = rprWidget;
        this.favoriteSupplier = favoriteSupplier;

        var entityType = rename.getEntity();
        this.entity = (LivingEntity) entityType.create(client().world, null);
        prepareEntity(entity, rename);

        int size = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * config().scaleFactorEntity);
        int width = (int) (Graphics.DEFAULT_PREVIEW_WIDTH + size * entity.getWidth() - 1);
        int height = (int) (Graphics.DEFAULT_PREVIEW_HEIGHT + size * entity.getHeight() - 1);

        entityPreviewTooltipComponent = new EntityPreviewTooltipComponent(
                entity,
                width, height, size,
                config().spinMobPreview
        );
    }

    @Override
    protected void addTooltips() {
        super.addTooltips();

        if (!rprWidget.getCurrentTab().forCraftItemOnly) {
            MultiItemTooltipComponent component = multiItemTooltipComponent(rprWidget, rename);
            tooltipComponents.add(component);
        }

        tooltipComponents.add(mobNameTooltipComponent(rename.getEntity()));

        if (config().showPackName && rename.getPackName() != null) {
            tooltipComponents.add(packNameTooltipComponent(rename.getPackName()));
        }

        if (config().showNamePattern && rprWidget.getCurrentTab() != RPRWidget.Tab.FAVORITE) {
            TooltipComponent pattern = namePatternTooltipComponent(rename.getOriginalNamePattern());
            if (pattern != null) tooltipComponents.add(pattern);
        }
    }

    public static TooltipComponent mobNameTooltipComponent(EntityType<?> entityType) {
        return Graphics.tooltipOf(
                Text.translatable(entityType.getTranslationKey())
                        .fillStyle(Style.EMPTY.withColor(Formatting.YELLOW))
        );
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY) {
        Graphics.renderEntityInBox(context,
                new ScreenRect(
                        renderArea.getX() + 1,
                        renderArea.getY() + 1,
                        renderArea.getWidth() - 2,
                        renderArea.getHeight() - 2
                ),
                14 / (Math.max(entity.getHeight(), entity.getWidth())),
                entity,
                false,
                200
        );
    }

    @Override
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY) {
        super.onRenderTooltip(context, mouseX, mouseY);
        if (!config().enablePreview) return;
        drawPreview(
                context,
                mouseX, mouseY,
                tooltipComponents
        );
    }

    @Override
    public void drawPreview(DrawContext context, int mouseX, int mouseY, List<TooltipComponent> mainTooltip) {
        Graphics.drawTooltipWithFixedBorders(
                context,
                textRenderer(),
                entityPreviewTooltipComponent,
                mouseX, mouseY,
                new PreviewTooltipPositioner(config().previewPos, mainTooltip),
                favoriteSupplier.get()
        );
    }

    protected void prepareEntity(Entity entity, CEMRename rename) {
        if (entity == null) return;
        if (entity instanceof SnowGolemEntity snowGolem) {
            snowGolem.setHasPumpkin(!config().disableSnowGolemPumpkin);
        }
        entity.setCustomName(rename.getName());
    }

    public static class Builder extends RenameRenderer.Builder<CEMRename> implements AcceptsRPRWidget, AcceptsFavoriteSupplier {
        protected Supplier<Boolean> favoriteSupplier = () -> false;
        protected RPRWidget rprWidget = null;

        public Builder(CEMRename rename, RenderArea renderArea) {
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
        public CEMRenameRenderer build() {
            var renderer = new CEMRenameRenderer(rename, renderArea, rprWidget, favoriteSupplier);
            renderer.addTooltips();
            return renderer;
        }
    }
}
