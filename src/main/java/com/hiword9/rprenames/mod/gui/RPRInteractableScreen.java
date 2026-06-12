package com.hiword9.rprenames.mod.gui;

import net.minecraft.world.inventory.ContainerInput;

import static com.hiword9.rprenames.util.Util.*;

/**
 * Intended to be used in {@link net.minecraft.client.gui.screens.Screen Screen} subclass.
 * This interface has methods that are used in {@link com.hiword9.rprenames.mod.gui.widget.RPRWidget RPRWidget}
 *
 * @see com.hiword9.rprenames.mod.gui.widget.RPRWidget RPRWidget
 * @see com.hiword9.rprenames.mod.mixin.AnvilScreenMixin AnvilScreenMixin
 */
public interface RPRInteractableScreen {
    /**
     * Place stack from inventorySlot to craftSlot.
     *
     * @param inventorySlot id of slot in inventory
     * @param craftSlot id of slot in crafting grid
     */
    default void moveToCraft(int inventorySlot, int craftSlot) {
        var interactionManager = client().gameMode;
        if (player() == null || interactionManager == null) return;

        int syncId = player().containerMenu.containerId;

        // swapping in hotbar but picking in inventory cause server will ignore swapping if slot >= 9
        if (inventorySlot >= 9) {
            int i = inventorySlot - 9;
            i += getCraftSlotsAmount();
            // adding number of crafting slots because they are first in slots list, and we need to avoid them

            interactionManager.handleContainerInput(syncId, i, 0, ContainerInput.PICKUP, player());
            interactionManager.handleContainerInput(syncId, craftSlot, 0, ContainerInput.PICKUP, player());
            interactionManager.handleContainerInput(syncId, i, 0, ContainerInput.PICKUP, player());
        } else {
            interactionManager.handleContainerInput(syncId, craftSlot, inventorySlot, ContainerInput.SWAP, player());
        }
    }

    /**
     * Default logic: Place stack from workSlot to first suitable slot in inventory.
     * If no available slots found, drops stack on ground.
     */
    default void moveToInventory(int workSlot) {
        var interactionManager = client().gameMode;
        if (player() == null || interactionManager == null) return;

        var inventory = player().getInventory();
        var stack = player().containerMenu.slots.get(workSlot).getItem();
        if (stack.isEmpty()) return;
        int syncId = player().containerMenu.containerId;

        if (inventory.getSlotWithRemainingSpace(stack) != -1 || inventory.getFreeSlot() != -1) {
            interactionManager.handleContainerInput(syncId, workSlot, 0, ContainerInput.QUICK_MOVE, player());
            moveToInventory(workSlot);
        } else {
            interactionManager.handleContainerInput(syncId, workSlot, 99, ContainerInput.THROW, player());
        }
    }

    int getCraftSlotsAmount();

    void updateMenuShift();
}
