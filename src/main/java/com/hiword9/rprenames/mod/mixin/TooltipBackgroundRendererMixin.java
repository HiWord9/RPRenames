package com.hiword9.rprenames.mod.mixin;

import com.hiword9.rprenames.mod.gui.Graphics;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.hiword9.rprenames.mod.util.Util.config;

@Mixin(value = TooltipRenderUtil.class)
public abstract class TooltipBackgroundRendererMixin {
    @Inject(
            at = @At(value = "TAIL"),
            method = "extractTooltipBackground"
    )
    private static void onRender(GuiGraphicsExtractor graphics, int x, int y, int width, int height, Identifier texture, CallbackInfo ci) {
        if (!Graphics.renderTooltipAsFavorite || !config().renderStarInFavoriteTooltip) return;
        Graphics.renderStarInFavoriteTooltip(graphics, x, y, width);
    }

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;getFrameSprite(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/resources/Identifier;"
            ),
            method = "extractTooltipBackground"
    )
    private static @Nullable Identifier onGetFrameTexture(@Nullable Identifier texture) {
        if (!Graphics.renderTooltipAsFavorite || texture != null || !config().recolorFavoriteTooltip) return texture;
        return Graphics.FAVORITE_TOOLTIP_FRAME_TEXTURE;
    }
}
