package com.HiWord9.RPRenames.util.gui.widget.button;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.Tab;
import com.HiWord9.RPRenames.util.config.generation.CEMList;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.AbstractRename;
import com.HiWord9.RPRenames.util.rename.CEMRename;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class RenameButton extends ClickableWidget {
    private static final ModConfig config = ModConfig.INSTANCE;
    final int highlightColor = config.getSlotHighlightRGBA();

    private static final Identifier TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/button.png");

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 25;
    public static final int BUTTON_HEIGHT = 25;

    static final int TEXTURE_WIDTH = BUTTON_WIDTH * 2;
    static final int TEXTURE_HEIGHT = BUTTON_HEIGHT * 2;

    static final int FOCUSED_OFFSET_V = BUTTON_WIDTH;
    static final int FAVORITE_OFFSET_U = BUTTON_HEIGHT;

    boolean selected = false;

    final public boolean favorite;

    LivingEntity entity = null;
    ItemStack item;
    ItemStack icon;
    ArrayList<TooltipComponent> tooltipComponents;
    EquipmentSlot equipmentSlot;

    final int indexInInventory;
    final boolean isInInventory;
    final boolean asCurrentItem;
    final public AbstractRename rename;
    final boolean enoughStackSize;
    final boolean enoughDamage;
    final boolean hasEnchant;
    final boolean hasEnoughLevels;

    public RenameButton(RPRWidget instance, AbstractRename rename, ArrayList<TooltipComponent> tooltip,
                        int x, int y,
                        boolean favorite,
                        int indexInInventory, boolean isInInventory, boolean asCurrentItem,
                        boolean enoughStackSize, boolean enoughDamage,
                        boolean hasEnchant, boolean hasEnoughLevels) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;

        this.favorite = favorite;

        this.indexInInventory = indexInInventory;
        this.isInInventory = isInInventory;
        this.asCurrentItem = asCurrentItem;
        this.rename = rename;
        this.enoughStackSize = enoughStackSize;
        this.enoughDamage = enoughDamage;
        this.hasEnchant = hasEnchant;
        this.hasEnoughLevels = hasEnoughLevels;

        this.item = rename.toStack();

        if (rename instanceof CEMRename cemRename) {
            var entityType = CEMList.EntityFromName(cemRename.getMob().entity());
            var client = MinecraftClient.getInstance();
            assert entityType != null;
            this.entity = (LivingEntity) entityType.create(client.world);
            prepareEntity(entity);
        }
        if (rename instanceof CEMRename cemRename && rename.getProperties() == null) {
            this.icon = new ItemStack(cemRename.getMob().icon());
        } else {
            this.icon = this.item.copy();
        }
        this.tooltipComponents = tooltip;
        this.equipmentSlot = null;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int u = favorite ? FAVORITE_OFFSET_U : 0;
        int v = hovered || (selected && config.highlightSelected) ? FOCUSED_OFFSET_V : 0;
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        drawElements(context);
    }

    public void postRender(DrawContext context, int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return;
        ArrayList<TooltipComponent> tooltip = new ArrayList<>(tooltipComponents);
        Screen screen = rprWidget.getScreen();
        if (!isCEM() && config.enablePreview) {
            if (!hasShiftDown() && !config.playerPreviewByDefault) {
                if (!config.disablePlayerPreviewTips) {
                    tooltip.add(TooltipComponent.of(
                            Text.translatable("rprenames.gui.tooltipHint.playerPreviewTip.holdShift")
                                    .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true))
                                    .asOrderedText()));
                }
            } else if (hasShiftDown() != config.playerPreviewByDefault) {
                screen.setFocused(null);
                if (!config.disablePlayerPreviewTips) {
                    tooltip.add(TooltipComponent.of(
                            Text.translatable("rprenames.gui.tooltipHint.playerPreviewTip.pressF")
                                    .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true))
                                    .asOrderedText()));
                }
            }
        }
        if ((rprWidget.getCurrentTab() == Tab.INVENTORY || rprWidget.getCurrentTab() == Tab.GLOBAL) && (config.slotHighlightColorALPHA > 0 && config.highlightSlot)) {
            if (screen instanceof HandledScreen<?> handledScreen) {
                highlightSlot(context, handledScreen.x, handledScreen.y, handledScreen.getScreenHandler().slots, highlightColor);
            }
        }
        Graphics.drawTooltip(
                context,
                MinecraftClient.getInstance().textRenderer,
                tooltip,
                mouseX, mouseY,
                HoveredTooltipPositioner.INSTANCE
        );
        if (config.enablePreview) {
            drawPreview(
                    context,
                    mouseX, mouseY,
                    52, 52,
                    config.scaleFactorItem, config.scaleFactorEntity
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            return execute(button);
        }
        return false;
    }

    public boolean execute(int button) {
        rprWidget.onRenameButton(button, favorite,
                indexInInventory, isInInventory, asCurrentItem,
                rename,
                enoughStackSize, enoughDamage,
                hasEnchant, hasEnoughLevels);
        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void drawElements(DrawContext context) {
        if (isCEM() && config.renderMobRenamesAsEntities) {
            Graphics.renderEntityInBox(context,
                    new ScreenRect(getX(), getY(), getWidth() - 1, getHeight() - 1), 1,
                    14 / (Math.max(entity.getHeight(), entity.getWidth())), entity, false, 200);
        } else {
            Graphics.renderStack(context, icon, getX() + 4, getY() + 4);
        }
    }

    public void highlightSlot(DrawContext context, int xOffset, int yOffset, DefaultedList<Slot> slots, int highlightColor) {
        for (Item item : rename.getItems()) {
            int slotNum = -1;
            int i = 0;
            Slot slot = null;
            for (Slot s : slots) {
                if ((i != 1 && i != 2) && s.getStack().isOf(item)) {
                    slotNum = i;
                    slot = s;
                    break;
                }
                i++;
            }
            if (slotNum < 0) continue;

            Graphics.highlightSlot(context, xOffset, yOffset, slot, highlightColor);
        }
    }

    public void drawPreview(DrawContext context, int mouseX, int mouseY, int width, int height, double scaleFactorItem, double scaleFactorEntity) {
        int newWidth;
        int newHeight;
        int size;
        LivingEntity entity = this.entity;
        boolean shouldPreviewEntity = isCEM();
        boolean shouldPreviewPlayer = hasShiftDown() != config.playerPreviewByDefault;
        if (shouldPreviewEntity || shouldPreviewPlayer) {
            size = (int) (32 * scaleFactorEntity);
            if (!shouldPreviewEntity) {
                entity = MinecraftClient.getInstance().player;
            }
            if (entity == null) return;
            newWidth = (int) (width + size * entity.getWidth() - 1);
            newHeight = (int) (height + size * entity.getHeight() - 1);
        } else {
            size = (int) (16 * scaleFactorItem);
            newWidth = (int) (width / 2 * scaleFactorItem);
            newHeight = (int) (height / 2 * scaleFactorItem);
        }
        int[] pos = setPreviewPos(mouseX, mouseY, newWidth, newHeight);
        int x = pos[0];
        int y = pos[1];

        if (shouldPreviewEntity) {
            entityPreview(context, x, y, newWidth, newHeight, size, config.spinMobPreview, entity);
        } else if (shouldPreviewPlayer) {
            playerPreview(context, x, y, newWidth, newHeight, size, config.spinPlayerPreview, (ClientPlayerEntity) entity, item);
        } else {
            itemPreview(context, x, y, newWidth, newHeight, size, item);
        }
    }

    boolean fPressFuse = false;

    private boolean isFKeyJustPressed() {
        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F)) {
            if (!fPressFuse) {
                fPressFuse = true;
                return true;
            }
        } else {
            fPressFuse = false;
        }
        return false;
    }

    private int[] setPreviewPos(int x, int y, int width, int height) {
        int[] positions = new int[2];
        Screen screen = rprWidget.getScreen();
        if (config.previewPos == PreviewPos.LEFT) {
            x -= (5 + width);
            y -= 16;

            if (y + height > screen.height) {
                y = screen.height - height;
            }
        } else {
            x += 8;
            int yOffset = 0;
            int tooltipHeight = 3 + 3;
            for (TooltipComponent component : tooltipComponents) {
                tooltipHeight += component.getHeight();
            }
            if (tooltipComponents.size() > 1) {
                tooltipHeight += 2;
            }
            if (config.enablePreview && !isCEM() && !config.disablePlayerPreviewTips && (!config.playerPreviewByDefault || !hasShiftDown())) {
                tooltipHeight += 10;
            }
            yOffset += tooltipHeight + 2 - 16;
            if (config.previewPos == PreviewPos.BOTTOM) {
                if (y + yOffset + height > screen.height && ((y - (height + 18)) > height / -2)) {
                    y -= (height + 18);
                } else {
                    y += yOffset;
                }
            } else if (config.previewPos == PreviewPos.TOP) {
                if (y - (height + 18) < 0 && (y + yOffset + height - screen.height < height / 2)) {
                    y += yOffset;
                } else {
                    y -= (height + 18);
                }
            }
        }
        positions[0] = x;
        positions[1] = y;
        return positions;
    }

    private void itemPreview(DrawContext context, int x, int y, int width, int height, int size, ItemStack itemStack) {
        Graphics.drawTooltipBackground(context, x, y, width, height, favorite,  400);

        int newX = x + width / 2 - size / 2;
        int newY = y + height / 2 - size / 2;
        Graphics.renderStack(context, itemStack, newX, newY, 400, size);
    }

    private void entityPreview(DrawContext context, int x, int y, int width, int height, int size, boolean spin, LivingEntity entity) {
        Graphics.drawTooltipBackground(context, x, y, width, height, favorite);
        Graphics.renderEntityInBox(context,
                new ScreenRect(x, y, width, height), Graphics.TOOLTIP_CORNER,
                size, entity, spin);
    }

    private void playerPreview(DrawContext context, int x, int y, int width, int height, int size, boolean spin, ClientPlayerEntity player, ItemStack item) {
//        var player = MinecraftClient.getInstance().player;

        boolean extraSlotAvailable = true;
        EquipmentSlot extraEquipmentSlot = null;

        if (item.getItem() instanceof ArmorItem armorItem) {
            extraEquipmentSlot = armorItem.getSlotType();
        } else if (Block.getBlockFromItem(item.getItem()) == Blocks.CARVED_PUMPKIN) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (Block.getBlockFromItem(item.getItem()) instanceof AbstractSkullBlock) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (item.getItem() instanceof ElytraItem) {
            extraEquipmentSlot = EquipmentSlot.CHEST;
        } else {
            extraSlotAvailable = false;
        }

        if (equipmentSlot == null) {
            if (extraSlotAvailable) {
                equipmentSlot = extraEquipmentSlot;
            } else {
                equipmentSlot = EquipmentSlot.MAINHAND;
            }
        }

        if (isFKeyJustPressed()) {
            if (equipmentSlot == EquipmentSlot.HEAD) {
                if (extraSlotAvailable && extraEquipmentSlot != EquipmentSlot.HEAD && config.alwaysAllowPlayerPreviewHead) {
                    equipmentSlot = extraEquipmentSlot;
                } else {
                    equipmentSlot = EquipmentSlot.MAINHAND;
                }
            } else if (equipmentSlot == EquipmentSlot.MAINHAND) {
                equipmentSlot = EquipmentSlot.OFFHAND;
            } else if (equipmentSlot == EquipmentSlot.OFFHAND) {
                if (config.alwaysAllowPlayerPreviewHead) {
                    equipmentSlot = EquipmentSlot.HEAD;
                } else {
                    if (extraSlotAvailable) {
                        equipmentSlot = extraEquipmentSlot;
                    } else {
                        equipmentSlot = EquipmentSlot.MAINHAND;
                    }
                }
            } else if (equipmentSlot == extraEquipmentSlot) {
                equipmentSlot = EquipmentSlot.MAINHAND;
            }
        }

        boolean isArmor = false;
        int armorSlot = 0;
        if (equipmentSlot != EquipmentSlot.MAINHAND && equipmentSlot != EquipmentSlot.OFFHAND) {
            isArmor = true;
            if (equipmentSlot == EquipmentSlot.LEGS) {
                armorSlot = 1;
            } else if (equipmentSlot == EquipmentSlot.CHEST) {
                armorSlot = 2;
            } else if (equipmentSlot == EquipmentSlot.HEAD) {
                armorSlot = 3;
            }
        }

        assert player != null;
        ItemStack temp = player.getEquippedStack(equipmentSlot);

        if (isArmor) {
            player.getInventory().armor.set(armorSlot, item);
        } else {
            player.equipStack(equipmentSlot, item);
        }

        float h = player.bodyYaw;
        float i = player.getYaw();
        float j = player.getPitch();
        float k = player.prevHeadYaw;
        float l = player.headYaw;

        Graphics.drawTooltipBackground(context, x, y, width, height, favorite);
        Graphics.renderEntityInBox(context,
                new ScreenRect(x, y, width, height), Graphics.TOOLTIP_CORNER,
                size, player, spin);

        player.bodyYaw = h;
        player.setYaw(i);
        player.setPitch(j);
        player.prevHeadYaw = k;
        player.headYaw = l;

        if (isArmor) {
            player.getInventory().armor.set(armorSlot, temp);
        } else {
            player.equipStack(equipmentSlot, temp);
        }
    }

    private void prepareEntity(Entity entity) {
        if (entity == null) return;
        if (entity instanceof SnowGolemEntity) {
            ((SnowGolemEntity) entity).setHasPumpkin(!config.disableSnowGolemPumpkin);
        }
        entity.setCustomName(Text.of(rename.getName()));
    }

    public boolean isCEM() {
        return rename instanceof CEMRename;
    }

    public enum PreviewPos {
        BOTTOM,
        LEFT,
        TOP
    }
}
