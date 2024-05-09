package com.HiWord9.RPRenames.util.gui;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.rename.RenamesHelper;
import com.HiWord9.RPRenames.util.rename.Rename;
import com.HiWord9.RPRenames.util.config.generation.CEMList;
import com.HiWord9.RPRenames.util.gui.button.RenameButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static com.HiWord9.RPRenames.util.gui.Graphics.SLOT_SIZE;

public class RenameButtonHolder extends Screen {
    private final ModConfig config = ModConfig.INSTANCE;

    final ViewMode viewMode;
    RenameButton button;
    boolean CEM = false;
    LivingEntity entity = null;
    Rename rename;
    ItemStack item;
    ItemStack icon;
    Text displayText = Text.empty();
    final int orderOnPage;
    ArrayList<TooltipComponent> tooltip = new ArrayList<>();
    boolean active;

    EquipmentSlot equipmentSlot = null;

    final int rowSize = 9;
    final int firstSlotX = 7;
    final int firstSlotY = 83;

    public RenameButtonHolder(ViewMode viewMode, int orderOnPage) {
        super(null);
        this.viewMode = viewMode;
        this.orderOnPage = orderOnPage;
    }

    public void drawElements(DrawContext context) {
        if (active) {
            if (viewMode == ViewMode.LIST) {
                if (CEM && config.renderMobRenamesAsEntities) {
                    Graphics.renderEntityInBox(context,
                            new ScreenRect(button.getX(), button.getY(), button.getHeight(), button.getHeight()), 1,
                            12 / (Math.max(entity.getHeight(), entity.getWidth())), entity, false, 200);
                } else {
                    Graphics.renderStack(context, icon, button.getX() + 2, button.getY() + 2);
                }
                Graphics.renderText(context, displayText, Graphics.DEFAULT_TEXT_COLOR, button.getX() + (button.getWidth() - 20) / 2 + 20, button.getY() + 7, true, true);
            } else {
                if (CEM && config.renderMobRenamesAsEntities) {
                    Graphics.renderEntityInBox(context,
                            new ScreenRect(button.getX(), button.getY(), button.getWidth() - 1, button.getHeight() - 1), 1,
                            14 / (Math.max(entity.getHeight(), entity.getWidth())), entity, false, 200);
                } else {
                    Graphics.renderStack(context, icon, button.getX() + 4, button.getY() + 4);
                }
            }
        }
    }

    public void highlightSlot(DrawContext context, ArrayList<Item> inventory, Item currentItem, int highlightColor) {
        int slotNum;
        for (Item item : rename.getItems()) {
            slotNum = inventory.indexOf(item);
            if (slotNum < 0) continue;

            int x = 26;
            int y = 46;
            if (!inventory.get(slotNum).equals(currentItem)) {
                boolean isOnHotBar = false;
                if (slotNum < rowSize) {
                    slotNum += rowSize * 3;
                    isOnHotBar = true;
                } else {
                    slotNum -= rowSize;
                }
                int orderInRow = slotNum % rowSize;
                int row = slotNum / rowSize;
                x = firstSlotX + (SLOT_SIZE * orderInRow);
                y = firstSlotY + (SLOT_SIZE * row);
                if (isOnHotBar) {
                    y += 4;
                }
            }
            RenderSystem.enableDepthTest();
            context.fillGradient(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 10, highlightColor, highlightColor);
        }
    }

    public void drawPreview(DrawContext context, int mouseX, int mouseY, int width, int height, double scaleFactorItem, double scaleFactorEntity) {
        if (!CEM) {
            if (hasShiftDown() != config.playerPreviewByDefault) {
                playerPreview(context, mouseX, mouseY, (int) (width * scaleFactorEntity), (int) (height * scaleFactorEntity), (int) (32 * scaleFactorEntity), config.spinPlayerPreview, item);
            } else {
                itemPreview(context, mouseX, mouseY, (int) (width / 2 * scaleFactorItem), (int) (height / 2 * scaleFactorItem), (int) (16 * scaleFactorItem), item);
            }
        } else {
            entityPreview(context, mouseX, mouseY, (int) (width * scaleFactorEntity), (int) (height * scaleFactorEntity), (int) (32 * scaleFactorEntity), config.spinMobPreview, entity);
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
        if (config.previewPos == PreviewPos.LEFT) {
            x -= (5 + width);
            y -= 16;

            if (y + height > this.height) {
                y = this.height - height;
            }
        } else {
            x += 8;
            int yOffset = 0;
            int tooltipHeight = 3 + 3;
            for (TooltipComponent component : tooltip) {
                tooltipHeight += component.getHeight();
            }
            if (tooltip.size() > 1) {
                tooltipHeight += 2;
            }
            if (config.enablePreview && !CEM && !config.disablePlayerPreviewTips && (!config.playerPreviewByDefault || !hasShiftDown())) {
                tooltipHeight += 10;
            }
            yOffset += tooltipHeight + 2 - 16;
            if (config.previewPos == PreviewPos.BOTTOM) {
                if (y + yOffset + height > this.height && ((y - (height + 18)) > height / -2)) {
                    y -= (height + 18);
                } else {
                    y += yOffset;
                }
            } else if (config.previewPos == PreviewPos.TOP) {
                if (y - (height + 18) < 0 && (y + yOffset + height - this.height < height / 2)) {
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

    private void itemPreview(DrawContext context, int mouseX, int mouseY, int width, int height, int size, ItemStack itemStack) {
        int[] pos = setPreviewPos(mouseX, mouseY, width, height);
        int x = pos[0];
        int y = pos[1];

        Graphics.drawTooltipBackground(context, x, y, width, height, button.favorite,  400);

        int newX = x + width / 2 - size / 2;
        int newY = y + height / 2 - size / 2;
        Graphics.renderStack(context, itemStack, newX, newY, 400, size);
    }

    private void prepareEntity(Entity entity) {
        if (entity == null) return;
        if (entity instanceof SnowGolemEntity) {
            ((SnowGolemEntity) entity).setHasPumpkin(!config.disableSnowGolemPumpkin);
        }
        entity.setCustomName(Text.of(rename.getName()));
    }

    private void entityPreview(DrawContext context, int mouseX, int mouseY, int width, int height, int size, boolean spin, LivingEntity entity) {
        int newWidth = (int) (width + size * entity.getWidth() - 1);
        int newHeight = (int) (height + size * entity.getHeight() - 1);

        int[] pos = setPreviewPos(mouseX, mouseY, newWidth, newHeight);
        int x = pos[0];
        int y = pos[1];

        Graphics.drawTooltipBackground(context, x, y, newWidth, newHeight, button.favorite);
        Graphics.renderEntityInBox(context,
                new ScreenRect(x, y, newWidth, newHeight), Graphics.TOOLTIP_CORNER,
                size, entity, spin);
    }

    private void playerPreview(DrawContext context, int mouseX, int mouseY, int width, int height, int size, boolean spin, ItemStack item) {
        var entity = MinecraftClient.getInstance().player;

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

        assert entity != null;
        ItemStack temp = entity.getEquippedStack(equipmentSlot);

        if (isArmor) {
            entity.getInventory().armor.set(armorSlot, item);
        } else {
            entity.equipStack(equipmentSlot, item);
        }

        int newWidth = (int) (width + size * entity.getWidth() - 1);
        int newHeight = (int) (height + size * entity.getHeight() - 1);
        int[] pos = setPreviewPos(mouseX, mouseY, newWidth, newHeight);
        int x = pos[0];
        int y = pos[1];

        float h = entity.bodyYaw;
        float i = entity.getYaw();
        float j = entity.getPitch();
        float k = entity.prevHeadYaw;
        float l = entity.headYaw;


        Graphics.drawTooltipBackground(context, x, y, newWidth, newHeight, button.favorite);
        Graphics.renderEntityInBox(context,
                new ScreenRect(x, y, newWidth, newHeight), Graphics.TOOLTIP_CORNER,
                size, entity, spin);

        entity.bodyYaw = h;
        entity.setYaw(i);
        entity.setPitch(j);
        entity.prevHeadYaw = k;
        entity.headYaw = l;

        if (isArmor) {
            entity.getInventory().armor.set(armorSlot, temp);
        } else {
            entity.equipStack(equipmentSlot, temp);
        }
    }

    public RenameButton getButton() {
        return button;
    }

    public int getOrderOnPage() {
        return orderOnPage;
    }

    public ArrayList<TooltipComponent> getTooltip() {
        return tooltip;
    }

    public boolean isCEM() {
        return CEM;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setParameters(RenameButton button, Rename rename, ArrayList<TooltipComponent> tooltip) {
        this.button = button;
        this.rename = rename;
        this.CEM = rename.isCEM();
        if (viewMode == ViewMode.LIST) {
            displayText = shortText(Text.of(this.rename.getName()), button.getWidth() - 32);
        }

        this.item = RenamesHelper.createItem(rename);

        if (CEM) {
            var entityType = CEMList.EntityFromName(rename.getMob().entity());
            var client = MinecraftClient.getInstance();
            assert entityType != null;
            this.entity = (LivingEntity) entityType.create(client.world);
            prepareEntity(entity);
        }
        if (CEM && rename.getProperties() == null) {
            this.icon = new ItemStack(rename.getMob().icon());
        } else {
            this.icon = this.item.copy();
        }
        this.tooltip = tooltip;
        this.equipmentSlot = null;
        this.active = true;

        this.client = MinecraftClient.getInstance();
        assert client.currentScreen != null;
        this.height = client.currentScreen.height;
        this.width = client.currentScreen.width;
    }

    private Text shortText(Text text, int length) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        String shortText;
        shortText = text.getString();
        if (renderer.getWidth(shortText) > length) {
            while (renderer.getWidth(shortText) > length || shortText.endsWith(" ")) {
                shortText = shortText.substring(0, shortText.length() - 1);
            }
            return Text.of(shortText + "...");
        }
        return Text.of(shortText);
    }

    public enum PreviewPos {
        BOTTOM,
        LEFT,
        TOP
    }

    public enum ViewMode {
        LIST,
        GRID
    }
}
