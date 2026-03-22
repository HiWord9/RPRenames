package com.hiword9.rprenames.mod.mixin;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.RPRInteractableScreen;
import com.hiword9.rprenames.mod.gui.widget.GhostCraft;
import com.hiword9.rprenames.mod.gui.widget.Offsetable;
import com.hiword9.rprenames.mod.gui.widget.OffsetableWidget;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget;
import com.hiword9.rprenames.mod.gui.widget.external.FavoriteButton;
import com.hiword9.rprenames.mod.gui.widget.external.OpenerButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static com.hiword9.rprenames.mod.util.Util.*;

@Mixin(value = AnvilScreen.class, priority = 1200)
public abstract class AnvilScreenMixin extends Screen implements RPRInteractableScreen, Offsetable {
    protected AnvilScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private TextFieldWidget nameField;

    boolean afterPutInAnvilFirst = false;
    boolean afterPutInAnvilSecond = false;

    private static final int MENU_SHIFT = 77;

    RPRWidget rprWidget = new RPRWidget();

    GhostCraft ghostCraft;
    OpenerButton opener;
    FavoriteButton favoriteButton;

    List<Drawable> widgets = new ArrayList<>();

    boolean init = false;

    @Inject(at = @At("TAIL"), method = "setup")
    private void init(CallbackInfo ci) {
        if (shouldNotModify() || init) return;
        init = true;

        assert client != null && client.currentScreen != null;
        int x = ((AnvilScreen) client.currentScreen).x;
        int y = ((AnvilScreen) client.currentScreen).y;

        opener = new OpenerButton(rprWidget, x + 3, y + 44);
        favoriteButton = new FavoriteButton(rprWidget, x, y, config().favoriteButtonPosition);

        var slots = ((AnvilScreen) client.currentScreen).getScreenHandler().slots;
        ghostCraft = new GhostCraft(
                new GhostCraft.GhostSlot(x + slots.get(0).x - 1, y + slots.get(0).y - 1),
                new GhostCraft.GhostSlot(x + slots.get(1).x - 1, y + slots.get(1).y - 1),
                new GhostCraft.GhostSlot(x + slots.get(2).x - 1, y + slots.get(2).y - 1)
        );

        RPRInteractableScreen rprInteractableScreen = null;
        if (client.currentScreen instanceof RPRInteractableScreen screen) {
            rprInteractableScreen = screen;
        }

        rprWidget.init(
                x - RPRWidget.WIDGET_WIDTH - 1, y,
                rprInteractableScreen,
                RPRenames.renamesProvider,
                RPRenames.favoritesManager,
                nameField,
                favoriteButton,
                ghostCraft
        );

        widgets.addAll(List.of(
                opener, favoriteButton,
                ghostCraft, rprWidget
        ));

        if (config().openByDefault) opener.execute();
    }

    @Inject(at = @At("RETURN"), method = "onRenamed")
    private void newNameEntered(CallbackInfo ci) {
        if (shouldNotModify() || !init) return;
        rprWidget.updatedName();
    }

    @Redirect(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/AnvilScreen;init(Lnet/minecraft/client/MinecraftClient;II)V"),
            method = "resize"
    )
    private void onResize(AnvilScreen instance, MinecraftClient client, int width, int height) {
        if (shouldNotModify()) {
            instance.init(client, width, height);
            return;
        }

        int prevX = instance.x;
        int prevY = instance.y;

        instance.init(client, width, height);

        offsetWidgets(instance.x - prevX, instance.y - prevY);

        if (rprWidget.isOpen()) updateMenuShift();
    }

    @Inject(at = @At(value = "HEAD"), method = "keyPressed")
    public void onKeyPressedHead(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (shouldNotModify()) return;
        afterPutInAnvilFirst = false;
        afterPutInAnvilSecond = false;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;isActive()Z"), method = "keyPressed")
    private boolean onKeyPressedNameFieldIsActive(TextFieldWidget instance, KeyInput input) {
        if (shouldNotModify()) return instance.isActive();

        var widgetAccepted = false;
        for (var w : widgets) if (w instanceof Element element) {
            if (element.keyPressed(input)) {
                widgetAccepted = true;
                break;
            }
        }
        return widgetAccepted
                || rprWidget.searchField.isActive()
                || instance.isActive();
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        if (!shouldNotModify()) {
            for (var w : widgets) if (w instanceof Element element) {
                if (element.keyReleased(input))
                    return true;
            }
        }
        return super.keyReleased(input);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!shouldNotModify()) {
            afterPutInAnvilFirst = false;
            afterPutInAnvilSecond = false;

            for (var w : widgets) if (w instanceof Element element) {
                if (element.mouseClicked(click, doubled)) {
                    if (element == ghostCraft) {
                        if (rprWidget.getActiveItemStack().isEmpty()) {
                            nameField.setText("");
                            if (rprWidget.getCurrentTab().forCraftItemOnly) {
                                rprWidget.resetPageContent();
                            }
                        }
                        continue;
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (!shouldNotModify()) {
            for (var w : widgets) if (w instanceof Element element) {
                element.mouseMoved(mouseX, mouseY);
            }
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (!shouldNotModify()) {
            for (var w : widgets) if (w instanceof Element element) {
                if (element.mouseDragged(click, offsetX, offsetY))
                    return true;
            }
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!shouldNotModify()) {
            for (var w : widgets) if (w instanceof Element element) {
                if (element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                    return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Inject(at = @At("HEAD"), method = "onSlotUpdate", cancellable = true)
    private void itemUpdateHead(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
        if (shouldNotModify()) return;
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
        if (config().fixDelayedPacketsChangingTab) {
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
        if (ItemStack.areEqual(stack, rprWidget.getActiveItemStack())) ci.cancel();
    }

    @Inject(at = @At("RETURN"), method = "onSlotUpdate")
    private void itemUpdateReturn(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
        if (shouldNotModify()) return;
        rprWidget.updatedItem(slotId, stack);
    }

    @Override
    public void updateMenuShift() {
        if (!config().offsetMenu) return;
        offsetX(MENU_SHIFT * (rprWidget.isOpen() ? 1 : -1));
    }

    @Override
    public void offset(int x, int y) {
        var screen = (AnvilScreen) (Object) this;
        screen.x += x;
        screen.y += y;

        OffsetableWidget.offset(nameField, x, y);
        offsetWidgets(x, y);
    }

    private void offsetWidgets(int x, int y) {
        for (var w : widgets) if (w instanceof Offsetable offsetable) {
            offsetable.offset(x, y);
        }
    }

    @Inject(at = @At("HEAD"), method = "drawForeground")
    private void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (shouldNotModify()) return;
        if (client == null || client.currentScreen == null) return;

        int xScreenOffset = ((AnvilScreen) client.currentScreen).x;
        int yScreenOffset = ((AnvilScreen) client.currentScreen).y;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(-xScreenOffset, -yScreenOffset);

        for (var drawable : widgets) {
            drawable.render(context, mouseX, mouseY, 0);
        }

        matrices.popMatrix();
    }

    @Override
    public void moveToCraft(int inventorySlot, int craftSlot) {
        if (client == null) return;

        if (
                config().fixDelayedPacketsChangingTab
                        && !client.isInSingleplayer()
                        && !rprWidget.getActiveItemStack().isEmpty()
        ) afterPutInAnvilFirst = true;

        RPRInteractableScreen.super.moveToCraft(inventorySlot, craftSlot);
    }

    @Override
    public int getCraftSlotsAmount() {
        return 3;
    }

    private boolean shouldNotModify() {
        return !config().enableAnvilModification;
    }
}