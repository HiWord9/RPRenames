package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.Tabs;
import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import com.HiWord9.RPRenames.util.gui.GhostCraft;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.RenameButton;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends Screen {

    private static final ModConfig config = ModConfig.INSTANCE;

    protected AnvilScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private TextFieldWidget nameField;

    boolean open = config.openByDefault;

    private static final Identifier MENU_TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/menu.png");
    int menuTextureWidth = 213;
    int menuTextureHeight = 166;
    int menuWidth = 147;
    int menuHeight = menuTextureHeight;
    int menuXOffset = -1;
    int tabWidth = 33;
    int tabHeight = 26;
    int tabOffsetY = 6;
    int startTabOffsetY = 6;
    private static final Identifier OPENER_TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/opener.png");
    int openerTextureWidth = 22;
    int openerTextureHeight = 88;
    int openerWidth = 22;
    int openerHeight = 22;
    private static final Identifier BUTTON_TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/button.png");
    int buttonTextureWidth = 127;
    int buttonTextureHeight = 80;
    int buttonWidth = buttonTextureWidth;
    int buttonHeight = 20;
    int buttonOffsetY = 2;
    private static final Identifier BUTTON_TEXTURE_GRID = new Identifier(RPRenames.MOD_ID, "textures/gui/button_grid.png");
    int buttonGridTextureWidth = 50;
    int buttonGridTextureHeight = 50;
    int buttonGridWidth = 25;
    int buttonGridHeight = 25;
    private static final Identifier PAGE_ARROWS_TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/page_arrows.png");
    int pageArrowsTextureWidth = 60;
    int pageArrowsTextureHeight = 48;
    int pageArrowsWidth = 30;
    int pageArrowsHeight = 16;
    private static final Identifier FAVORITE_BUTTON_TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/favorite_button.png");
    int favoriteButtonTextureWidth = 9;
    int favoriteButtonTextureHeight = 18;
    int favoriteButtonWidth = 9;
    int favoriteButtonHeight = 9;

    int buttonXOffset = 10;

    int highlightColor = config.getSlotHighlightRGBA();

    final int backgroundWidth = Graphics.backgroundWidth;
    final int backgroundHeight = Graphics.backgroundHeight;

    int page = 0;
    int maxPageElements = 5;
    int currentRenameListSize;

    String currentItem = "air";
    ItemStack itemAfterUpdate;
    boolean afterInventoryTab = false;
    boolean afterGlobalTab = false;
    int tempPage;

    TexturedButtonWidget opener;
    TexturedButtonWidget openerOpened;

    ArrayList<RenameButton> buttons = new ArrayList<>();

    TexturedButtonWidget searchTab;
    TexturedButtonWidget favoriteTab;
    TexturedButtonWidget inventoryTab;
    TexturedButtonWidget globalTab;
    Tabs currentTab = Tabs.SEARCH;

    TexturedButtonWidget addToFavorite;
    TexturedButtonWidget removeFromFavorite;

    TexturedButtonWidget pageDown;
    TexturedButtonWidget pageUp;
    Text pageCount = Text.empty();

    GhostCraft ghostCraft = new GhostCraft();

    ArrayList<Rename> originalRenameList = new ArrayList<>();
    ArrayList<Rename> currentRenameList = new ArrayList<>();

    TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

    TextFieldWidget searchField;
    int searchFieldXOffset = 23;
    Text SEARCH_HINT_TEXT = Text.translatable("rprenames.gui.searchHintText").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);

    String searchTag = "";

    @Inject(at = @At("RETURN"), method = "setup")
    private void init(CallbackInfo ci) {
        if (config.enableAnvilModification) {
            RPRenames.LOGGER.info("Starting RPRenames modification on AnvilScreen");

            int pageButtonsY = this.height / 2 + 57;
            if (config.viewMode == RenameButton.ViewMode.GRID) {
                pageButtonsY -= 4;
                maxPageElements = 20;
            }

            for (int i = 0; i < maxPageElements; i++) {
                buttons.add(new RenameButton(config.viewMode, i));
            }

            pageDown = new TexturedButtonWidget(this.width / 2 - backgroundWidth / 2 - menuWidth + menuXOffset + buttonXOffset, pageButtonsY, pageArrowsWidth, pageArrowsHeight, 0, 0, pageArrowsHeight, PAGE_ARROWS_TEXTURE, pageArrowsTextureWidth, pageArrowsTextureHeight, (button -> {
                if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    page = 0;
                } else {
                    page--;
                }
                defineButtons();
                showButtons();
                tabsUpdate();
                updatePageWidgets();
                if (page == 0) {
                    button.active = false;
                }
                pageUp.active = true;
            }));
            pageUp = new TexturedButtonWidget(this.width / 2 - backgroundWidth / 2 + menuXOffset - buttonXOffset - pageArrowsWidth, pageButtonsY, pageArrowsWidth, pageArrowsHeight, pageArrowsWidth, 0, pageArrowsHeight, PAGE_ARROWS_TEXTURE, pageArrowsTextureWidth, pageArrowsTextureHeight, (button -> {
                if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                    page = ((currentRenameList.size() + maxPageElements - 1) / maxPageElements - 1);
                } else {
                    page++;
                }
                defineButtons();
                showButtons();
                tabsUpdate();
                updatePageWidgets();
                pageDown.active = true;
                if ((page + 1) * maxPageElements > currentRenameListSize - 1) {
                    button.active = false;
                }
            }));

            opener = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, 0, 22, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, (button) -> switchOpen());
            openerOpened = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, openerHeight * 2, openerHeight, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, null);

            addDrawableChild(opener);

            searchTab = new TexturedButtonWidget(this.width / 2 - backgroundWidth / 2 - menuWidth + menuXOffset - (tabWidth - 3), this.height / 2 - backgroundHeight / 2 + startTabOffsetY, tabWidth, tabHeight, 0, 0, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
                currentTab = Tabs.SEARCH;
                screenUpdate();
            });
            favoriteTab = new TexturedButtonWidget(this.width / 2 - backgroundWidth / 2 - menuWidth + menuXOffset - (tabWidth - 3), this.height / 2 - backgroundHeight / 2 + startTabOffsetY + (tabHeight + tabOffsetY), tabWidth, tabHeight, 0, tabHeight, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
                currentTab = Tabs.FAVORITE;
                screenUpdate();
            });
            inventoryTab = new TexturedButtonWidget(this.width / 2 - backgroundWidth / 2 - menuWidth + menuXOffset - (tabWidth - 3), this.height / 2 - backgroundHeight / 2 + startTabOffsetY + (tabHeight + tabOffsetY) * 2, tabWidth, tabHeight, 0, tabHeight * 2, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
                currentTab = Tabs.INVENTORY;
                screenUpdate();
            });
            globalTab = new TexturedButtonWidget(this.width / 2 - backgroundWidth / 2 - menuWidth + menuXOffset - (tabWidth - 3), this.height / 2 - backgroundHeight / 2 + startTabOffsetY + (tabHeight + tabOffsetY) * 4, tabWidth, tabHeight, 0, tabHeight * 3, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
                currentTab = Tabs.GLOBAL;
                screenUpdate();
            });

            addToFavorite = new TexturedButtonWidget(this.width / 2 + config.favoritePosX, this.height / 2 + config.favoritePosY, favoriteButtonWidth, favoriteButtonHeight, 0, favoriteButtonHeight, 0, FAVORITE_BUTTON_TEXTURE, favoriteButtonTextureWidth, favoriteButtonTextureHeight, button -> {
                String favoriteName = nameField.getText();

                String item = currentItem;
                if (item.equals("air") && !ghostCraft.slot1.isEmpty()) {
                    item = ConfigManager.getIdAndPath(ghostCraft.slot1.getItem());
                }
                if (!item.equals("air")) {
                    ConfigManager.addToFavorites(favoriteName, item);
                    favoriteButtonsUpdate(nameField.getText());
                    if (open) {
                        screenUpdate(page);
                    }
                }
            });
            removeFromFavorite = new TexturedButtonWidget(this.width / 2 + config.favoritePosX, this.height / 2 + config.favoritePosY, favoriteButtonWidth, favoriteButtonHeight, 0, 0, 0, FAVORITE_BUTTON_TEXTURE, favoriteButtonTextureWidth, favoriteButtonTextureHeight, button -> {
                String favoriteName = nameField.getText();

                String item = currentItem;
                if (item.equals("air") && !ghostCraft.slot1.isEmpty()) {
                    item = ConfigManager.getIdAndPath(ghostCraft.slot1.getItem());
                }
                if (!item.equals("air")) {
                    ConfigManager.removeFromFavorites(favoriteName, item);
                    favoriteButtonsUpdate(nameField.getText());
                    if (open) {
                        screenUpdate(page);
                    }
                }
            });

            searchField = new TextFieldWidget(renderer, this.width / 2 - (backgroundWidth / 2) - menuWidth + menuXOffset + searchFieldXOffset, this.height / 2 - 68, menuWidth - 38, 10, Text.of(""));
            searchField.setChangedListener(this::onSearch);
            searchField.setDrawsBackground(false);
            searchField.setMaxLength(1024);
            if (open) {
                openMenu();
            } else {
                screenUpdate();
            }
        }
    }

    private void switchOpen() {
        if (!open) {
            openMenu();
        } else {
            closeMenu();
        }
    }

    private void openMenu() {
        open = true;
        RPRenames.LOGGER.info("Opening RPRenames Menu");
        if (currentItem.equals("air")) {
            currentTab = Tabs.GLOBAL;
        } else {
            currentTab = Tabs.SEARCH;
        }
        screenUpdate();
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
        remove(openerOpened);
        currentTab = Tabs.SEARCH;
        RPRenames.LOGGER.info("Closing RPRenames Menu");
    }

    private void screenUpdate() {
        screenUpdate(0);
    }

    private void screenUpdate(int savedPage) {
        page = savedPage;
        opener.active = true;
        if (afterInventoryTab) {
            currentTab = Tabs.INVENTORY;
            page = tempPage;
        } else if (afterGlobalTab) {
            currentTab = Tabs.GLOBAL;
            page = tempPage;
        }
        calcRenameList();

        if (open) {
            removeWidgets();
            updateSearchRequest(page);
            reloadButton(openerOpened);
            addDrawableChild(searchField);
            searchField.setFocusUnlocked(true);
            nameField.setFocused(false);
            nameField.setFocusUnlocked(true);
            tabsUpdate();
        }
    }

    private void calcRenameList() {
        if (currentTab == Tabs.SEARCH) {
            originalRenameList = ConfigManager.getAllRenames(currentItem);
        } else if (currentTab == Tabs.FAVORITE) {
            originalRenameList = ConfigManager.getFavorites(currentItem);
        } else if (currentTab == Tabs.INVENTORY) {
            ArrayList<String> currentInvList = getInventory();
            ArrayList<String> checked = new ArrayList<>();
            ArrayList<Rename> names = new ArrayList<>();
            for (String item : currentInvList) {
                if (!item.equals("air") && !checked.contains(item)) {
                    checked.add(item);
                    ArrayList<Rename> renames = ConfigManager.getAllRenames(item);
                    names.addAll(renames);
                }
            }
            originalRenameList = names;
        } else if (currentTab == Tabs.GLOBAL) {
            originalRenameList = ConfigManager.getAllRenames();
        }
    }

    private void updateSearchRequest() {
        updateSearchRequest(0);
    }

    private void updateSearchRequest(int page) {
        hideButtons();
        currentRenameList = search(originalRenameList, searchTag);

        this.page = page;
        if (this.page >= (currentRenameList.size() + maxPageElements - 1) / maxPageElements) {
            this.page = ((currentRenameList.size() + maxPageElements - 1) / maxPageElements) - 1;
            if (this.page == -1) {
                this.page = 0;
            }
        }
        currentRenameListSize = currentRenameList.size();

        updatePageWidgets();

        defineButtons();
        showButtons();
    }

    @Inject(at = @At("RETURN"), method = "onRenamed")
    private void newNameEntered(String name, CallbackInfo ci) {
        if (config.enableAnvilModification) {
            favoriteButtonsUpdate(name);
        }
    }

    private void favoriteButtonsUpdate(String name) {
        remove(addToFavorite);
        remove(removeFromFavorite);
        if (!name.isEmpty()) {
            boolean favorite = Rename.isFavorite(currentItem.equals("air") ? ghostCraft.slot1.isEmpty() ? "air" : ConfigManager.getIdAndPath(ghostCraft.slot1.getItem()) : currentItem, name);
            if (favorite) {
                addDrawableChild(removeFromFavorite);
            } else {
                addDrawableChild(addToFavorite);
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AnvilScreen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "resize")
    private void onResize(AnvilScreen instance, MinecraftClient client, int width, int height) {
        if (config.enableAnvilModification) {
            buttons.clear();
            String tempSearchFieldText = searchField.getText();
            instance.init(client, width, height);
            searchField.setText(tempSearchFieldText);
        } else {
            instance.init(client, width, height);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;isActive()Z"), method = "keyPressed")
    private boolean onKeyPressedNameFieldIsActive(TextFieldWidget instance, int keyCode, int scanCode, int modifiers) {
        if (config.enableAnvilModification) {
            searchField.keyPressed(keyCode, scanCode, modifiers);
            return instance.isActive() || searchField.isActive();
        }
        return instance.isActive();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int xScreenOffset = (this.width - backgroundWidth) / 2;
        int yScreenOffset = (this.height - backgroundHeight) / 2;
        if (ghostCraft.doRender) {
            if ((mouseX - xScreenOffset >= 26 && mouseX - xScreenOffset <= 151) && (mouseY - yScreenOffset >= 46 && mouseY - yScreenOffset <= 64)) {
                ghostCraft.reset();
                if (currentItem.equals("air")) {
                    nameField.setText("");
                }
            }
        }
        if (open) {
            for (RenameButton renameButton : buttons) {
                if (renameButton.isActive()) {
                    renameButton.getButton().mouseClicked(mouseX, mouseY, button);
                }
            }
            pageDown.mouseClicked(mouseX, mouseY, button);
            pageUp.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    ArrayList<String> invChangeHandler = new ArrayList<>();

    @Inject(at = @At("RETURN"), method = "handledScreenTick")
    private void onHandledScreenTick(CallbackInfo ci) {
        if (config.enableAnvilModification) {
            searchField.tick();
            if (invChangeHandler.isEmpty()) {
                invChangeHandler = getInventory();
            } else {
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
        }
    }

    @Inject(at = @At("RETURN"), method = "onSlotUpdate")
    private void itemUpdate(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
        if (config.enableAnvilModification) {
            if (slotId == 0 || slotId == 1) {
                ghostCraft.reset();
            }
            if (slotId == 0) {
                if (stack.isEmpty()) {
                    currentItem = "air";
                    searchField.setText("");
                    searchField.setFocusUnlocked(false);
                    remove(searchField);
                    searchField.setFocused(false);
                } else {
                    currentItem = ConfigManager.getIdAndPath(stack.getItem());
                    itemAfterUpdate = stack.copy();
                    searchField.setFocusUnlocked(true);
                    currentTab = Tabs.SEARCH;
                    favoriteButtonsUpdate(nameField.getText());
                }
                if (open) {
                    if (currentTab != Tabs.GLOBAL) {
                        screenUpdate();
                    } else {
                        updateSearchRequest();
                    }
                } else {
                    screenUpdate();
                }
            }
        }
    }

    private ArrayList<String> getInventory() {
        ArrayList<String> inventoryList = new ArrayList<>();
        assert MinecraftClient.getInstance().player != null;
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        for (ItemStack itemStack : inventory.main) {
            inventoryList.add(ConfigManager.getIdAndPath(itemStack.getItem()));
        }
        inventoryList.add(currentItem);
        return inventoryList;
    }

    @Inject(at = @At("HEAD"), method = "drawForeground")
    private void frameUpdate(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (config.enableAnvilModification) {
            if (open) {
                int xScreenOffset = (this.width - backgroundWidth) / 2;
                int yScreenOffset = (this.height - backgroundHeight) / 2;
                RenderSystem.enableDepthTest();
                context.drawTexture(MENU_TEXTURE, -menuWidth + menuXOffset, 0, 0, tabWidth * 2, 0, menuWidth, menuHeight, menuTextureWidth, menuTextureHeight);
                int i = 0;
                while (currentTab != Tabs.values()[i]) {
                    i++;
                }
                i = i == 3 ? i + 1 : i;
                context.drawTexture(MENU_TEXTURE, -menuWidth + menuXOffset - tabWidth + 3, startTabOffsetY + (tabHeight + tabOffsetY) * i, 0, tabWidth, tabHeight * (i == 4 ? 3 : i), tabWidth, tabHeight, menuTextureWidth, menuTextureHeight);

                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(-xScreenOffset, -yScreenOffset, 0);
                for (RenameButton renameButton : buttons) {
                    if (renameButton.isActive()) {
                        renameButton.getButton().render(context, mouseX, mouseY, 0);
                    }
                }
                pageDown.render(context, mouseX, mouseY, 0);
                pageUp.render(context, mouseX, mouseY, 0);
                matrices.pop();

                Graphics.renderText(context, pageCount, menuWidth / -2 + menuXOffset, backgroundHeight - (config.viewMode == RenameButton.ViewMode.GRID ? 26 : 22), false, true);
                if (searchField != null) {
                    if (!searchField.isFocused() && searchField.getText().isEmpty()) {
                        Graphics.renderText(context, SEARCH_HINT_TEXT, -1, -menuWidth + searchFieldXOffset, searchFieldXOffset - 8, true, false);
                    }
                }

                ghostCraft.render(context, mouseX - xScreenOffset, mouseY - yScreenOffset);

                if (currentRenameList.isEmpty()) {
                    String s;
                    if (currentItem.equals("air") && (currentTab == Tabs.FAVORITE || currentTab == Tabs.SEARCH)) {
                        s = "rprenames.gui.putItem";
                    } else {
                        if (currentTab == Tabs.FAVORITE) {
                            s = "rprenames.gui.noFavoriteRenamesFound";
                        } else {
                            s = "rprenames.gui.noRenamesFound";
                        }
                    }
                    Graphics.renderText(context, Text.translatable(s).copy().fillStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY)), -1, (-menuWidth + menuXOffset) / 2, 37, true, true);
                } else {
                    for (RenameButton renameButton : buttons) {
                        renameButton.drawElements(context, menuWidth, menuXOffset, buttonXOffset);
                    }

                    MinecraftClient client = MinecraftClient.getInstance();
                    if (!config.disablePageArrowsTips && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
                        matrices.push();
                        matrices.translate(-xScreenOffset, -yScreenOffset, 0);
                        if (pageUp.isMouseOver(mouseX, mouseY) && pageUp.isHovered()) {
                            context.drawTooltip(client.textRenderer, Text.translatable("rprenames.gui.pageUp.toLast.tooltip").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)), mouseX, mouseY);
                        } else if (pageDown.isMouseOver(mouseX, mouseY) && pageDown.isHovered()) {
                            context.drawTooltip(client.textRenderer, Text.translatable("rprenames.gui.pageDown.toFirst.tooltip").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)), mouseX, mouseY);
                        }
                        matrices.pop();
                    }

                    for (RenameButton renameButton : buttons) {
                        if (renameButton.getButton() != null) {
                            if (renameButton.getButton().isMouseOver(mouseX, mouseY) && renameButton.getButton().isHovered() && renameButton.isActive()) {
                                ArrayList<Text> lines = new ArrayList<>(renameButton.getTooltip());
                                if (!renameButton.isCEM() && config.enablePreview) {
                                    if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) && !config.playerPreviewByDefault) {
                                        if (!config.disablePlayerPreviewTips) {
                                            lines.add(Text.translatable("rprenames.gui.tooltipHint.playerPreviewTip.holdShift").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)));
                                        }
                                    } else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) != config.playerPreviewByDefault) {
                                        searchField.setFocused(false);
                                        if (!config.disablePlayerPreviewTips) {
                                            lines.add(Text.translatable("rprenames.gui.tooltipHint.playerPreviewTip.pressF").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)));
                                        }
                                    }
                                }
                                matrices.push();
                                matrices.translate(-xScreenOffset, -yScreenOffset, 0);
                                context.drawTooltip(client.textRenderer, lines, mouseX, mouseY);
                                matrices.pop();
                                if ((currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) && (config.slotHighlightColorALPHA > 0 && config.highlightSlot)) {
                                    renameButton.highlightSlot(context, getInventory(), currentItem, highlightColor);
                                }
                                if (config.enablePreview) {
                                    renameButton.drawPreview(context, mouseX - xScreenOffset, mouseY - yScreenOffset, 52, 52, config.scaleFactorItem, config.scaleFactorEntity);
                                }
                            }
                        }
                    }
                }
                RenderSystem.disableDepthTest();
            } else {
                searchField.setFocused(false);
            }
        }
    }

    private void showButtons() {
        for (RenameButton renameButton : buttons) {
            int orderOnPage = renameButton.getOrderOnPage();
            if (orderOnPage + page * maxPageElements <= currentRenameListSize - 1) {
                renameButton.setActive(true);
            }
        }
    }

    private void hideButtons() {
        for (RenameButton renameButton : buttons) {
            renameButton.setActive(false);
        }
    }

    private void removeWidgets() {
        for (RenameButton renameButton : buttons) {
            renameButton.setActive(false);
        }
        pageCount = Text.empty();
        remove(searchTab);
        remove(favoriteTab);
        remove(inventoryTab);
        remove(globalTab);
    }

    private void reloadButton(ClickableWidget button) {
        remove(button);
        addDrawableChild(button);
    }

    private static void putInAnvil(int slotInInventory, MinecraftClient client) {
        assert client.player != null;
        int syncId = client.player.currentScreenHandler.syncId;
        assert client.interactionManager != null;
        client.interactionManager.clickSlot(syncId, 0, slotInInventory, SlotActionType.SWAP, client.player);
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
        assert MinecraftClient.getInstance().player != null;
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        String item = rename.getItem();
        boolean isInInventory = getInventory().contains(item);
        int indexInInventory = getInventory().indexOf(item);
        boolean asCurrentItem = item.equals(currentItem);
        boolean favorite = Rename.isFavorite(item, rename.getName());

        ArrayList<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.of(rename.getName()));
        if (currentTab == Tabs.INVENTORY) {
            tooltip.add(Text.of(config.translateItemNames ? Text.translatable(Registries.ITEM.get(new Identifier(item)).getTranslationKey()).getString() : item).copy().fillStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)));
        } else if (currentTab == Tabs.GLOBAL) {
            tooltip.add(Text.of(config.translateItemNames ? Text.translatable(Registries.ITEM.get(new Identifier(item)).getTranslationKey()).getString() : item).copy().fillStyle(Style.EMPTY.withColor(getInventory().contains(item) ? Formatting.DARK_AQUA : Formatting.RED)));
        }
        if (item.equals(ConfigManager.getIdAndPath(Items.NAME_TAG)) && rename.isCEM()) {
            Identifier mob = new Identifier(rename.getMob().entity());
            var entityType = Registries.ENTITY_TYPE.get(mob);
            tooltip.add(Text.of(config.translateMobNames ? Text.translatable(entityType.getTranslationKey()).getString() : rename.getMob().entity()).copy().fillStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
        }

        boolean enoughStackSize;
        boolean enoughDamage;
        boolean hasEnchant = false;
        boolean hasEnoughLevels = false;

        if (rename.getStackSize() != null && rename.getStackSize() > 1) {
            if (currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) {
                if (asCurrentItem) {
                    enoughStackSize = Rename.isInBounds(itemAfterUpdate.getCount(), rename.getOriginalStackSize());
                } else if (isInInventory) {
                    enoughStackSize = Rename.isInBounds(inventory.main.get(indexInInventory).getCount(), rename.getOriginalStackSize());
                } else {
                    enoughStackSize = true;
                }
            } else {
                enoughStackSize = Rename.isInBounds(itemAfterUpdate.getCount(), rename.getOriginalStackSize());
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
        } else {
            enoughStackSize = true;
        }

        if (rename.getDamage() != null && rename.getDamage() > 0) {
            if (currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) {
                if (asCurrentItem) {
                    enoughDamage = Rename.isInBounds(itemAfterUpdate.getDamage(), rename.getOriginalDamage(), item);
                } else if (isInInventory) {
                    enoughDamage = Rename.isInBounds(inventory.main.get(indexInInventory).getDamage(), rename.getOriginalDamage(), item);
                } else {
                    enoughDamage = true;
                }
            } else {
                enoughDamage = Rename.isInBounds(itemAfterUpdate.getDamage(), rename.getOriginalStackSize());
            }
            if (config.showExtraProperties) {
                if (config.showOriginalProperties) {
                    tooltip.add(Text.of("damage").copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                            .append(Text.of("=").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.of(rename.getOriginalDamage()).copy().fillStyle(Style.EMPTY.withColor(enoughDamage ? Formatting.GREEN : Formatting.DARK_RED))));
                } else {
                    tooltip.add(Text.of(Text.translatable("rprenames.gui.tooltipHint.damage").getString() + " " + rename.getDamage()).copy().fillStyle(Style.EMPTY.withColor(enoughDamage ? Formatting.GRAY : Formatting.DARK_RED)));
                }
            }
        } else {
            enoughDamage = true;
        }

        if (rename.getEnchantment() != null) {
            Map<Enchantment, Integer> enchantments = Maps.newLinkedHashMap();
            if (currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) {
                if (asCurrentItem) {
                    enchantments = EnchantmentHelper.fromNbt(itemAfterUpdate.getEnchantments());
                } else if (isInInventory) {
                    enchantments = EnchantmentHelper.fromNbt(inventory.main.get(indexInInventory).getEnchantments());
                }
            } else {
                enchantments = EnchantmentHelper.fromNbt(itemAfterUpdate.getEnchantments());
            }
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                String enchantName = rename.getEnchantment();
                if (!enchantName.contains(":")) {
                    enchantName = Identifier.DEFAULT_NAMESPACE + Identifier.NAMESPACE_SEPARATOR + enchantName;
                }
                if (Objects.requireNonNull(Registries.ENCHANTMENT.getId(enchantment)).toString().equals(enchantName)) {
                    hasEnchant = true;
                    if (Rename.isInBounds(entry.getValue(), rename.getOriginalEnchantmentLevel())) {
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
        } else {
            hasEnchant = true;
            hasEnoughLevels = true;
        }

        if (config.showPackName) {
            if (rename.getPackName() != null) {
                tooltip.add(Text.of(rename.getPackName()).copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD)));
            }
        }

        if (config.showNbtDisplayName && currentTab != Tabs.FAVORITE) {
            if (rename.getOriginalNbtDisplayName() != null) {
                tooltip.add(Text.of("nbt.display.Name=" + rename.getOriginalNbtDisplayName()).copy().fillStyle(Style.EMPTY.withColor(Formatting.BLUE)));
            }
        }

        boolean finalHasEnchant = hasEnchant;
        boolean finalHasEnoughLevels = hasEnoughLevels;
        int x;
        int y;
        int width;
        int height;
        int u;
        int v;
        Identifier texture;
        int textureWidth;
        int textureHeight;
        if (config.viewMode == RenameButton.ViewMode.LIST) {
            x = this.width / 2 - backgroundWidth / 2 - menuWidth + menuXOffset + buttonXOffset;
            y = this.height / 2 - 53 + (orderOnPage * (buttonHeight + buttonOffsetY));
            width = buttonWidth;
            height = buttonHeight;
            u = 0;
            v = favorite ? buttonHeight * 2 : 0;
            texture = BUTTON_TEXTURE;
            textureWidth = buttonTextureWidth;
            textureHeight = buttonTextureHeight;
        } else {
            x = this.width / 2 - backgroundWidth / 2 - menuWidth + menuXOffset + buttonXOffset + 1 + (orderOnPage % 5 * buttonGridWidth);
            y = this.height / 2 - backgroundHeight / 2 + 31 + (orderOnPage / 5 * buttonGridHeight);
            width = buttonGridWidth;
            height = buttonGridHeight;
            u = favorite ? buttonGridWidth : 0;
            v = 0;
            texture = BUTTON_TEXTURE_GRID;
            textureWidth = buttonGridTextureWidth;
            textureHeight = buttonGridTextureHeight;
        }
        TexturedButtonWidget texturedButtonWidget = new TexturedButtonWidget(x, y, width, height, u, v, height, texture, textureWidth, textureHeight, (button) -> {
            ghostCraft.reset();
            if (currentTab == Tabs.INVENTORY || currentTab == Tabs.GLOBAL) {
                if (indexInInventory != 36 && isInInventory) {
                    if (currentTab == Tabs.INVENTORY) {
                        afterInventoryTab = true;
                    } else {
                        afterGlobalTab = true;
                    }
                    tempPage = page;
                    if (!asCurrentItem) {
                        putInAnvil(indexInInventory, MinecraftClient.getInstance());
                    }
                    afterInventoryTab = false;
                    afterGlobalTab = false;
                } else if (indexInInventory != 36) {
                    assert (client != null ? client.player : null) != null;
                    for (int s = 0; s < 2; s++) {
                        moveToInventory(s, inventory);
                    }

                    ItemStack ghostSource = new ItemStack(ConfigManager.itemFromName(rename.getItem()));
                    ghostSource.setCount(rename.getStackSize());
                    ghostSource.setDamage(rename.getDamage());

                    ItemStack ghostEnchant = ItemStack.EMPTY;
                    if (rename.getEnchantment() != null) {
                        ghostEnchant = new ItemStack(Items.ENCHANTED_BOOK);
                        ghostEnchant.getOrCreateNbt();
                        assert ghostEnchant.getNbt() != null;
                        if (!ghostEnchant.getNbt().contains("Enchantments", 9)) {
                            ghostEnchant.getNbt().put("Enchantments", new NbtList());
                        }
                        NbtList nbtList = ghostEnchant.getNbt().getList("Enchantments", 10);
                        nbtList.add(EnchantmentHelper.createNbt(new Identifier(rename.getEnchantment()), rename.getEnchantmentLevel()));
                    }

                    ItemStack ghostResult = RenameButton.createItem(rename);

                    ghostCraft.setSlots(ghostSource, ghostEnchant, ghostResult);
                    ghostCraft.setRender(true);
                } else {
                    moveToInventory(1, inventory);
                }
            }
            if (currentTab == Tabs.SEARCH || isInInventory) {
                if (!enoughStackSize || !enoughDamage) {
                    ghostCraft.setForceRenderBG(true, null, true);
                    ghostCraft.setRender(true);
                }
                if (!finalHasEnchant || !finalHasEnoughLevels) {
                    ItemStack ghostEnchant = new ItemStack(Items.ENCHANTED_BOOK);
                    ghostEnchant.getOrCreateNbt();
                    assert ghostEnchant.getNbt() != null;
                    if (!ghostEnchant.getNbt().contains("Enchantments", 9)) {
                        ghostEnchant.getNbt().put("Enchantments", new NbtList());
                    }
                    NbtList nbtList = ghostEnchant.getNbt().getList("Enchantments", 10);
                    nbtList.add(EnchantmentHelper.createNbt(new Identifier(rename.getEnchantment()), rename.getEnchantmentLevel()));
                    ghostCraft.setSlots(ItemStack.EMPTY, ghostEnchant, ItemStack.EMPTY);
                    ghostCraft.setForceRenderBG(null, null, true);
                    ghostCraft.setRender(true);
                }
            }
            nameField.setText(rename.getName());
        });
        buttons.get(orderOnPage).setParameters(texturedButtonWidget, rename, page, tooltip);
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
        pageDown.active = page != 0;
        pageUp.active = (page + 1) * maxPageElements <= currentRenameListSize - 1;
        pageCount = Text.of(page + 1 + "/" + (currentRenameList.size() + maxPageElements - 1) / maxPageElements);
    }

    private void tabsUpdate() {
        reloadButton(searchTab);
        reloadButton(favoriteTab);
        reloadButton(inventoryTab);
        reloadButton(globalTab);
        searchTab.active = true;
        favoriteTab.active = true;
        inventoryTab.active = true;
        globalTab.active = true;
        if (currentTab == Tabs.SEARCH) {
            searchTab.active = false;
        } else if (currentTab == Tabs.FAVORITE) {
            favoriteTab.active = false;
        } else if (currentTab == Tabs.INVENTORY) {
            inventoryTab.active = false;
        } else if (currentTab == Tabs.GLOBAL) {
            globalTab.active = false;
        }
    }

    private void onSearch(String search) {
        searchTag = search;
        if (open) {
            updateSearchRequest();
        }
    }

    private ArrayList<Rename> search(ArrayList<Rename> list, String match) {
        ArrayList<Rename> cutList = new ArrayList<>();
        if (match.startsWith("#")) {
            String matchTag = match.substring(1);
            if (matchTag.contains(" ") && !matchTag.toUpperCase(Locale.ROOT).contains("#REGEX:") && !matchTag.toUpperCase(Locale.ROOT).contains("#IREGEX:")) {
                matchTag = matchTag.substring(0, matchTag.indexOf(" "));
            } else if (matchTag.contains(" #")) {
                matchTag = matchTag.substring(0, matchTag.indexOf(" #"));
            }
            if (matchTag.toUpperCase(Locale.ROOT).startsWith("REGEX:") || matchTag.toUpperCase(Locale.ROOT).startsWith("IREGEX:")) {
                String regex = matchTag;
                boolean caseInsensitive = false;
                if (matchTag.toUpperCase(Locale.ROOT).startsWith("I")) {
                    regex = regex.substring(1);
                    caseInsensitive = true;
                }
                regex = regex.substring(6);

                boolean isRegex;
                try {
                    Pattern.compile(regex);
                    isRegex = true;
                } catch (PatternSyntaxException e) {
                    isRegex = false;
                }

                if (isRegex) {
                    for (Rename r : list) {
                        if (caseInsensitive ? r.getName().toUpperCase(Locale.ROOT).matches(regex.toUpperCase(Locale.ROOT)) : r.getName().matches(regex)) {
                            cutList.add(r);
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("PACK:") || matchTag.toUpperCase(Locale.ROOT).startsWith("PACKNAME:")) {
                String packName = matchTag.substring(4);
                while (packName.charAt(0) != ':') {
                    packName = packName.substring(1);
                }
                packName = packName.substring(1);
                for (Rename r : list) {
                    if (r.getPackName() != null && r.getPackName().replace(" ", "_").toUpperCase(Locale.ROOT).contains(packName.toUpperCase(Locale.ROOT))) {
                        cutList.add(r);
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("ITEM:")) {
                String itemName = matchTag.substring(5);
                for (Rename r : list) {
                    if (r.getItem() != null && r.getItem().toUpperCase(Locale.ROOT).contains(itemName.toUpperCase(Locale.ROOT))) {
                        cutList.add(r);
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("STACKSIZE:") || matchTag.toUpperCase(Locale.ROOT).startsWith("STACK:") || matchTag.toUpperCase(Locale.ROOT).startsWith("SIZE:")) {
                String stackSize = matchTag.toUpperCase(Locale.ROOT).substring(4);
                while (stackSize.charAt(0) != ':') {
                    stackSize = stackSize.substring(1);
                }
                stackSize = stackSize.substring(1);
                if (stackSize.matches("[0-9]+")) {
                    for (Rename r : list) {
                        if (Rename.isInBounds(Integer.parseInt(stackSize), r.getOriginalStackSize())) {
                            cutList.add(r);
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("DAMAGE:")) {
                String damage = matchTag.substring(7);
                if (damage.matches("[0-9]+")) {
                    for (Rename r : list) {
                        if (Rename.isInBounds(Integer.parseInt(damage), r.getOriginalDamage(), r.getItem())) {
                            cutList.add(r);
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("ENCH:") || matchTag.toUpperCase(Locale.ROOT).startsWith("ENCHANT:") || matchTag.toUpperCase(Locale.ROOT).startsWith("ENCHANTMENT:")) {
                String enchant = matchTag.toUpperCase(Locale.ROOT).substring(4);
                while (enchant.charAt(0) != ':') {
                    enchant = enchant.substring(1);
                }
                enchant = enchant.substring(1);
                for (Rename r : list) {
                    if (r.getEnchantment() != null) {
                        ArrayList<String> split = Rename.split(r.getOriginalEnchantment());
                        for (String s : split) {
                            if (s.toUpperCase(Locale.ROOT).contains(enchant)) {
                                cutList.add(r);
                                break;
                            }
                        }
                    }
                }
            } else if (matchTag.toUpperCase(Locale.ROOT).startsWith("FAV:") || matchTag.toUpperCase(Locale.ROOT).startsWith("FAVORITE:")) {
                for (Rename r : list) {
                    if (Rename.isFavorite(r.getItem(), r.getName())) {
                        cutList.add(r);
                    }
                }
            }
            if (match.substring(1).contains(" ") && !matchTag.toUpperCase(Locale.ROOT).contains("#REGEX:") && !matchTag.toUpperCase(Locale.ROOT).contains("#IREGEX:")) {
                cutList = search(cutList, match.substring(match.indexOf(" ") + 1));
            } else if (match.substring(1).contains(" #")) {
                cutList = search(cutList, match.substring(match.indexOf(" #") + 1));
            }
        } else {
            if (match.startsWith("\\#")) {
                match = match.substring(1);
            }
            boolean isRegex = false;
            try {
                Pattern.compile(match);
                isRegex = true;
            } catch (Exception ignored) {
            }
            for (Rename r : list) {
                if (r.getName().toUpperCase(Locale.ROOT).contains(match.toUpperCase(Locale.ROOT)) || (isRegex && r.getName().toUpperCase(Locale.ROOT).matches(match.toUpperCase(Locale.ROOT)))) {
                    cutList.add(r);
                }
            }
        }
        return cutList;
    }
}