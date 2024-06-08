package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.gui.Graphics;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TooltipBackgroundRenderer.class)
public abstract class TooltipBackgroundRendererMixin {
    private static final ModConfig config = ModConfig.INSTANCE;

    private static final int START_COLOR = ColorHelper.Argb.getArgb(160, 255, 217, 61);
    private static final int END_COLOR = ColorHelper.Argb.getArgb(160, 255, 169, 61);

    @Shadow
    private static void renderBorder(DrawContext context, int x, int y, int width, int height, int z, int startColor, int endColor) {}

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;renderBorder(Lnet/minecraft/client/gui/DrawContext;IIIIIII)V"),
            method = "render"
    )
    private static void onRenderBorder(DrawContext context, int x, int y, int width, int height, int z, int startColor, int endColor) {
        if (!config.recolorFavoriteTooltip || !Graphics.renderTooltipAsFavorite || startColor != 1347420415 || endColor != 1344798847) {
            renderBorder(context, x, y, width, height, z, startColor, endColor);
            return;
        }
        renderBorder(context, x, y, width, height, z, START_COLOR, END_COLOR);
        if (!config.renderStarInFavoriteTooltip) return;
        Graphics.renderStarInFavoriteTooltip(context, x, y, width);
    }
}
