package com.HiWord9.RPRenames.util.gui;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import com.HiWord9.RPRenames.util.config.generation.CEMList;
import com.HiWord9.RPRenames.util.gui.button.RenameButton;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class RenameButtonHolder extends Screen {
    private final ModConfig config = ModConfig.INSTANCE;

    ViewMode viewMode;
    RenameButton button;
    boolean CEM = false;
    LivingEntity entity = null;
    Rename rename;
    ItemStack item;
    ItemStack icon;
    Text displayText = Text.empty();
    int orderOnPage;
    ArrayList<Text> tooltip = new ArrayList<>();
    int page;
    boolean active;

    EquipmentSlot equipmentSlot = null;

    int buttonHeight;
    int buttonOffsetY;

    int slotSize = 18;
    int rowSize = 9;
    int firstSlotX = 7;
    int firstSlotY = 83;

    final int backgroundWidth = Graphics.backgroundWidth;
    final int backgroundHeight = Graphics.backgroundHeight;

    public RenameButtonHolder(ViewMode viewMode, int orderOnPage) {
        super(null);
        this.viewMode = viewMode;
        if (viewMode == ViewMode.LIST) {
            buttonHeight = 20;
            buttonOffsetY = 2;
        } else {
            buttonHeight = 25;
            buttonOffsetY = 0;
        }
        this.orderOnPage = orderOnPage;
    }

    public void drawElements(DrawContext context) {
        if (active) {
            if (viewMode == ViewMode.LIST) {
                Graphics.renderStack(context, icon, button.getX() + 2, button.getY() + 2);
                Graphics.renderText(context, displayText, Graphics.DEFAULT_TEXT_COLOR, button.getX() + (button.getWidth() - 20) / 2 + 20, button.getY() + 7, true, true);
            } else {
                Graphics.renderStack(context, icon, button.getX() + 4, button.getY() + 4);
            }
        }
    }

    public void highlightSlot(DrawContext context, ArrayList<String> inventory, String currentItem, int highlightColor) {
        int slotNum = inventory.indexOf(rename.getItem());
        if (slotNum <= -1) return;
        int x;
        int y;
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
            x = firstSlotX + (slotSize * orderInRow);
            y = firstSlotY + (slotSize * row);
            if (isOnHotBar) {
                y += 4;
            }
        } else {
            x = 26;
            y = 46;
        }
        RenderSystem.enableDepthTest();
        context.fillGradient(x, y, x + slotSize, y + slotSize, 10, highlightColor, highlightColor);
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

    boolean pressFuse = false;

    private boolean isKeyJustPressed(int key) {
        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key)) {
            if (!pressFuse) {
                pressFuse = true;
                return true;
            }
        } else {
            pressFuse = false;
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
            int yOffset = 2;
            int rows = tooltip.size();
            if (config.enablePreview && !CEM && !config.disablePlayerPreviewTips && (!config.playerPreviewByDefault || !hasShiftDown())) {
                rows++;
            }
            if (rows > 1) {
                yOffset += 10 * (rows - 1) + 2;
            }
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

        Graphics.drawTooltipBackground(context, x, y, width, height, 400);

        int newX = x + width / 2 - size / 2;
        int newY = y + height / 2 - size / 2;
        Graphics.renderStack(context, itemStack, newX, newY, 400, size);
    }

    private void entityPreview(DrawContext context, int mouseX, int mouseY, int width, int height, int size, boolean spin, LivingEntity entity) {
        assert entity != null;
        entity.setCustomName(Text.of(rename.getName()));
        if (entity instanceof SnowGolemEntity) {
            ((SnowGolemEntity) entity).setHasPumpkin(!config.disableSnowGolemPumpkin);
        }

        int newWidth = (int) (width + size * entity.getWidth() - 1);
        int newHeight = (int) (height + size * entity.getHeight() - 1);

        int[] pos = setPreviewPos(mouseX, mouseY, newWidth, newHeight);
        int x = pos[0];
        int y = pos[1];

        Graphics.drawTooltipBackground(context, x, y, newWidth, newHeight);
        Graphics.renderEntity(context, x + newWidth / 2, (int) (y + newHeight - size * 0.75), size, entity, spin);
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

        if (isKeyJustPressed(GLFW.GLFW_KEY_F)) {
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

        Graphics.drawTooltipBackground(context, x, y, newWidth, newHeight);
        Graphics.renderPlayer(context, x + newWidth / 2, (int) (y + newHeight - size * 0.75), size, entity, spin);

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

    public ArrayList<Text> getTooltip() {
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

    public void setParameters(RenameButton button, Rename rename, int page, ArrayList<Text> tooltip) {
        this.button = button;
        this.rename = rename;
        this.CEM = rename.isCEM();
        if (viewMode == ViewMode.LIST) {
            displayText = shortText(Text.of(this.rename.getName()), button.getWidth() - 32);
        }

        this.item = createItem(rename);

        if (CEM) {
            var entityType = CEMList.EntityFromName(rename.getMob().entity());
            var client = MinecraftClient.getInstance();
            assert entityType != null;
            this.entity = (LivingEntity) entityType.create(client.world);
        }
        if (CEM && rename.getProperties() == null) {
            this.icon = new ItemStack(ConfigManager.itemFromName(rename.getMob().icon()));
        } else {
            this.icon = this.item.copy();
        }
        this.page = page;
        this.tooltip = tooltip;
        this.equipmentSlot = null;
        this.active = true;

        this.client = MinecraftClient.getInstance();
        assert client.currentScreen != null;
        this.height = client.currentScreen.height;
        this.width = client.currentScreen.width;
    }

    public static ItemStack createItem(Rename rename) {
        ItemStack item = new ItemStack(ConfigManager.itemFromName(rename.getItem()));
        item.setCustomName(Text.of(rename.getName()));
        item.setCount(rename.getStackSize());
        item.setDamage(rename.getDamage());
        if (rename.getEnchantment() != null) {
            item.getOrCreateNbt();
            assert item.getNbt() != null;
            if (!item.getNbt().contains("Enchantments", 9)) {
                item.getNbt().put("Enchantments", new NbtList());
            }
            NbtList nbtList = item.getNbt().getList("Enchantments", 10);
            nbtList.add(EnchantmentHelper.createNbt(new Identifier(rename.getEnchantment()), rename.getEnchantmentLevel()));
        }
        return item;
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
