package com.HiWord9.RPRenames.util.rename.renderer;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.gui.tooltipcomponent.preview.EntityPreviewTooltipComponent;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.tooltipcomponent.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.type.CEMRename;
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

import java.util.ArrayList;

public class CEMRenameRenderer extends DefaultRenameRenderer<CEMRename> implements RenameRenderer.Preview {
    private static final ModConfig config = ModConfig.INSTANCE;

    RPRWidget rprWidget;
    boolean favorite;

    LivingEntity entity;
    EntityPreviewTooltipComponent entityPreviewTooltipComponent;

    public CEMRenameRenderer(CEMRename rename, RPRWidget rprWidget, boolean favorite) {
        super(rename);
        this.rprWidget = rprWidget;
        this.favorite = favorite;

        var entityType = rename.getMob().getEntity();
        var client = MinecraftClient.getInstance();
        this.entity = (LivingEntity) entityType.create(client.world);
        prepareEntity(entity, rename);

        int size = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * config.scaleFactorEntity);
        int width = (int) (Graphics.DEFAULT_PREVIEW_WIDTH + size * entity.getWidth() - 1);
        int height = (int) (Graphics.DEFAULT_PREVIEW_HEIGHT + size * entity.getHeight() - 1);

        entityPreviewTooltipComponent = new EntityPreviewTooltipComponent(
                entity,
                width, height, size,
                config.spinMobPreview
        );

        int index = 1;

        if (rprWidget.getCurrentTab() == RPRWidget.Tab.INVENTORY || rprWidget.getCurrentTab() == RPRWidget.Tab.GLOBAL) {
            MultiItemTooltipComponent component = multiItemTooltipComponent(rprWidget, rename);
            tooltipComponents.add(index++, component);
        }

        tooltipComponents.add(index++, mobNameTooltipComponent(entityType));

        if (!config.showPackName && getDisplayPackName() != null) {
            tooltipComponents.remove(index);
        }
        if (config.showNamePattern && rprWidget.getCurrentTab() != RPRWidget.Tab.FAVORITE) {
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

    public String getDisplayPackName() {
        return rename.getMob().getPackName();
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        Graphics.renderEntityInBox(context,
                new ScreenRect(buttonX + 1, buttonY + 1, buttonWidth - 2, buttonHeight - 2),
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
    public void drawPreview(DrawContext context, int mouseX, int mouseY, ArrayList<TooltipComponent> mainTooltip) {
        Graphics.drawTooltipWithFixedBorders(
                context,
                MinecraftClient.getInstance().textRenderer,
                entityPreviewTooltipComponent,
                mouseX, mouseY,
                new PreviewTooltipPositioner(mainTooltip),
                favorite
        );
    }

    private void prepareEntity(Entity entity, CEMRename rename) {
        if (entity == null) return;
        if (entity instanceof SnowGolemEntity) {
            ((SnowGolemEntity) entity).setHasPumpkin(!config.disableSnowGolemPumpkin);
        }
        entity.setCustomName(Text.of(rename.getName()));
    }
}
