package com.hiword9.rprenames.mod.gui;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.widget.external.FavoriteButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static com.hiword9.rprenames.mod.util.Util.*;

public class Graphics {
    static public final int DEFAULT_PREVIEW_WIDTH = 42;
    static public final int DEFAULT_PREVIEW_HEIGHT = 42;
    static public final int DEFAULT_PREVIEW_SIZE_ENTITY = 32;
    static public final int DEFAULT_PREVIEW_SIZE_ITEM = 16;

    static public final int SLOT_SIZE = 18;
    static public final int STACK_IN_SLOT_SIZE = 16;

    public static final int HIGHLIGHT_COLOR_WRONG = 822018048;
    public static final int HIGHLIGHT_COLOR_SECOND = 822018303;

    static public final int DEFAULT_TEXT_COLOR = 0xFFFFFFFF;

    static public boolean renderTooltipAsFavorite = false;

    public static final Identifier FAVORITE_TOOLTIP_FRAME_TEXTURE = Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "favorite_tooltip");

    public static void renderText(GuiGraphicsExtractor context, Component text, int x, int y, boolean shadow, boolean centered) {
        renderText(context, text, DEFAULT_TEXT_COLOR, x, y, shadow, centered);
    }

    public static void renderText(GuiGraphicsExtractor context, Component text, int color, int x, int y, boolean shadow, boolean centered) {
        var renderer = textRenderer();
        int xOffset = centered ? renderer.width(text) / 2 : 0;
        context.text(renderer, text, x - xOffset, y, color, shadow);
    }

    public static void renderGuiTexture(
            GuiGraphicsExtractor context,
            Identifier texture,
            int x, int y, float u, float v,
            int width, int height,
            int textureWidth, int textureHeight
    ) {
        context.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x, y, u, v,
                width, height,
                textureWidth, textureHeight
        );
    }

    public static void renderStack(GuiGraphicsExtractor context, ItemStack itemStack, int x, int y) {
        renderStack(context, itemStack, x, y, STACK_IN_SLOT_SIZE);
    }

    public static void renderStack(GuiGraphicsExtractor context, ItemStack itemStack, int x, int y, int size) {
        float scale = size != STACK_IN_SLOT_SIZE ? ((float) size / STACK_IN_SLOT_SIZE) : 1f;
        context.nextStratum();
        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(scale, scale);
        context.fakeItem(itemStack, 0, 0);
        matrices.popMatrix();
        context.guiRenderState.up();
    }

    @SuppressWarnings("unchecked")
    public static <S extends EntityRenderState, T extends Entity> void renderEntityInBox(GuiGraphicsExtractor context, ScreenRectangle rect, double size, T entity, boolean spin) {
        if (entity instanceof Squid) size /= 1.5;
        else if (entity instanceof ItemEntity) size *= 2;

        if (entity instanceof LivingEntity l && l.isBaby()) size /= 1.7;

        var quaternion = (new Quaternionf()).rotateZ(3.1415927F);
        var quaternion2 = (new Quaternionf()).rotateX(-10.f * 0.017453292F);
        quaternion.mul(quaternion2);

        var camera = client().getCameraEntity();
        if (camera != null) {
            entity.setPosRaw(camera.getX(), camera.getY(), camera.getZ());
        }

        if (!(entity instanceof Player)) {
            assert player() != null;
            entity.tickCount = player().tickCount;
        }
        setupAngles(entity, spin);

        var vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F, 0.0F);

        var entityRenderDispatcher = client().getEntityRenderDispatcher();
        var entityRenderer = (EntityRenderer<? super T, S>) entityRenderDispatcher.getRenderer(entity);
        var entityRenderState = entityRenderer.createRenderState();
        entityRenderer.extractRenderState(entity, entityRenderState, 1.0F);

        context.entity(
                entityRenderState,
                (float) size, vector3f, quaternion, quaternion2,
                rect.left(), rect.top(), rect.right(), rect.bottom()
        );
    }

    private static void setupAngles(Entity entity, boolean spin) {
        float yaw = spin ? (float) (((System.currentTimeMillis() / 10)) % 360) : 225.0F;
        entity.setYRot(yaw);
        entity.setYHeadRot(yaw);
        entity.setXRot(0.f);
        if (entity instanceof LivingEntity living) living.yBodyRot = yaw;
    }

    public static void drawTooltip(
            GuiGraphicsExtractor context, Font textRenderer,
            List<ClientTooltipComponent> components,
            int x, int y,
            ClientTooltipPositioner positioner
    ) {
        drawTooltip(context, textRenderer, components, x, y, positioner, false);
    }

    public static void drawTooltip(
            GuiGraphicsExtractor context, Font textRenderer,
            ClientTooltipComponent component,
            int x, int y,
            ClientTooltipPositioner positioner,
            boolean favorite
    ) {
        drawTooltip(context, textRenderer, List.of(component), x, y, positioner, favorite);
    }

    public static void drawTooltipWithFixedBorders(
            GuiGraphicsExtractor context, Font textRenderer,
            ClientTooltipComponent component,
            int x, int y,
            ClientTooltipPositioner positioner,
            boolean favorite
    ) {
        drawTooltip(
                context, textRenderer,
                List.of(component,
                        new ClientTooltipComponent() { //dump tooltip component to increase list size
                            public int getHeight(Font textRenderer) {return 0;}
                            public int getWidth(Font textRenderer) {return 0;}
                        }
                ),
                x, y, positioner, favorite
        );
    }

    public static void drawTooltip(
            GuiGraphicsExtractor context, Font textRenderer,
            List<ClientTooltipComponent> components,
            int x, int y,
            ClientTooltipPositioner positioner,
            boolean favorite
    ) {
        renderTooltipAsFavorite = favorite;
        context.tooltip(textRenderer, components, x, y, positioner, null);
        renderTooltipAsFavorite = false;
    }

    public static void renderStarInFavoriteTooltip(GuiGraphicsExtractor context, int x, int y, int width) {
        context.pose().pushMatrix();
        context.pose().translate(0,0);
        renderGuiTexture(
                context,
                FavoriteButton.TEXTURE,
                x + width - (FavoriteButton.BUTTON_WIDTH), y,
                0, 0,
                FavoriteButton.BUTTON_WIDTH, FavoriteButton.BUTTON_HEIGHT,
                FavoriteButton.TEXTURE_WIDTH, FavoriteButton.TEXTURE_HEIGHT
        );
        context.pose().popMatrix();
    }

    public static <H extends AbstractContainerMenu, S extends AbstractContainerScreen<H> & RPRInteractableScreen> void highlightAvailableSlots(
            List<Item> items, GuiGraphicsExtractor context, S screen, int color
    ) {
        var allSlots = screen.getMenu().slots;

        var slotsToHighlight = new ArrayList<Slot>();
        slotsToHighlight.add(allSlots.getFirst());
        slotsToHighlight.addAll(
                allSlots.subList(screen.getCraftSlotsAmount(), allSlots.size())
        );

        highlightSlots(items, slotsToHighlight, context, screen.leftPos, screen.topPos, color);
    }

    public static void highlightSlots(
            List<Item> items, List<Slot> slots,
            GuiGraphicsExtractor context,
            int xOffset, int yOffset,
            int color
    ) {
        for (Slot slot : slots)
            if (items.contains(slot.getItem().getItem()))
                highlightSlot(context, xOffset, yOffset, slot, color);
    }

    public static void highlightSlot(GuiGraphicsExtractor context, int xOffset, int yOffset, Slot slot, int color) {
        int x = xOffset + slot.x - 1;
        int y = yOffset + slot.y - 1;
        context.fillGradient(x, y, x + SLOT_SIZE, y + SLOT_SIZE, color, color);
    }

    public static ClientTooltipComponent tooltipOf(String string) {
        return tooltipOf(Component.nullToEmpty(string));
    }

    public static ClientTooltipComponent tooltipOf(Component mutableText) {
        return ClientTooltipComponent.create(mutableText.getVisualOrderText());
    }
}
