package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.gui.Graphics;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TooltipBackgroundRenderer.class)
public abstract class TooltipBackgroundRendererMixin {
    private static final ModConfig config = ModConfig.INSTANCE;

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"
            ),
            method = "render"
    )
    private static void onRender(DrawContext context, int x, int y, int width, int height, int z, Identifier texture, CallbackInfo ci) {
        if (!Graphics.renderTooltipAsFavorite || !config.renderStarInFavoriteTooltip) return;
        Graphics.renderStarInFavoriteTooltip(context, x, y, width, z);
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/tooltip/TooltipBackgroundRenderer;getFrameTexture(Lnet/minecraft/util/Identifier;)Lnet/minecraft/util/Identifier;"
            ),
            method = "render"
    )
    private static @Nullable Identifier onGetFrameTexture(@Nullable Identifier texture) {
        if (!Graphics.renderTooltipAsFavorite || texture != null || !config.recolorFavoriteTooltip) return texture;
        return Graphics.FAVORITE_TOOLTIP_FRAME_TEXTURE;
    }
}
