package com.HiWord9.RPRenames;

import com.HiWord9.RPRenames.configGeneration.CEMList;
import com.HiWord9.RPRenames.configGeneration.ConfigManager;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import me.shedaniel.math.Color;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3f;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;

public class RenameButton extends Screen {

    private final ModConfig config = ModConfig.INSTANCE;

    TexturedButtonWidget button;
    boolean CEM = false;
    int mobInList;
    Text name;
    ItemStack item;
    ItemIcon icon;
    WLabel displayText = new WLabel(Text.of(""),0xffffff);
    int orderOnPage;
    ArrayList<Text> tooltip = new ArrayList<>();
    int page;
    boolean active;

    EquipmentSlot equipmentSlot = null;

    int buttonHeight = 20;
    int buttonOffsetY = 2;

    int slotSize = 18;
    int rowSize = 9;
    int firstSlotX = 7;
    int firstSlotY = 83;

    public RenameButton() {
        super(null);
    }

    public void drawElements(MatrixStack matrices, int mouseX, int mouseY) {
        if (active) {
            icon.paint(matrices, -128, 32 + ((buttonHeight + 2) * orderOnPage), 16);
            WLabel shadow = new WLabel(displayText.getText(), 0x3f3f3f);
            shadow.setHorizontalAlignment(HorizontalAlignment.CENTER).paint(matrices, -70, 38 + (buttonHeight + buttonOffsetY) * orderOnPage, mouseX, mouseY);
            displayText.setHorizontalAlignment(HorizontalAlignment.CENTER).paint(matrices, -71, 37 + (buttonHeight + buttonOffsetY) * orderOnPage, mouseX, mouseY);
        }
    }

    public void highlightSlot(MatrixStack matrices, ArrayList<Integer> currentInvOrder, ArrayList<String> inventory, String currentItem, int highlightColor) {
        int orderInList = (page * 5) + orderOnPage;
        if (orderInList + 1 <= currentInvOrder.size()) {
            int slotNum = currentInvOrder.get(orderInList);
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
            fill(matrices, x, y, x + slotSize, y + slotSize, highlightColor);
        }
    }

    public void drawPreview(MatrixStack matrices, int x, int y, int width, int height, double scaleFactorItem, double scaleFactorEntity) {
        int xOffset = 8;
        int yOffset = 2;
        int rows = tooltip.size();
        if (config.enablePreview && !CEM && !config.disablePlayerPreviewTips && (!config.playerPreviewByDefault || !InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT))) {
            rows++;
        }
        if (rows > 1) {
            yOffset += 10 * (rows - 1) + 2;
        }
        if (!CEM) {
            if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) != config.playerPreviewByDefault) {
                playerPreview(matrices, x + xOffset, y + yOffset, (int) (width * scaleFactorEntity), (int) (height * scaleFactorEntity), (int) (32 * scaleFactorEntity), config.spinPlayerPreview, item);
            } else {
                itemPreview(matrices, x + xOffset, y + yOffset, (int) (width / 2 * scaleFactorItem), (int) (height / 2 * scaleFactorItem), (int) (16 * scaleFactorItem));
            }
        } else {
            entityPreview(matrices, x + xOffset, y + yOffset, (int) (width * scaleFactorEntity), (int) (height * scaleFactorEntity), (int) (32 * scaleFactorEntity), config.spinMobPreview);
        }
    }

    boolean bl = false;
    private boolean isKeyJustPressed(int key) {
        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), key)) {
            if (!bl) {
                bl = true;
                return true;
            }
        } else {
            bl = false;
        }
        return false;
    }

    private void itemPreview(MatrixStack matrices, int x, int y, int width, int height, int size) {
        drawFakeTooltip(matrices, x, y, width, height);
        icon.paint(matrices, x + width / 2 - size / 2, y + height / 2 - size / 2, size);
    }

    private void entityPreview(MatrixStack matrices, int x, int y, int width, int height, int size, boolean spin) {
        var entityType = CEMList.mobs[mobInList];
        var client = MinecraftClient.getInstance();
        var entity = (LivingEntity) entityType.create(client.world);
        assert entity != null;
        entity.setCustomName(name);
        if (entity instanceof SnowGolemEntity) {
            ((SnowGolemEntity) entity).setHasPumpkin(!config.disableSnowGolemPumpkin);
        }

        int newWidth = (int) (width + size * entity.getWidth() - 1);
        int newHeight = (int) (height + size * entity.getHeight() - 1);

        drawFakeTooltip(matrices, x, y, newWidth, newHeight);
        renderEntity(matrices, x + newWidth / 2, (int) (y + newHeight - size * 0.75), size, entity, spin);
    }

    private void renderEntity(MatrixStack matrices, int x, int y, int size, Entity entity, boolean spin) {
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
        matrices.translate(x, y, 1050);
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

    private void playerPreview(MatrixStack matrices, int x, int y, int width, int height, int size, boolean spin, ItemStack item) {
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
        drawFakeTooltip(matrices, x, y, newWidth, newHeight);
        renderPlayer(matrices, x + newWidth / 2, (int) (y + newHeight - size * 0.75), size, entity, spin);

        if (isArmor) {
            entity.getInventory().armor.set(armorSlot, temp);
        } else {
            entity.equipStack(equipmentSlot, temp);
        }
    }

    private void renderPlayer(MatrixStack matrices, int x, int y, int size, LivingEntity entity, boolean spin) {
        DiffuseLighting.disableGuiDepthLighting();
        matrices.push();
        matrices.translate(x, y, 1050);
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

        float h = entity.bodyYaw;
        float i = entity.getYaw();
        float j = entity.getPitch();
        float k = entity.prevHeadYaw;
        float l = entity.headYaw;

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

        entity.bodyYaw = h;
        entity.setYaw(i);
        entity.setPitch(j);
        entity.prevHeadYaw = k;
        entity.headYaw = l;

        matrices.pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    private void setupAngles(Entity entity, boolean spin) {
        float yaw = spin ? (float) (((System.currentTimeMillis() / 10)) % 360) : 225.0F;
        entity.setYaw(yaw);
        entity.setHeadYaw(yaw);
        entity.setPitch(0.f);
        if (entity instanceof LivingEntity living) {
            living.bodyYaw = yaw;
        }
    }

    public void drawFakeTooltip(MatrixStack matrices, int x, int y, int width, int height) {
        int alpha = 239;
        int bgColor = Color.ofRGBA(15,0,15, alpha).getColor();
        int topColor = Color.ofRGBA(37,1,91, alpha).getColor();
        int bottomColor = Color.ofRGBA(24,1,51, alpha).getColor();
        int x2 = x + width - 1;
        int y2 = y + height - 1;
        fill(matrices, x + 1, y, x2 - 1 + 1, y + 1, bgColor);
        fill(matrices, x + 1, y2, x2 - 1 + 1, y2 + 1, bgColor);
        fill(matrices, x, y + 1, x + 1, y2 - 1 + 1, bgColor);
        fill(matrices, x2, y + 1, x2 + 1, y2 - 1 + 1, bgColor);
        fill(matrices, x + 2, y + 2, x2 - 2 + 1, y2 - 2 + 1, bgColor);
        fill(matrices, x + 2, y + 1, x2 - 2 + 1, y + 1 + 1, topColor);
        fill(matrices, x + 2, y2 - 1, x2 - 2 + 1, y2 - 1 + 1, bottomColor);
        fillGradient(matrices, x + 1, y + 1, x + 1 + 1, y2 - 1 + 1, topColor, bottomColor);
        fillGradient(matrices, x2 - 1, y + 1, x2 - 1 + 1, y2 - 1 + 1, topColor, bottomColor);
    }

    public TexturedButtonWidget getButton() {
        return button;
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

    public void setParameters(TexturedButtonWidget button, Text name, ItemStack item, int page, int orderOnPage, boolean isCEM, int mobInList, ArrayList<Text> tooltip) {
        this.button = button;
        this.name = name;
        displayText.setText(shortText(name));
        this.item = item;
        this.item.setCustomName(name);
        this.icon = new ItemIcon(item);
        this.page = page;
        this.orderOnPage = orderOnPage;
        this.CEM = isCEM;
        this.mobInList = mobInList;
        this.tooltip = tooltip;
        this.equipmentSlot = null;
        this.active = true;
    }

    private Text shortText(Text text) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
        String shortText;
        shortText = text.getString();
        if (renderer.getWidth(shortText) > 92 - 5) {
            while (renderer.getWidth(shortText) > 92 - 5) {
                shortText = shortText.substring(0, shortText.length() - 1);
            }
            return Text.of(shortText + "...");
        }
        return Text.of(shortText);
    }

    public static boolean calcFavorite(String item, String name) {
        File file = new File(RPRenames.configPathFavorite + item + ".json");
        if (file.exists()) {
            String[] favoriteList = ConfigManager.configRead(file).getName();
            for (String s : favoriteList) {
                if (name.equals(s)) {
                    return true;
                }
            }
        }
        return false;
    }
}
