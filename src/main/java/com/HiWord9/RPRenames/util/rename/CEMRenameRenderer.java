package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.Tab;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import net.minecraft.client.MinecraftClient;
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
import org.joml.Vector2ic;

import java.util.ArrayList;

public class CEMRenameRenderer extends DefaultRenameRenderer implements RenameRenderer.Preview {
    private static final ModConfig config = ModConfig.INSTANCE;

    CEMRename rename;
    RPRWidget rprWidget;
    boolean favorite;

    LivingEntity entity;

    CEMRenameRenderer(CEMRename rename, RPRWidget rprWidget, boolean favorite) {
        super(rename);
        this.rename = rename;
        this.rprWidget = rprWidget;
        this.favorite = favorite;

        var entityType = rename.getMob().entity();
        var client = MinecraftClient.getInstance();
        this.entity = (LivingEntity) entityType.create(client.world);
        prepareEntity(entity, rename);

        int index = 1;

        if (rprWidget.getCurrentTab() == Tab.INVENTORY || rprWidget.getCurrentTab() == Tab.GLOBAL) {
            MultiItemTooltipComponent component = multiItemTooltipComponent(rprWidget, rename);
            tooltipComponents.add(index++, component);
        }

        tooltipComponents.add(index++, mobNameTooltipComponent(entityType));

        if (!config.showPackName && rename.getPackName() != null) {
            tooltipComponents.remove(index);
        }
        if (config.showNamePattern && rprWidget.getCurrentTab() != Tab.FAVORITE) {
            TooltipComponent pattern = namePatternTooltipComponent(rename);
            if (pattern != null) tooltipComponents.add(pattern);
        }
    }

    public static TooltipComponent mobNameTooltipComponent(EntityType<?> entityType) {
        return TooltipComponent.of(
                Text.translatable(entityType.getTranslationKey())
                        .copy().fillStyle(Style.EMPTY.withColor(Formatting.YELLOW))
                        .asOrderedText());
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        Graphics.renderEntityInBox(context,
                new ScreenRect(buttonX, buttonY, buttonWidth, buttonHeight), 1,
                14 / (Math.max(entity.getHeight(), entity.getWidth())), entity, false, 200);
    }

    @Override
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        super.onRenderTooltip(context, mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
        if (!config.enablePreview) return;
        drawPreview(
                context,
                mouseX, mouseY,
                tooltipComponents
        );
    }

    @Override
    public void drawPreview(DrawContext context, int mouseX, int mouseY, ArrayList<TooltipComponent> tooltipComponents) {
        int size = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * config.scaleFactorEntity);
        int width = Graphics.DEFAULT_PREVIEW_WIDTH;
        int height = Graphics.DEFAULT_PREVIEW_HEIGHT;
        int newWidth = (int) (width + size * entity.getWidth() - 1);
        int newHeight = (int) (height + size * entity.getHeight() - 1);

        var screen = rprWidget.getScreen();
        Vector2ic vector2ic = new PreviewTooltipPositioner(tooltipComponents)
                .getPosition(
                        screen.width, screen.height,
                        mouseX, mouseY,
                        newWidth, newHeight
                );
        int x = vector2ic.x();
        int y = vector2ic.y();
        entityPreview(context, x, y, newWidth, newHeight, size, config.spinMobPreview, entity);
    }

    private void entityPreview(DrawContext context, int x, int y, int width, int height, int size, boolean spin, LivingEntity entity) {
        Graphics.drawTooltipBackground(context, x, y, width, height, favorite);
        Graphics.renderEntityInBox(context,
                new ScreenRect(x, y, width, height), Graphics.TOOLTIP_CORNER,
                size, entity, spin);
    }

    private void prepareEntity(Entity entity, CEMRename rename) {
        if (entity == null) return;
        if (entity instanceof SnowGolemEntity) {
            ((SnowGolemEntity) entity).setHasPumpkin(!config.disableSnowGolemPumpkin);
        }
        entity.setCustomName(Text.of(rename.getName()));
    }
}
