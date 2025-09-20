package com.HiWord9.RPRenames.util.gui.tooltip_component.preview;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class PlayerPreviewTooltipComponent extends EntityPreviewTooltipComponent {
    public final ItemStack stack;

    boolean extraSlotAvailable = true;
    EquipmentSlot extraEquipmentSlot = null;
    EquipmentSlot equipmentSlot;

    public PlayerPreviewTooltipComponent(ClientPlayerEntity entity, ItemStack stack, int width, int height, int size, boolean spin) {
        super(entity, width, height, size, spin);
        this.stack = stack;

        ComponentMap components = this.stack.getComponents();
        if (components.contains(DataComponentTypes.EQUIPPABLE)) {
            var component = components.get(DataComponentTypes.EQUIPPABLE);
            if (component != null) {
                extraEquipmentSlot = component.slot();
            } else {
                extraSlotAvailable = false;
            }
        } else if (Block.getBlockFromItem(this.stack.getItem()) == Blocks.CARVED_PUMPKIN) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (Block.getBlockFromItem(this.stack.getItem()) instanceof AbstractSkullBlock) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (components.contains(DataComponentTypes.GLIDER)) {
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
    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
        ClientPlayerEntity player = (ClientPlayerEntity) entity;

        assert player != null;
        ItemStack temp = player.getEquippedStack(equipmentSlot);

        player.equipStack(equipmentSlot, stack);

        float h = player.bodyYaw;
        float i = player.getYaw();
        float j = player.getPitch();
        float k = player.lastHeadYaw;
        float l = player.headYaw;

        super.drawItems(textRenderer, x, y, width, height, context);

        player.bodyYaw = h;
        player.setYaw(i);
        player.setPitch(j);
        player.lastHeadYaw = k;
        player.headYaw = l;

        player.equipStack(equipmentSlot, temp);
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
