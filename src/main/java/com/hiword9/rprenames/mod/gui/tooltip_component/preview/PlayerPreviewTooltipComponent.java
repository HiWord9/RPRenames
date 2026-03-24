package com.hiword9.rprenames.mod.gui.tooltip_component.preview;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.List;

import static net.minecraft.world.entity.EquipmentSlot.*;

public class PlayerPreviewTooltipComponent extends EntityPreviewTooltipComponent {
    public final ItemStack stack;

    boolean extraSlotAvailable = true;
    EquipmentSlot extraEquipmentSlot = null;
    EquipmentSlot equipmentSlot;

    static final List<EquipmentSlot> ALLOWED_EQUIPMENT_SLOTS
            = List.of(MAINHAND, OFFHAND, FEET, LEGS, CHEST, HEAD);

    boolean alwaysAllowPlayerPreviewHead;

    public PlayerPreviewTooltipComponent(
            LocalPlayer entity, ItemStack stack,
            int width, int height,
            int size,
            boolean spin,
            boolean alwaysAllowPlayerPreviewHead
    ) {
        super(entity, width, height, size, spin);
        this.stack = stack;
        this.alwaysAllowPlayerPreviewHead = alwaysAllowPlayerPreviewHead;

        DataComponentMap components = this.stack.getComponents();
        if (components.has(DataComponents.EQUIPPABLE)) {
            var component = components.get(DataComponents.EQUIPPABLE);
            if (component != null && ALLOWED_EQUIPMENT_SLOTS.contains(component.slot())) {
                extraEquipmentSlot = component.slot();
            } else {
                extraSlotAvailable = false;
            }
        } else if (Block.byItem(this.stack.getItem()) == Blocks.CARVED_PUMPKIN) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (Block.byItem(this.stack.getItem()) instanceof AbstractSkullBlock) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (components.has(DataComponents.GLIDER)) {
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
    public void renderImage(Font textRenderer, int x, int y, int width, int height, GuiGraphics context) {
        LocalPlayer player = (LocalPlayer) entity;

        assert player != null;
        ItemStack temp = player.getItemBySlot(equipmentSlot);

        player.setItemSlot(equipmentSlot, stack);

        float h = player.yBodyRot;
        float i = player.getYRot();
        float j = player.getXRot();
        float k = player.yHeadRotO;
        float l = player.yHeadRot;

        super.renderImage(textRenderer, x, y, width, height, context);

        player.yBodyRot = h;
        player.setYRot(i);
        player.setXRot(j);
        player.yHeadRotO = k;
        player.yHeadRot = l;

        player.setItemSlot(equipmentSlot, temp);
    }

    public void cycleSlots() {
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
