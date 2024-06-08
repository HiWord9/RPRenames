package com.HiWord9.RPRenames.util.gui.tooltipcomponent.preview;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;

public class PlayerPreviewTooltipComponent extends EntityPreviewTooltipComponent {
    public final ItemStack stack;

    boolean extraSlotAvailable = true;
    EquipmentSlot extraEquipmentSlot = null;
    EquipmentSlot equipmentSlot;

    public PlayerPreviewTooltipComponent(ClientPlayerEntity entity, ItemStack stack, int width, int height, int size, boolean spin) {
        super(entity, width, height, size, spin);
        this.stack = stack;

        if (this.stack.getItem() instanceof ArmorItem armorItem) {
            extraEquipmentSlot = armorItem.getSlotType();
        } else if (Block.getBlockFromItem(this.stack.getItem()) == Blocks.CARVED_PUMPKIN) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (Block.getBlockFromItem(this.stack.getItem()) instanceof AbstractSkullBlock) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (this.stack.getItem() instanceof ElytraItem) {
            extraEquipmentSlot = EquipmentSlot.CHEST;
        } else {
            extraSlotAvailable = false;
        }

        if (extraSlotAvailable) {
            equipmentSlot = extraEquipmentSlot;
        } else {
            equipmentSlot = EquipmentSlot.MAINHAND;
        }
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        ClientPlayerEntity player = (ClientPlayerEntity) entity;

        boolean isArmor = false;
        int armorSlot = 0;
        if (equipmentSlot != EquipmentSlot.MAINHAND && equipmentSlot != EquipmentSlot.OFFHAND) {
            isArmor = true;
            armorSlot = equipmentSlot.getEntitySlotId();
        }

        assert player != null;
        ItemStack temp = player.getEquippedStack(equipmentSlot);

        if (isArmor) {
            player.getInventory().armor.set(armorSlot, stack);
        } else {
            player.equipStack(equipmentSlot, stack);
        }

        float h = player.bodyYaw;
        float i = player.getYaw();
        float j = player.getPitch();
        float k = player.prevHeadYaw;
        float l = player.headYaw;

        super.drawItems(textRenderer, x, y, context);

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

    public void cycleSlots() {
        cycleSlots(false);
    }

    public void cycleSlots(boolean alwaysAllowPlayerPreviewHead) {
        if (equipmentSlot == EquipmentSlot.HEAD) {
            if (extraSlotAvailable && extraEquipmentSlot != EquipmentSlot.HEAD && alwaysAllowPlayerPreviewHead) {
                equipmentSlot = extraEquipmentSlot;
            } else {
                equipmentSlot = EquipmentSlot.MAINHAND;
            }
        } else if (equipmentSlot == EquipmentSlot.MAINHAND) {
            equipmentSlot = EquipmentSlot.OFFHAND;
        } else if (equipmentSlot == EquipmentSlot.OFFHAND) {
            if (alwaysAllowPlayerPreviewHead) {
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
}
