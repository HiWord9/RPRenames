package com.HiWord9.RPRenames.util.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public class Graphics extends Screen {
    static final int TOOLTIP_COLOR_BG = -267386864;
    static final int TOOLTIP_COLOR_TOP = -266010277;
    static final int TOOLTIP_COLOR_BOTTOM = -266862285;

    static public final int DEFAULT_TEXT_COLOR = 0xffffff;

    static public final int backgroundWidth = 176;
    static public final int backgroundHeight = 166;

    protected Graphics() {
        super(null);
    }

    public static void renderText(MatrixStack matrices, Text text, int x, int y, boolean shadow, boolean centered) {
        renderText(matrices, text, DEFAULT_TEXT_COLOR, x, y, shadow, centered);
    }

    public static void renderText(MatrixStack matrices, Text text, int color, int x, int y, boolean shadow, boolean centered) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer renderer = client.textRenderer;
        int xOffset = 0;
        if (centered) {
            xOffset = renderer.getWidth(text) / 2;
        }
        if (shadow) {
            renderer.drawWithShadow(matrices, text, x - xOffset, y, color);
        } else {
            renderer.draw(matrices, text, x - xOffset, y, color);
        }
    }

    public static void renderStack(ItemStack itemStack, int x, int y) {
        renderStack(itemStack, x, y, null, 16);
    }

    public static void renderStack(ItemStack itemStack, int x, int y, @Nullable Integer z, int size) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer renderer = client.getItemRenderer();
        MatrixStack modelViewMatrices = RenderSystem.getModelViewStack();

        float scale = size != 16 ? ((float) size / 16f) : 1f;

        modelViewMatrices.push();
        modelViewMatrices.translate(x, y, 0);
        modelViewMatrices.scale(scale, scale, 1);

        float temp = 999;
        if (z != null) {
            temp = renderer.zOffset;
            renderer.zOffset = z;
        }

        RenderSystem.applyModelViewMatrix();
        renderer.renderInGui(itemStack, 0, 0);
        modelViewMatrices.pop();
        RenderSystem.applyModelViewMatrix();

        if (z != null) {
            renderer.zOffset = temp;
        }
    }

    public static void renderEntity(MatrixStack matrices, int x, int y, int size, Entity entity, boolean spin) {
        DiffuseLighting.disableGuiDepthLighting();
        matrices.push();
        if (entity instanceof SquidEntity) {
            size /= 1.5;
        } else if (entity instanceof ItemEntity) {
            size *= 2;
        }
        if (entity instanceof LivingEntity living && living.isBaby()) {
            size /= 1.7;
        }
        matrices.translate(x, y, 1500);
        matrices.scale(1f, 1f, -1);
        matrices.translate(0, 0, 1000);
        matrices.scale(size, size, size);
        var quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.f);
        var quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(-10.f);
        quaternion.hamiltonProduct(quaternion2);
        matrices.multiply(quaternion);
        if (MinecraftClient.getInstance().cameraEntity != null) {
            entity.setPos(MinecraftClient.getInstance().cameraEntity.getX(), MinecraftClient.getInstance().cameraEntity.getY(), MinecraftClient.getInstance().cameraEntity.getZ());
        }

        assert MinecraftClient.getInstance().player != null;
        entity.age = MinecraftClient.getInstance().player.age;
        setupAngles(entity, spin);

        var entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        var immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        entityRenderDispatcher.render(entity, 0, 0, 0, 0.f, 1.f, matrices, immediate,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        matrices.pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    public static void renderPlayer(MatrixStack matrices, int x, int y, int size, LivingEntity player, boolean spin) {
        DiffuseLighting.disableGuiDepthLighting();
        matrices.push();
        matrices.translate(x, y, 1500);
        matrices.scale(1f, 1f, -1);
        matrices.translate(0, 0, 1000);
        matrices.scale(size, size, size);
        var quaternion = Vec3f.POSITIVE_Z.getDegreesQuaternion(180.f);
        var quaternion2 = Vec3f.POSITIVE_X.getDegreesQuaternion(-10.f);
        quaternion.hamiltonProduct(quaternion2);
        matrices.multiply(quaternion);
        if (MinecraftClient.getInstance().cameraEntity != null) {
            player.setPos(MinecraftClient.getInstance().cameraEntity.getX(), MinecraftClient.getInstance().cameraEntity.getY(), MinecraftClient.getInstance().cameraEntity.getZ());
        }

        float h = player.bodyYaw;
        float i = player.getYaw();
        float j = player.getPitch();
        float k = player.prevHeadYaw;
        float l = player.headYaw;

        setupAngles(player, spin);

        var entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        var immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        entityRenderDispatcher.render(player, 0, 0, 0, 0.f, 1.f, matrices, immediate,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
        );
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);

        player.bodyYaw = h;
        player.setYaw(i);
        player.setPitch(j);
        player.prevHeadYaw = k;
        player.headYaw = l;

        matrices.pop();
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

    public static void drawTooltipBackground(MatrixStack matrices, int x, int y, int width, int height) {
        drawTooltipBackground(matrices, x, y, width, height, 400);
    }

    public static void drawTooltipBackground(MatrixStack matrices, int x, int y, int width, int height, int z) {
        int x2 = x + width - 1;
        int y2 = y + height - 1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        fillGradient(matrix4f, bufferBuilder, x + 1, y, x2, y + 1, z, TOOLTIP_COLOR_BG, TOOLTIP_COLOR_BG);
        fillGradient(matrix4f, bufferBuilder, x + 1, y2, x2, y2 + 1, z, TOOLTIP_COLOR_BG, TOOLTIP_COLOR_BG);
        fillGradient(matrix4f, bufferBuilder, x, y + 1, x + 1, y2, z, TOOLTIP_COLOR_BG, TOOLTIP_COLOR_BG);
        fillGradient(matrix4f, bufferBuilder, x2, y + 1, x2 + 1, y2, z, TOOLTIP_COLOR_BG, TOOLTIP_COLOR_BG);
        fillGradient(matrix4f, bufferBuilder, x + 2, y + 2, x2 - 1, y2 - 1, z, TOOLTIP_COLOR_BG, TOOLTIP_COLOR_BG);
        fillGradient(matrix4f, bufferBuilder, x + 2, y + 1, x2 - 1, y + 2, z, TOOLTIP_COLOR_TOP, TOOLTIP_COLOR_TOP);
        fillGradient(matrix4f, bufferBuilder, x + 2, y2 - 1, x2 - 1, y2, z, TOOLTIP_COLOR_BOTTOM, TOOLTIP_COLOR_BOTTOM);
        fillGradient(matrix4f, bufferBuilder, x + 1, y + 1, x + 2, y2, z, TOOLTIP_COLOR_TOP, TOOLTIP_COLOR_BOTTOM);
        fillGradient(matrix4f, bufferBuilder, x2 - 1, y + 1, x2, y2, z, TOOLTIP_COLOR_TOP, TOOLTIP_COLOR_BOTTOM);
        RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }
}
