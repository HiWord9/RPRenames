package com.hiword9.rprenames.mod.impl.rename.renderer;

import com.hiword9.rprenames.api.core.rename.renderer.RenameRenderer;
import com.hiword9.rprenames.api.core.rename.renderer.SimpleRenameRenderer;
import com.hiword9.rprenames.api.ext.rename.renderer.PreviewTooltipPositioner;
import com.hiword9.rprenames.api.ext.rename.renderer.Previewable;
import com.hiword9.rprenames.mod.gui.tooltip_component.preview.EntityPreviewTooltipComponent;
import com.hiword9.rprenames.mod.gui.Graphics;
import com.hiword9.rprenames.mod.gui.tooltip_component.MultiItemTooltipComponent;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget;
import com.hiword9.rprenames.mod.impl.rename.CEMRename;
import com.hiword9.rprenames.mod.impl.rename.renderer.builder.AcceptsFavoriteSupplier;
import com.hiword9.rprenames.mod.impl.rename.renderer.builder.AcceptsRPRWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import java.util.List;
import java.util.function.Supplier;

import static com.hiword9.rprenames.mod.util.RenameRendererHelper.*;
import static com.hiword9.rprenames.mod.util.Util.*;

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
        this.entity = (LivingEntity) entityType.create(client().level, null);
        prepareEntity(entity, rename);

        int size = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * config().scaleFactorEntity);
        int width = (int) (Graphics.DEFAULT_PREVIEW_WIDTH + size * entity.getBbWidth() - 1);
        int height = (int) (Graphics.DEFAULT_PREVIEW_HEIGHT + size * entity.getBbHeight() - 1);

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
            ClientTooltipComponent pattern = namePatternTooltipComponent(rename.getOriginalNamePattern());
            if (pattern != null) tooltipComponents.add(pattern);
        }
    }

    public static ClientTooltipComponent mobNameTooltipComponent(EntityType<?> entityType) {
        return Graphics.tooltipOf(
                Component.translatable(entityType.getDescriptionId())
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
        );
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Graphics.renderEntityInBox(graphics,
                new ScreenRectangle(
                        renderArea.getX() + 1,
                        renderArea.getY() + 1,
                        renderArea.getWidth() - 2,
                        renderArea.getHeight() - 2
                ),
                14 / (Math.max(entity.getBbHeight(), entity.getBbWidth())),
                entity,
                false
        );
    }

    @Override
    public void onRenderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.onRenderTooltip(graphics, mouseX, mouseY);
        if (!config().enablePreview) return;
        drawPreview(
                graphics,
                mouseX, mouseY,
                tooltipComponents
        );
    }

    @Override
    public void drawPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY, List<ClientTooltipComponent> mainTooltip) {
        Graphics.drawTooltipWithFixedBorders(
                graphics,
                font(),
                entityPreviewTooltipComponent,
                mouseX, mouseY,
                new PreviewTooltipPositioner(config().previewPos, mainTooltip),
                favoriteSupplier.get()
        );
    }

    protected void prepareEntity(Entity entity, CEMRename rename) {
        if (entity == null) return;
        if (entity instanceof SnowGolem snowGolem) {
            snowGolem.setPumpkin(!config().disableSnowGolemPumpkin);
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
