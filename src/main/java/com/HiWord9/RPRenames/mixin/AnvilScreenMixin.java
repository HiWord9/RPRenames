package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.accessor.AnvilScreenMixinAccessor;
import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.Rename;
import com.HiWord9.RPRenames.util.RenamesHelper;
import com.HiWord9.RPRenames.util.RenamesManager;
import com.HiWord9.RPRenames.util.Tabs;
import com.HiWord9.RPRenames.util.config.*;
import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.gui.GhostCraft;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.util.gui.MultiItemTooltipComponent.TooltipItem;
import com.HiWord9.RPRenames.util.gui.RenameButtonHolder;
import com.HiWord9.RPRenames.util.gui.button.*;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@Mixin(value = AnvilScreen.class, priority = 1200)
public abstract class AnvilScreenMixin extends Screen implements AnvilScreenMixinAccessor {
    private static final ModConfig config = ModConfig.INSTANCE;

    protected AnvilScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private TextFieldWidget nameField;

    boolean afterPutInAnvilFirst = false;
    boolean afterPutInAnvilSecond = false;

    boolean open;

    private static final Identifier MENU_TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/menu.png");

    final int menuTextureHeight = 166;
    final int menuWidth = 147;
    final int menuHeight = menuTextureHeight;
    final int menuXOffset = -1;
    final int tabOffsetY = 6;
    final int startTabOffsetY = 6;
    final int buttonOffsetY = 2;
    final int buttonXOffset = 10;

    final int highlightColor = config.getSlotHighlightRGBA();

    int page = 0;
    int maxPageElements = 5;
    int currentRenameListSize;

    final String nullItem = "air";
    String currentItem = nullItem;
    ItemStack stackInAnvil = ItemStack.EMPTY;
    boolean shouldNotUpdateTab = false;
    int tempPage;

    OpenerButton opener;

    TabButton searchTab;
    TabButton favoriteTab;
    TabButton inventoryTab;
    TabButton globalTab;

    FavoriteButton favoriteButton;
    RandomButton randomButton;

    TextFieldWidget searchField;

    PageButton pageDown;
    PageButton pageUp;

    Text pageCount = Text.empty();

    ArrayList<RenameButtonHolder> buttons = new ArrayList<>();

    Tabs currentTab = Tabs.SEARCH;

    GhostCraft ghostCraft = new GhostCraft();

    ArrayList<Rename> originalRenameList = new ArrayList<>();
    ArrayList<Rename> currentRenameList = new ArrayList<>();

    TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

    final int menuShift = 77;
    final int searchFieldXOffset = 23;
    final Text SEARCH_HINT_TEXT = Text.translatable("rprenames.gui.searchHintText").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);

    String searchTag = "";

    @Inject(at = @At("HEAD"), method = "setup")
    private void init(CallbackInfo ci) {
        if (!config.enableAnvilModification) return;

        assert client != null && client.currentScreen != null;
        int x = ((AnvilScreen) client.currentScreen).x;
        int y = ((AnvilScreen) client.currentScreen).y;

        int menuX = x - menuWidth + menuXOffset;

        int pageButtonsY = y + 140;
        if (config.viewMode == RenameButtonHolder.ViewMode.GRID) {
            pageButtonsY -= 4;
            maxPageElements = 20;
        }

        for (int i = 0; i < maxPageElements; i++) {
            buttons.add(new RenameButtonHolder(config.viewMode, i));
        }

        pageDown = new PageButton(menuX + buttonXOffset, pageButtonsY, PageButton.Type.DOWN);
        pageUp = new PageButton(x + menuXOffset - buttonXOffset - PageButton.BUTTON_WIDTH, pageButtonsY, PageButton.Type.UP);

        opener = new OpenerButton(x + 3, y + 44);

        int tabX = menuX - (TabButton.BUTTON_WIDTH - 3);
        int tabY = y + startTabOffsetY;
        searchTab = new TabButton(tabX, tabY, Tabs.SEARCH);
        favoriteTab = new TabButton(tabX, tabY + (TabButton.BUTTON_HEIGHT + tabOffsetY), Tabs.FAVORITE);
        inventoryTab = new TabButton(tabX, tabY + (TabButton.BUTTON_HEIGHT + tabOffsetY) * 2, Tabs.INVENTORY);
        globalTab = new TabButton(tabX, tabY + (TabButton.BUTTON_HEIGHT + tabOffsetY) * 4, Tabs.GLOBAL);

        favoriteButton = new FavoriteButton(x + 88 + config.favoritePosX, y + 83 + config.favoritePosY);
        randomButton = new RandomButton(x + menuXOffset - 14 - RandomButton.BUTTON_WIDTH, y + 14, randomNumber() % RandomButton.SIDES);

        searchField = new TextFieldWidget(renderer, menuX + searchFieldXOffset, y + 15, menuWidth - 53, 10, Text.of(""));
        searchField.setChangedListener(this::onSearch);
        searchField.setDrawsBackground(false);
        searchField.setMaxLength(1024);
    }

    @Inject(at = @At("TAIL"), method = "setup")
    private void initTail(CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        if (config.openByDefault) {
            openMenu();
        } else {
            screenUpdate();
        }
    }

    public void switchOpen() {
        if (!open) {
            openMenu();
        } else {
            closeMenu();
        }
    }

    public void addOrRemoveFavorite(boolean add) {
        addOrRemoveFavorite(add, nameField.getText(), getItemInFirstSlot());
    }

    public void addOrRemoveFavorite(boolean add, String favoriteName, String item) {
        if (!item.equals(nullItem)) {
            if (add) {
                FavoritesManager.addToFavorites(favoriteName, item);
            } else {
                FavoritesManager.removeFromFavorites(favoriteName, item);
            }
            favoriteButtonsUpdate(nameField.getText());
            if (open) {
                screenUpdate(page);
            }
        }
    }

    public void onPageDown() {
        if (hasShiftDown()) {
            page = 0;
        } else {
            page--;
        }
        updateWidgets();
        if (page == 0) {
            pageDown.active = false;
        }
        pageUp.active = true;
    }

    public void onPageUp() {
        if (hasShiftDown()) {
            page = ((currentRenameList.size() + maxPageElements - 1) / maxPageElements - 1);
        } else {
            page++;
        }
        updateWidgets();
        pageDown.active = true;
        if ((page + 1) * maxPageElements > currentRenameListSize - 1) {
            pageUp.active = false;
        }
    }

    public void setTab(Tabs tab) {
        if (tab == currentTab) return;
        currentTab = tab;
        screenUpdate();
    }

    public Tabs getCurrentTab() {
        return currentTab;
    }

    public void onRenameButton(int button, boolean favorite,
            int indexInInventory, boolean isInInventory,
            boolean asCurrentItem, PlayerInventory inventory,
            Rename rename,
            boolean enoughStackSize, boolean enoughDamage, boolean hasEnchant, boolean hasEnoughLevels) {
        if (button == 1 && !rename.getItems().isEmpty()) {
            if (currentTab == Tabs.SEARCH || currentTab == Tabs.FAVORITE || asCurrentItem) {
                addOrRemoveFavorite(
                        !favorite,
                        rename.getName(),
                        getItemInFirstSlot()
                );
            } else {
                if (favorite) {
                    for (String item : rename.getItems()) {
                        if (FavoritesManager.isFavorite(item, rename.getName())) {
                            FavoritesManager.removeFromFavorites(rename.getName(), item);
                        }
                    }
                    favoriteButtonsUpdate(nameField.getText());
                    if (open) {
                        screenUpdate(page);
                    }
                } else {
                    addOrRemoveFavorite(
                            true,
                            rename.getName(),
                            isInInventory ? getInventory().get(indexInInventory) : rename.getItems().get(0)
                    );
                }
            }

            return;
        }

        ghostCraft.reset();
        if (indexInInventory != 36 && isInInventory) {
            shouldNotUpdateTab = currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL;
            tempPage = page;
            if (!asCurrentItem) {
                putInAnvil(indexInInventory, MinecraftClient.getInstance());
            }
            shouldNotUpdateTab = false;
        } else if (indexInInventory != 36) {
            for (int s = 0; s < 2; s++) {
                moveToInventory(s, inventory);
            }

            ItemStack[] ghostCraftItems = RenamesHelper.getGhostCraftItems(rename);

            ghostCraft.setSlots(ghostCraftItems[0], ghostCraftItems[1], ghostCraftItems[2]);
            ghostCraft.setRender(true);
        } else {
            moveToInventory(1, inventory);
        }
        if (isInInventory) {
            if (!enoughStackSize || !enoughDamage) {
                ghostCraft.setForceRenderBG(true, null, true);
                ghostCraft.setRender(true);
            }
            if (!hasEnchant || !hasEnoughLevels) {
                ghostCraft.setSlots(ItemStack.EMPTY, RenamesHelper.getGhostCraftEnchant(rename), ItemStack.EMPTY);
                ghostCraft.setForceRenderBG(null, null, true);
                ghostCraft.setRender(true);
            }
        }
        nameField.setText(rename.getName());
    }

    public void chooseRandomRename() {
        int randomNumber = randomNumber();
        int randomSide = randomNumber % RandomButton.SIDES;
        randomButton.setSide(randomSide);
        if (currentRenameListSize < 1) return;
        int renameNumber = randomNumber % currentRenameListSize;
        page = renameNumber / maxPageElements;
        updateWidgets();
        buttons.get(renameNumber % maxPageElements).getButton().execute(0);
    }

    private int randomNumber() {
        assert client != null && client.player != null;
        return client.player.getRandom().nextBetween(0, Integer.MAX_VALUE - 1);
    }

    private void openMenu() {
        open = true;
        if (currentItem.equals(nullItem)) {
            currentTab = Tabs.GLOBAL;
        } else {
            currentTab = Tabs.SEARCH;
        }
        screenUpdate();
        updateMenuShift();
    }

    private void closeMenu() {
        open = false;
        searchField.setFocused(false);
        searchField.setFocusUnlocked(false);
        searchField.setText("");
        removeWidgets();
        remove(searchField);
        nameField.setFocused(true);
        nameField.setFocusUnlocked(false);
        opener.setOpen(open);
        currentTab = Tabs.SEARCH;
        updateMenuShift();
    }

    private void updateSelected() {
        for (RenameButtonHolder renameButtonHolder : buttons) {
            RenameButton button = renameButtonHolder.getButton();
            if (button == null) continue;
            button.setSelected(button.rename.getItems().contains(getItemInFirstSlot())
                    && button.rename.getName().equals(nameField.getText()));
        }
    }

    private void updateWidgets() {
        defineButtons();
        updateSelected();
        showButtons();
        updatePageWidgets();
    }

    private void screenUpdate() {
        screenUpdate(0);
    }

    private void screenUpdate(int savedPage) {
        page = savedPage;
        opener.active = true;
        if (shouldNotUpdateTab) {
            page = tempPage;
        }
        calcRenameList();

        if (open) {
            removeWidgets();
            updateSearchRequest(page);
            opener.setOpen(open);
            addDrawableChild(searchField);
            searchField.setFocusUnlocked(true);
            nameField.setFocused(false);
            nameField.setFocusUnlocked(true);
        }
    }

    private void calcRenameList() {
        switch (currentTab) {
            case SEARCH -> originalRenameList = RenamesManager.getRenames(getItemInFirstSlot());
            case FAVORITE -> originalRenameList = FavoritesManager.getFavorites(getItemInFirstSlot());
            case INVENTORY -> {
                ArrayList<String> currentInvList = getInventory();
                ArrayList<String> checked = new ArrayList<>();
                ArrayList<Rename> names = new ArrayList<>();
                for (String item : currentInvList) {
                    if (!item.equals(nullItem) && !checked.contains(item)) {
                        checked.add(item);
                        ArrayList<Rename> renames = RenamesManager.getRenames(item);
                        for (Rename r : renames) {
                            if (!names.contains(r)) names.add(r);
                        }
                    }
                }
                originalRenameList = names;
            }
            case GLOBAL -> originalRenameList = RenamesManager.getAllRenames();
        }
    }

    private void updateSearchRequest() {
        updateSearchRequest(0);
    }

    private void updateSearchRequest(int page) {
        hideButtons();
        currentRenameList = RenamesHelper.search(originalRenameList, searchTag);

        this.page = page;
        if (this.page >= (currentRenameList.size() + maxPageElements - 1) / maxPageElements) {
            this.page = ((currentRenameList.size() + maxPageElements - 1) / maxPageElements) - 1;
            if (this.page == -1) {
                this.page = 0;
            }
        }
        currentRenameListSize = currentRenameList.size();

        updateWidgets();
    }

    private String getItemInFirstSlot() {
        String item = currentItem;
        if (item.equals(nullItem) && !ghostCraft.slot1.isEmpty()) {
            item = ParserHelper.getIdAndPath(ghostCraft.slot1.getItem());
        }
        return item;
    }

    @Inject(at = @At("RETURN"), method = "onRenamed")
    private void newNameEntered(String name, CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        favoriteButtonsUpdate(name);
        updateSelected();
    }

    private void favoriteButtonsUpdate(String name) {
        if (!name.isEmpty()) {
            favoriteButton.active = true;
            boolean favorite = FavoritesManager.isFavorite(getItemInFirstSlot(), name);
            favoriteButton.setFavorite(favorite);
        } else {
            favoriteButton.active = false;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AnvilScreen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "resize")
    private void onResize(AnvilScreen instance, MinecraftClient client, int width, int height) {
        if (!config.enableAnvilModification) {
            instance.init(client, width, height);
            return;
        }
        buttons.clear();
        String tempSearchFieldText = searchField.getText();
        boolean prevOpen = open;
        if (prevOpen) {
            open = false;
            updateMenuShift();
        }
        instance.init(client, width, height);
        if (prevOpen && !open) {
            open = true;
            updateMenuShift();
            opener.setOpen(true);
        }
        searchField.setText(tempSearchFieldText);
    }

    @Inject(at = @At(value = "HEAD"), method = "keyPressed")
    public void onKeyPressedHead(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        afterPutInAnvilFirst = false;
        afterPutInAnvilSecond = false;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;isActive()Z"), method = "keyPressed")
    private boolean onKeyPressedNameFieldIsActive(TextFieldWidget instance, int keyCode, int scanCode, int modifiers) {
        if (!config.enableAnvilModification) return instance.isActive();
        searchField.keyPressed(keyCode, scanCode, modifiers);
        return instance.isActive() || searchField.isActive();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!config.enableAnvilModification) return super.mouseClicked(mouseX, mouseY, button);
        afterPutInAnvilFirst = false;
        afterPutInAnvilSecond = false;
        assert client != null && client.currentScreen != null;
        int xScreenOffset = ((AnvilScreen) client.currentScreen).x;
        int yScreenOffset = ((AnvilScreen) client.currentScreen).y;
        if (ghostCraft.doRender) {
            if ((mouseX - xScreenOffset >= 26 && mouseX - xScreenOffset <= 151) && (mouseY - yScreenOffset >= 46 && mouseY - yScreenOffset <= 64)) {
                ghostCraft.reset();
                if (currentItem.equals(nullItem)) {
                    nameField.setText("");
                    if (currentTab == Tabs.SEARCH || currentTab == Tabs.FAVORITE) {
                        screenUpdate();
                    }
                }
            }
        }
        if (open) {
            for (RenameButtonHolder renameButtonHolder : buttons) {
                if (!renameButtonHolder.isActive()) continue;
                renameButtonHolder.getButton().mouseClicked(mouseX, mouseY, button);
            }
            pageDown.mouseClicked(mouseX, mouseY, button);
            pageUp.mouseClicked(mouseX, mouseY, button);

            searchTab.mouseClicked(mouseX, mouseY, button);
            favoriteTab.mouseClicked(mouseX, mouseY, button);
            inventoryTab.mouseClicked(mouseX, mouseY, button);
            globalTab.mouseClicked(mouseX, mouseY, button);

            randomButton.mouseClicked(mouseX, mouseY, button);
        }

        opener.mouseClicked(mouseX, mouseY, button);
        favoriteButton.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    ArrayList<String> invChangeHandler = new ArrayList<>();

    @Inject(at = @At("RETURN"), method = "drawBackground")
    private void onDrawBackground(CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        if (invChangeHandler.isEmpty()) {
            invChangeHandler = getInventory();
            return;
        }
        ArrayList<String> temp = getInventory();
        int i = 0;
        boolean equal = true;
        while (i < temp.size() || i < invChangeHandler.size()) {
            if (invChangeHandler.size() > i && temp.size() > i) {
                if (!invChangeHandler.get(i).equals(temp.get(i))) {
                    equal = false;
                    break;
                }
            } else {
                equal = false;
                break;
            }
            i++;
        }
        if (!equal) {
            invChangeHandler = temp;
            if (open) {
                screenUpdate(page);
            }
        }
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
        if (stacksEqual(stack, stackInAnvil)) ci.cancel();
    }

    @Inject(at = @At("RETURN"), method = "onSlotUpdate")
    private void itemUpdateReturn(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        if (slotId == 0 || slotId == 1) {
            ghostCraft.reset();
        }
        if (slotId != 0) return;
        stackInAnvil = stack.copy();
        if (stack.isEmpty()) {
            currentItem = nullItem;
            searchField.setFocusUnlocked(false);
            remove(searchField);
            searchField.setFocused(false);
        } else {
            currentItem = ParserHelper.getIdAndPath(stack.getItem());
            searchField.setFocusUnlocked(true);
            if (!shouldNotUpdateTab) currentTab = Tabs.SEARCH;
        }
        if (!open || currentTab != Tabs.GLOBAL) {
            screenUpdate();
        } else {
            updateSearchRequest(page);
        }
    }

    private ArrayList<String> getInventory() {
        ArrayList<String> inventoryList = new ArrayList<>();
        assert MinecraftClient.getInstance().player != null;
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        for (ItemStack itemStack : inventory.main) {
            inventoryList.add(ParserHelper.getIdAndPath(itemStack.getItem()));
        }
        inventoryList.add(currentItem);
        return inventoryList;
    }

    private void updateMenuShift() {
        if (!config.offsetMenu) return;
        offsetX(menuShift * (open ? 1 : -1));
    }

    private void offsetX(int x) {
        if (client == null || client.currentScreen == null) return;

        ((AnvilScreen) client.currentScreen).x += x;

        nameField.setX(nameField.getX() + x);
        opener.setX(opener.getX() + x);
        searchTab.setX(searchTab.getX() + x);
        favoriteTab.setX(favoriteTab.getX() + x);
        inventoryTab.setX(inventoryTab.getX() + x);
        globalTab.setX(globalTab.getX() + x);
        favoriteButton.setX(favoriteButton.getX() + x);
        randomButton.setX(randomButton.getX() + x);
        searchField.setX(searchField.getX() + x);
        pageDown.setX(pageDown.getX() + x);
        pageUp.setX(pageUp.getX() + x);

        for (RenameButtonHolder renameButtonHolder : buttons) {
            RenameButton button = renameButtonHolder.getButton();
            if (button == null) continue;
            button.setX(button.getX() + x);
        }
    }

    @Inject(at = @At("HEAD"), method = "drawForeground")
    private void frameUpdate(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (!config.enableAnvilModification) return;
        if (client == null || client.currentScreen == null) return;

        int xScreenOffset = ((AnvilScreen) client.currentScreen).x;
        int yScreenOffset = ((AnvilScreen) client.currentScreen).y;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(-xScreenOffset, -yScreenOffset, 0);
        opener.render(context, mouseX, mouseY, 0);
        favoriteButton.render(context, mouseX, mouseY, 0);
        matrices.pop();
        if (!open) {
            searchField.setFocused(false);
            return;
        }
        RenderSystem.enableDepthTest();
        context.drawTexture(MENU_TEXTURE, -menuWidth + menuXOffset, 0, 0, 0, 0, menuWidth, menuHeight, menuWidth, menuHeight);
        matrices.push();
        matrices.translate(-xScreenOffset, -yScreenOffset, 0);
        for (RenameButtonHolder renameButtonHolder : buttons) {
            if (renameButtonHolder.isActive()) {
                renameButtonHolder.getButton().render(context, mouseX, mouseY, 0);
                renameButtonHolder.drawElements(context);
            }
        }
        searchTab.render(context, mouseX, mouseY, 0);
        favoriteTab.render(context, mouseX, mouseY, 0);
        inventoryTab.render(context, mouseX, mouseY, 0);
        globalTab.render(context, mouseX, mouseY, 0);

        pageDown.render(context, mouseX, mouseY, 0);
        pageUp.render(context, mouseX, mouseY, 0);

        randomButton.render(context, mouseX, mouseY, 0);
        matrices.pop();

        Graphics.renderText(context, pageCount, menuWidth / -2 + menuXOffset, (config.viewMode == RenameButtonHolder.ViewMode.GRID ? 140 : 144), false, true);
        if (searchField != null && !searchField.isFocused() && searchField.getText().isEmpty()) {
            Graphics.renderText(context, SEARCH_HINT_TEXT, -1, -menuWidth + searchFieldXOffset, searchFieldXOffset - 8, true, false);
        }
        ghostCraft.render(context, mouseX - xScreenOffset, mouseY - yScreenOffset);

        if (currentRenameList.isEmpty()) {
            String key;
            if (getItemInFirstSlot().equals(nullItem) && (currentTab == Tabs.FAVORITE || currentTab == Tabs.SEARCH)) {
                key = "putItem";
            } else {
                key = currentTab == Tabs.FAVORITE ? "noFavoriteRenamesFound" : "noRenamesFound";
            }
            Graphics.renderText(context, Text.translatable("rprenames.gui." + key).copy().fillStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY)), -1, (-menuWidth + menuXOffset) / 2, 37, true, true);
        } else {
            for (RenameButtonHolder renameButtonHolder : buttons) {
                if (renameButtonHolder.getButton() != null && renameButtonHolder.getButton().isMouseOver(mouseX, mouseY) && renameButtonHolder.isActive()) {
                    ArrayList<TooltipComponent> tooltip = new ArrayList<>(renameButtonHolder.getTooltip());
                    if (!renameButtonHolder.isCEM() && config.enablePreview) {
                        if (!hasShiftDown() && !config.playerPreviewByDefault) {
                            if (!config.disablePlayerPreviewTips) {
                                tooltip.add(TooltipComponent.of(
                                        Text.translatable("rprenames.gui.tooltipHint.playerPreviewTip.holdShift")
                                                .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true))
                                                .asOrderedText()));
                            }
                        } else if (hasShiftDown() != config.playerPreviewByDefault) {
                            searchField.setFocused(false);
                            nameField.setFocused(false);
                            if (!config.disablePlayerPreviewTips) {
                                tooltip.add(TooltipComponent.of(
                                        Text.translatable("rprenames.gui.tooltipHint.playerPreviewTip.pressF")
                                                .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true))
                                                .asOrderedText()));
                            }
                        }
                    }
                    if ((currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) && (config.slotHighlightColorALPHA > 0 && config.highlightSlot)) {
                        renameButtonHolder.highlightSlot(context, getInventory(), currentItem, highlightColor);
                    }
                    matrices.push();
                    matrices.translate(-xScreenOffset, -yScreenOffset, 0);
                    Graphics.drawTooltip(
                            context,
                            MinecraftClient.getInstance().textRenderer,
                            tooltip,
                            mouseX, mouseY,
                            HoveredTooltipPositioner.INSTANCE
                    );
                    if (config.enablePreview) {
                        renameButtonHolder.drawPreview(
                                context,
                                mouseX, mouseY,
                                52, 52,
                                config.scaleFactorItem, config.scaleFactorEntity
                        );
                    }
                    matrices.pop();
                }
            }
        }
        RenderSystem.disableDepthTest();
    }

    private void showButtons() {
        for (RenameButtonHolder renameButtonHolder : buttons) {
            int orderOnPage = renameButtonHolder.getOrderOnPage();
            if (orderOnPage + page * maxPageElements <= currentRenameListSize - 1) {
                renameButtonHolder.setActive(true);
            }
        }
    }

    private void hideButtons() {
        for (RenameButtonHolder renameButtonHolder : buttons) {
            renameButtonHolder.setActive(false);
        }
    }

    private void removeWidgets() {
        hideButtons();
        pageCount = Text.empty();
    }

    private void putInAnvil(int slotInInventory, MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        int syncId = client.player.currentScreenHandler.syncId;

        if (
                config.fixDelayedPacketsChangingTab
                && !client.isInSingleplayer()
                && !stackInAnvil.isEmpty()
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

    public void moveToInventory(int slot, PlayerInventory inventory) {
        assert (client != null ? client.player : null) != null;
        ItemStack stack = client.player.currentScreenHandler.slots.get(slot).getStack();
        if (!stack.isEmpty()) {
            int syncId = client.player.currentScreenHandler.syncId;
            assert client.interactionManager != null;
            if (inventory.getOccupiedSlotWithRoomForStack(stack) != -1 || inventory.getEmptySlot() != -1) {
                client.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.QUICK_MOVE, client.player);
                moveToInventory(slot, inventory);
            } else {
                client.interactionManager.clickSlot(syncId, slot, 99, SlotActionType.THROW, client.player);
            }
        }
    }

    private void createButton(int orderOnPage, Rename rename) {
        if (MinecraftClient.getInstance().player == null) return;
        PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
        ArrayList<String> inventory = getInventory();
        String item;
        if (currentTab == Tabs.SEARCH) {
            item = getItemInFirstSlot();
        } else {
            item = rename.getItems().get(0);
            for (String s : rename.getItems()) {
                if (inventory.contains(s)) {
                    item = s;
                    break;
                }
            }
        }
        boolean asCurrentItem = item.equals(getItemInFirstSlot());
        boolean isInInventory = inventory.contains(item);
        int indexInInventory = inventory.indexOf(item);
        boolean favorite = false;
        if (currentTab != Tabs.SEARCH) {
            for (String s : rename.getItems()) {
                if (FavoritesManager.isFavorite(s, rename.getName())) {
                    favorite = true;
                    break;
                }
            }
        } else {
            favorite = FavoritesManager.isFavorite(item, rename.getName());
        }

        ArrayList<Object> tooltip = new ArrayList<>();
        tooltip.add(Text.of(rename.getName()));

        if (rename.getDescription() != null && config.showDescription) {
            ArrayList<Text> lines = PropertiesHelper.parseCustomDescription(rename.getDescription());
            tooltip.addAll(lines);
        }

        if ((currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL)) {
            ArrayList<TooltipItem> tooltipItems = new ArrayList<>();
            for (int i = 0; i < rename.getItems().size(); i++) {
                ItemStack itemStack = RenamesHelper.createItem(rename, false, i);
                tooltipItems.add(new TooltipItem(itemStack, inventory.contains(rename.getItems().get(i))));
            }
            tooltip.add(new MultiItemTooltipComponent(tooltipItems));
        }

        if (item.equals(ParserHelper.getIdAndPath(Items.NAME_TAG)) && rename.isCEM()) {
            Identifier mob = new Identifier(rename.getMob().entity());
            var entityType = Registries.ENTITY_TYPE.get(mob);
            tooltip.add(Text.translatable(entityType.getTranslationKey()).copy().fillStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
        }

        boolean enoughStackSize = true;
        boolean enoughDamage = true;
        boolean hasEnchant = false;
        boolean hasEnoughLevels = false;

        if (rename.getStackSize() != null && rename.getStackSize() > 1) {
            if (!(currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) || asCurrentItem) {
                enoughStackSize = PropertiesHelper.matchesRange(stackInAnvil.getCount(), rename.getOriginalStackSize());
            } else if (isInInventory) {
                enoughStackSize = PropertiesHelper.matchesRange(playerInventory.main.get(indexInInventory).getCount(), rename.getOriginalStackSize());
            }

            if (config.showExtraProperties) {
                if (config.showOriginalProperties) {
                    tooltip.add(Text.of("stackSize").copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                            .append(Text.of("=").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.of(rename.getOriginalStackSize()).copy().fillStyle(Style.EMPTY.withColor(enoughStackSize ? Formatting.GREEN : Formatting.DARK_RED))));
                } else {
                    tooltip.add(Text.of(Text.translatable("rprenames.gui.tooltipHint.stackSize").getString() + " " + rename.getStackSize()).copy().fillStyle(Style.EMPTY.withColor(enoughStackSize ? Formatting.GRAY : Formatting.DARK_RED)));
                }
            }
        }

        if (rename.getDamage() != null && rename.getDamage().damage > 0) {
            if (!(currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) || asCurrentItem) {
                enoughDamage = PropertiesHelper.matchesRange(stackInAnvil.getDamage(), rename.getOriginalDamage(), item);
            } else if (isInInventory) {
                enoughDamage = PropertiesHelper.matchesRange(playerInventory.main.get(indexInInventory).getDamage(), rename.getOriginalDamage(), item);
            }

            if (config.showExtraProperties) {
                if (config.showOriginalProperties) {
                    tooltip.add(Text.of("damage").copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                            .append(Text.of("=").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.of(rename.getOriginalDamage()).copy().fillStyle(Style.EMPTY.withColor(enoughDamage ? Formatting.GREEN : Formatting.DARK_RED))));
                } else {
                    tooltip.add(Text.of(Text.translatable("rprenames.gui.tooltipHint.damage").getString() + " " +
                                    rename.getDamage().damage + (rename.getDamage().percent ? "%" : ""))
                            .copy().fillStyle(Style.EMPTY.withColor(enoughDamage ? Formatting.GRAY : Formatting.DARK_RED)));
                }
            }
        }

        if (rename.getEnchantment() == null) {
            hasEnchant = true;
            hasEnoughLevels = true;
        } else {
            Map<Enchantment, Integer> enchantments = Maps.newLinkedHashMap();

            if (!(currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) || asCurrentItem) {
                enchantments = EnchantmentHelper.fromNbt(stackInAnvil.getEnchantments());
            } else if (isInInventory) {
                enchantments = EnchantmentHelper.fromNbt(playerInventory.main.get(indexInInventory).getEnchantments());
            }

            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                String enchantName = rename.getEnchantment();
                if (!enchantName.contains(":")) {
                    enchantName = Identifier.DEFAULT_NAMESPACE + Identifier.NAMESPACE_SEPARATOR + enchantName;
                }
                if (Objects.requireNonNull(Registries.ENCHANTMENT.getId(enchantment)).toString().equals(enchantName)) {
                    hasEnchant = true;
                    if (PropertiesHelper.matchesRange(entry.getValue(), rename.getOriginalEnchantmentLevel())) {
                        hasEnoughLevels = true;
                        break;
                    }
                }
            }
            if (currentTab == Tabs.GLOBAL && !isInInventory) {
                hasEnchant = true;
                hasEnoughLevels = true;
            }
            if (config.showExtraProperties) {
                if (config.showOriginalProperties) {
                    tooltip.add(Text.of("enchantmentIDs").copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                            .append(Text.of("=").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.of(rename.getOriginalEnchantment()).copy().fillStyle(Style.EMPTY.withColor(hasEnchant ? Formatting.GREEN : Formatting.DARK_RED))));
                    if (rename.getOriginalEnchantmentLevel() != null) {
                        tooltip.add(Text.of("enchantmentLevels").copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                                .append(Text.of("=").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                .append(Text.of(rename.getOriginalEnchantmentLevel()).copy().fillStyle(Style.EMPTY.withColor(hasEnoughLevels ? Formatting.GREEN : Formatting.DARK_RED))));
                    }
                } else {
                    Identifier enchant = Identifier.splitOn(rename.getEnchantment(), ':');
                    String namespace = enchant.getNamespace();
                    String path = enchant.getPath();
                    Text translatedEnchant = Text.translatable("enchantment." + namespace + "." + path);
                    Text translatedEnchantLevel = Text.translatable("enchantment.level." + rename.getEnchantmentLevel());
                    tooltip.add(Text.of(Text.translatable("rprenames.gui.tooltipHint.enchantment").getString() + " " + translatedEnchant.getString() + " " + translatedEnchantLevel.getString()).copy().fillStyle(Style.EMPTY.withColor(hasEnchant && hasEnoughLevels ? Formatting.GRAY : Formatting.DARK_RED)));
                }
            }
        }

        if (config.showPackName && rename.getPackName() != null) {
            String packName = rename.getPackName();
            if (packName.endsWith(".zip")) {
                tooltip.add(Text.of(packName.substring(0, packName.length() - 4))
                        .copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                        .append(Text.of(".zip").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                );
            } else {
                tooltip.add(Text.of(packName).copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD)));
            }
        }
        if (config.showNbtDisplayName && currentTab != Tabs.FAVORITE && rename.getOriginalNbtDisplayName() != null) {
            tooltip.add(Text.of("nbt.display.Name=" + rename.getOriginalNbtDisplayName()).copy().fillStyle(Style.EMPTY.withColor(Formatting.BLUE)));
        }

        int x;
        int y;
        assert client != null && client.currentScreen != null;
        int menuX = ((AnvilScreen) client.currentScreen).x - menuWidth + menuXOffset;
        int buttonX = menuX + buttonXOffset;
        int buttonY = ((AnvilScreen) client.currentScreen).y + 30;
        if (config.viewMode == RenameButtonHolder.ViewMode.LIST) {
            x = buttonX;
            y = buttonY + (orderOnPage * (RenameButton.BUTTON_HEIGHT_LIST + buttonOffsetY));
        } else {
            x = buttonX + 1 + (orderOnPage % 5 * RenameButton.BUTTON_WIDTH_GRID);
            y = buttonY + 1 + (orderOnPage / 5 * RenameButton.BUTTON_HEIGHT_GRID);
        }

        ArrayList<TooltipComponent> tooltipComponents = new ArrayList<>();
        for (Object o : tooltip) {
            if (o instanceof Text) {
                tooltipComponents.add(TooltipComponent.of(((Text) o).asOrderedText()));
            } else if (o instanceof TooltipComponent tooltipComponent) {
                tooltipComponents.add(tooltipComponent);
            }
        }

        RenameButton renameButton = new RenameButton(x, y, config.viewMode, favorite,
                indexInInventory, isInInventory, asCurrentItem,
                playerInventory, rename,
                enoughStackSize, enoughDamage,
                hasEnchant, hasEnoughLevels);

        buttons.get(orderOnPage).setParameters(renameButton, rename, page, tooltipComponents);
    }

    private void defineButtons() {
        hideButtons();
        for (int n = 0; n < maxPageElements; n++) {
            if (n + page * maxPageElements <= currentRenameListSize - 1) {
                createButton(n, currentRenameList.get(n + page * maxPageElements));
            }
        }
    }

    private void updatePageWidgets() {
        pageDown.active = page > 0;
        pageUp.active = (page + 1) * maxPageElements <= currentRenameListSize - 1;
        pageCount = Text.of(page + 1 + "/" + (currentRenameList.size() + maxPageElements - 1) / maxPageElements);
    }

    private void onSearch(String search) {
        searchTag = search;
        if (open) {
            updateSearchRequest();
        }
    }
}