package com.HiWord9.RPRenames.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Intended to be used in {@link net.minecraft.client.gui.screen.Screen Screen} subclass.
 * This interface has methods that are used in {@link com.HiWord9.RPRenames.util.gui.widget.RPRWidget RPRWidget}
 *
 * @see com.HiWord9.RPRenames.util.gui.widget.RPRWidget RPRWidget
 * @see com.HiWord9.RPRenames.mixin.AnvilScreenMixin AnvilScreenMixin
 */
public interface RPRInteractableScreen {
    /**
     * Place stack from inventorySlot to workSlot.
     *
     * @param inventorySlot id of slot in inventory
     * @param workSlot id of slot in crafting grid
     *
     * @see #moveToCraftInternal(int, int, int)
     */
    // moveToWorkSlotInternal may be used here
    void moveToCraft(int inventorySlot, int workSlot);

    /**
     * Default logic: Place stack from workSlot to first suitable slot in inventory.
     * If no available slots found, drops stack on ground.
     */
    default void moveToInventory(int workSlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        if (player == null || interactionManager == null) return;

        PlayerInventory inventory = player.getInventory();
        ItemStack stack = player.currentScreenHandler.slots.get(workSlot).getStack();
        if (stack.isEmpty()) return;
        int syncId = player.currentScreenHandler.syncId;

        if (inventory.getOccupiedSlotWithRoomForStack(stack) != -1 || inventory.getEmptySlot() != -1) {
            interactionManager.clickSlot(syncId, workSlot, 0, SlotActionType.QUICK_MOVE, player);
            moveToInventory(workSlot);
        } else {
            interactionManager.clickSlot(syncId, workSlot, 99, SlotActionType.THROW, player);
        }
    }

    /**
     * Place stack from slotInInventory to workSlot.
     * Note that {@code slotInInventory == 0} is first slot in inventory,
     * and {@code workSlot == 0} is first slot in workbench.
     * <p> Intended to be implemented in {@link #moveToCraft(int, int)}
     *
     * @param slotInInventory id of slot in inventory
     * @param workSlot id of slot in crafting grid
     * @param craftSlotsOffset number of crafting slots on screen
     */
    static void moveToCraftInternal(int slotInInventory, int workSlot, int craftSlotsOffset) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        if (player == null || interactionManager == null) return;

        int syncId = player.currentScreenHandler.syncId;

        // swapping in hotbar but picking in inventory cause server will ignore swapping if slot >= 9
        if (slotInInventory >= 9) {
            int i = slotInInventory - 9;
            i += craftSlotsOffset;
            // adding number of crafting slots because they are first in slots list, and we need to avoid them

            interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, player);
            interactionManager.clickSlot(syncId, workSlot, 0, SlotActionType.PICKUP, player);
            interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, player);
        } else {
            interactionManager.clickSlot(syncId, workSlot, slotInInventory, SlotActionType.SWAP, player);
        }
    }

    void updateMenuShift();
}
