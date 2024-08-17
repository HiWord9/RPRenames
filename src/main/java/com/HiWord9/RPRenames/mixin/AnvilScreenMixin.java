package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.FavoritesManager;
import com.HiWord9.RPRenames.util.gui.widget.button.external.FavoriteButton;
import com.HiWord9.RPRenames.util.gui.widget.button.external.OpenerButton;
import com.HiWord9.RPRenames.util.gui.widget.GhostCraft;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AnvilScreen.class, priority = 1200)
public abstract class AnvilScreenMixin extends Screen {
    private static final ModConfig config = ModConfig.INSTANCE;

    protected AnvilScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private TextFieldWidget nameField;

    boolean afterPutInAnvilFirst = false;
    boolean afterPutInAnvilSecond = false;

    final int menuXOffset = 1;

    OpenerButton opener;
    FavoriteButton favoriteButton;

    final int menuShift = 77;

    RPRWidget rprWidget = new RPRWidget();

    GhostCraft ghostCraft;

    @Inject(at = @At("TAIL"), method = "setup")
    private void init(CallbackInfo ci) {
        if (!config.enableAnvilModification) return;

        assert client != null && client.currentScreen != null;
        int x = ((AnvilScreen) client.currentScreen).x;
        int y = ((AnvilScreen) client.currentScreen).y;

        opener = new OpenerButton(rprWidget, x + 3, y + 44, this::onToggleOpenRPRWidget);
        favoriteButton = new FavoriteButton(rprWidget, x, y, config.favoriteButtonPosition);

        DefaultedList<Slot> slots = ((AnvilScreen) client.currentScreen).getScreenHandler().slots;
        ghostCraft = new GhostCraft(
                new GhostCraft.GhostSlot(x + slots.get(0).x - 1, y + slots.get(0).y - 1),
                new GhostCraft.GhostSlot(x + slots.get(1).x - 1, y + slots.get(1).y - 1),
                new GhostCraft.GhostSlot(x + slots.get(2).x - 1, y + slots.get(2).y - 1)
        );

        rprWidget.init(client, client.currentScreen, x - RPRWidget.WIDGET_WIDTH - menuXOffset, y);
        rprWidget.connect(
                new RPRWidget.ConnectionName() {
                    public String getText() {
                        return nameField.getText();
                    }
                    public void setText(String name) {
                        nameField.setText(name);
                    }
                },
                new RPRWidget.ConnectionSlotMovement() {
                    public void putInWorkSlot(int slotInInventory) {
                        putInAnvil(slotInInventory);
                    }
                    public void takeFromWorkSlot(int slotInWorkspace) {
                        moveToInventory(slotInWorkspace);
                    }
                },
                this::onFavoriteButtonUpdate,
                ghostCraft
        );

        if (config.openByDefault) {
            onToggleOpenRPRWidget();
        }
    }

    private void onFavoriteButtonUpdate(String name, Item item) {
        if (!name.isEmpty()) {
            favoriteButton.active = true;
            boolean favorite = FavoritesManager.isFavorite(item, name);
            favoriteButton.setFavorite(favorite);
        } else {
            favoriteButton.active = false;
        }
    }

    private void onToggleOpenRPRWidget() {
        rprWidget.toggleOpen();
        updateMenuShift();
        if (rprWidget.isOpen()) {
            nameField.setFocused(false);
            nameField.setFocusUnlocked(true);
        } else {
            nameField.setFocused(true);
            nameField.setFocusUnlocked(false);
        }
    }

    @Inject(at = @At("RETURN"), method = "onRenamed")
    private void newNameEntered(String name, CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        if (!rprWidget.init) return;
        rprWidget.nameUpdate(name);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AnvilScreen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "resize")
    private void onResize(AnvilScreen instance, MinecraftClient client, int width, int height) {
        if (!config.enableAnvilModification) {
            instance.init(client, width, height);
            return;
        }
        String tempSearchFieldText = rprWidget.searchField.getText();
        boolean prevOpen = rprWidget.isOpen();
        if (prevOpen) {
            onToggleOpenRPRWidget();
        }
        instance.init(client, width, height);
        if (prevOpen && !rprWidget.isOpen()) {
            onToggleOpenRPRWidget();
        }
        rprWidget.searchField.setText(tempSearchFieldText);
    }

    @Inject(at = @At(value = "HEAD"), method = "keyPressed")
    public void onKeyPressedHead(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!config.enableAnvilModification) return;
        afterPutInAnvilFirst = false;
        afterPutInAnvilSecond = false;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;isActive()Z"), method = "keyPressed")
    private boolean onKeyPressedNameFieldIsActive(TextFieldWidget instance, int keyCode, int scanCode, int modifiers) {
        if (!config.enableAnvilModification) return instance.isActive();
        return (rprWidget.keyPressed(keyCode, scanCode, modifiers) || rprWidget.searchField.isActive()) || instance.isActive();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!config.enableAnvilModification) return super.mouseClicked(mouseX, mouseY, button);
        afterPutInAnvilFirst = false;
        afterPutInAnvilSecond = false;
        if (opener.mouseClicked(mouseX, mouseY, button)) return true;
        if (favoriteButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (ghostCraft.mouseClicked(mouseX, mouseY, button)) {
            if (rprWidget.getCurrentItem().isEmpty()) {
                nameField.setText("");
                if (rprWidget.getCurrentTab() == RPRWidget.Tab.SEARCH || rprWidget.getCurrentTab() == RPRWidget.Tab.FAVORITE) {
                    rprWidget.screenUpdate();
                }
            }
        }
        if (rprWidget.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean stacksEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return stack1 == stack2;
        }
        if (!stack1.isOf(stack2.getItem())) return false;
        if (stack1.getCount() != stack2.getCount()) return false;
        if (stack1.getNbt() == null || stack2.getNbt() == null) {
            return stack1.getNbt() == stack2.getNbt();
        }
        return stack1.getNbt().toString().equals(stack2.getNbt().toString());
    }

    @Inject(at = @At("HEAD"), method = "onSlotUpdate", cancellable = true)
    private void itemUpdateHead(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        if (slotId != 0) return;

        /*
            Sometimes On server, after using putInAnvil() method client receives 3 changes on screen:
            1. Local changes on client side
            2. First packet from server, it tells what was in slot
            3. Second packet from server, it tells how slot actually changed (the same as local changes)

            So onSlotUpdate() method is called 3 times,
            and for rpr it looks like after moving stacks automatically
            player puts back previous stack in 0 slot and then puts new one again,
            what leads to changing tab to Search and resetting nameField.

            That's why there are a few fuses:
            1. First change is proceeded normally, the local one
            2. Second is ignored
            3. Third is ignored if it matches expected stack, the one that was places in slot 0 automatically.

            There is no such problem in singleplayer, so in singleplayer this is not proceeded.
        */
        if (config.fixDelayedPacketsChangingTab) {
            // Executing local changes normally and setting flag
            if (afterPutInAnvilFirst) {
                afterPutInAnvilFirst = false;
                afterPutInAnvilSecond = true;
                return;
            }

            // Ignoring first packet from server
            if (afterPutInAnvilSecond) {
                afterPutInAnvilSecond = false;
                ci.cancel();
                return;
            }
        }

        // Ignoring changes if stack did not change. Works for manual moving stacks too.
        if (stacksEqual(stack, rprWidget.getCurrentItem())) ci.cancel();
    }

    @Inject(at = @At("RETURN"), method = "onSlotUpdate")
    private void itemUpdateReturn(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        rprWidget.itemUpdate(slotId, stack);
    }

    private void updateMenuShift() {
        if (!config.offsetMenu) return;
        offsetX(menuShift * (rprWidget.isOpen() ? 1 : -1));
    }

    private void offsetX(int x) {
        if (client == null || client.currentScreen == null) return;

        ((AnvilScreen) client.currentScreen).x += x;

        nameField.setX(nameField.getX() + x);
        opener.setX(opener.getX() + x);
        favoriteButton.setX(favoriteButton.getX() + x);
        rprWidget.offsetX(x);
        ghostCraft.offsetX(x);
    }

    @Inject(at = @At("HEAD"), method = "drawForeground")
    private void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        if (client == null || client.currentScreen == null) return;

        int xScreenOffset = ((AnvilScreen) client.currentScreen).x;
        int yScreenOffset = ((AnvilScreen) client.currentScreen).y;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(-xScreenOffset, -yScreenOffset, 0);

        opener.render(context, mouseX, mouseY, 0);
        favoriteButton.render(context, mouseX, mouseY, 0);
        ghostCraft.render(context, mouseX, mouseY, 0);
        rprWidget.render(context, mouseX, mouseY, 0);

        matrices.pop();
    }

    private void putInAnvil(int slotInInventory) {
        if (client == null || client.player == null || client.interactionManager == null) return;
        int syncId = client.player.currentScreenHandler.syncId;

        if (
                config.fixDelayedPacketsChangingTab
                && !client.isInSingleplayer()
                && !rprWidget.getCurrentItem().isEmpty()
        ) afterPutInAnvilFirst = true;

        if (slotInInventory >= 9) {
            int i = slotInInventory - 9;
            i += 3;

            client.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, client.player);
        } else {
            client.interactionManager.clickSlot(syncId, 0, slotInInventory, SlotActionType.SWAP, client.player);
        }
    }

    private void moveToInventory(int slot) {
        assert (client != null ? client.player : null) != null;
        PlayerInventory inventory = client.player.getInventory();
        ItemStack stack = client.player.currentScreenHandler.slots.get(slot).getStack();
        if (!stack.isEmpty()) {
            int syncId = client.player.currentScreenHandler.syncId;
            assert client.interactionManager != null;
            if (inventory.getOccupiedSlotWithRoomForStack(stack) != -1 || inventory.getEmptySlot() != -1) {
                client.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.QUICK_MOVE, client.player);
                moveToInventory(slot);
            } else {
                client.interactionManager.clickSlot(syncId, slot, 99, SlotActionType.THROW, client.player);
            }
        }
    }
}