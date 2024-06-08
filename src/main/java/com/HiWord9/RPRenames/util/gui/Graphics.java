package com.HiWord9.RPRenames.util.gui;

import com.HiWord9.RPRenames.util.gui.widget.button.external.FavoriteButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.joml.Quaternionf;

import java.util.List;

public class Graphics {
    static public final int DEFAULT_PREVIEW_WIDTH = 50;
    static public final int DEFAULT_PREVIEW_HEIGHT = 50;
    static public final int DEFAULT_PREVIEW_SIZE_ENTITY = 32;
    static public final int DEFAULT_PREVIEW_SIZE_ITEM = 16;

    static public final int SLOT_SIZE = 18;
    static public final int STACK_IN_SLOT_SIZE = 16;

    public static final int HIGHLIGHT_COLOR_WRONG = 822018048;
    public static final int HIGHLIGHT_COLOR_SECOND = 822018303;

    static public final int DEFAULT_TEXT_COLOR = 0xffffff;

    static public boolean renderTooltipAsFavorite = false;

    public static void renderText(DrawContext context, Text text, int x, int y, boolean shadow, boolean centered) {
        renderText(context, text, DEFAULT_TEXT_COLOR, x, y, shadow, centered);
    }

    public static void renderText(DrawContext context, Text text, int color, int x, int y, boolean shadow, boolean centered) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer renderer = client.textRenderer;
        int xOffset = 0;
        if (centered) {
            xOffset = renderer.getWidth(text) / 2;
        }
        context.drawText(renderer, text, x - xOffset, y, color, shadow);
    }

    public static void renderStack(DrawContext context, ItemStack itemStack, int x, int y) {
        renderStack(context, itemStack, x, y, 0, STACK_IN_SLOT_SIZE);
    }

    public static void renderStack(DrawContext context, ItemStack itemStack, int x, int y, int z, int size) {
        float scale = size != STACK_IN_SLOT_SIZE ? ((float) size / STACK_IN_SLOT_SIZE) : 1f;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, z);
        matrices.scale(scale, scale, 1);
        context.drawItemWithoutEntity(itemStack, 0, 0);
        matrices.pop();
    }

    public static void renderEntityInBox(DrawContext context, ScreenRect rect, int size, Entity entity, boolean spin) {
        renderEntityInBox(context, rect, size, entity, spin, 500);
    }

    public static void renderEntityInBox(DrawContext context, ScreenRect rect, double size, Entity entity, boolean spin, int z) {
        context.enableScissor(
                rect.getLeft(), rect.getTop(),
                rect.getRight(), rect.getBottom()
        );
        renderEntity(context, rect.getLeft() + rect.width() / 2, (int) ((rect.getTop() + rect.height() / 2) + (size * entity.getHeight()) / 2), z, size, entity, spin);
        context.disableScissor();
    }

    public static void renderEntity(DrawContext context, int x, int y, int z, double size, Entity entity, boolean spin) {
        MinecraftClient client = MinecraftClient.getInstance();
        DiffuseLighting.disableGuiDepthLighting();
        context.getMatrices().push();
        if (entity instanceof SquidEntity) {
            size /= 1.5;
        } else if (entity instanceof ItemEntity) {
            size *= 2;
        }
        if (entity instanceof LivingEntity living && living.isBaby()) {
            size /= 1.7;
        }
        context.getMatrices().translate(x, y, 1000 + z);
        context.getMatrices().scale(1f, 1f, -1);
        context.getMatrices().translate(0, 0, 1000);
        context.getMatrices().scale((float) size, (float) size, (float) size);
        var quaternion = (new Quaternionf()).rotateZ(3.1415927F);
        var quaternion2 = (new Quaternionf()).rotateX(-10.f * 0.017453292F);
        quaternion.mul(quaternion2);
        context.getMatrices().multiply(quaternion);
        if (client.cameraEntity != null) {
            entity.setPos(client.cameraEntity.getX(), client.cameraEntity.getY(), client.cameraEntity.getZ());
        }

        if (!(entity instanceof PlayerEntity)) {
            assert client.player != null;
            entity.age = client.player.age;
        }
        setupAngles(entity, spin);

        var entityRenderDispatcher = client.getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        var immediate = client.getBufferBuilders().getEntityVertexConsumers();

        entityRenderDispatcher.render(entity, 0, 0, 0, 0.f, 1.f, context.getMatrices(), immediate,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    private static void setupAngles(Entity entity, boolean spin) {
        float yaw = spin ? (float) (((System.currentTimeMillis() / 10)) % 360) : 225.0F;
        entity.setYaw(yaw);
        entity.setHeadYaw(yaw);
        entity.setPitch(0.f);
        if (entity instanceof LivingEntity living) {
            living.bodyYaw = yaw;
        }
    }

    public static void drawTooltip(DrawContext context, TextRenderer textRenderer,
                                   List<TooltipComponent> components,
                                   int x, int y,
                                   TooltipPositioner positioner) {
        drawTooltip(context, textRenderer, components, x, y, positioner, false);
    }

    public static void drawTooltip(DrawContext context, TextRenderer textRenderer,
                                   TooltipComponent component,
                                   int x, int y,
                                   TooltipPositioner positioner,
                                   boolean favorite) {
        drawTooltip(context, textRenderer, List.of(component), x, y, positioner, favorite);
    }

    public static void drawTooltip(DrawContext context, TextRenderer textRenderer,
                                   List<TooltipComponent> components,
                                   int x, int y,
                                   TooltipPositioner positioner,
                                   boolean favorite) {
        renderTooltipAsFavorite = favorite;
        context.drawTooltip(textRenderer, components, x, y, positioner);
        renderTooltipAsFavorite = false;
    }

    public static void renderStarInFavoriteTooltip(DrawContext context, int x, int y, int width) {
        context.drawTexture(
                FavoriteButton.TEXTURE,
                x + width - (FavoriteButton.BUTTON_WIDTH + 3), y + 3, 400,
                0, 0,
                FavoriteButton.BUTTON_WIDTH, FavoriteButton.BUTTON_HEIGHT,
                FavoriteButton.TEXTURE_WIDTH, FavoriteButton.TEXTURE_HEIGHT
        );
    }

    public static void highlightSlot(DrawContext context, int xOffset, int yOffset, Slot slot, int color) {
        int x = xOffset + slot.x - 1;
        int y = yOffset + slot.y - 1;
        context.fillGradient(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 10, color, color);
    }
}
