package com.hiword9.rprenames.mod.gui.widget;

import com.hiword9.rprenames.mod.RPRenames;
import com.hiword9.rprenames.mod.gui.RPRInteractableScreen;
import com.hiword9.rprenames.mod.impl.renames_manager.favorite.FavoritesManager;
import com.hiword9.rprenames.mod.gui.Graphics;
import com.hiword9.rprenames.mod.gui.widget.external.FavoriteButton;
import com.hiword9.rprenames.api.core.renames_manager.RenamesProvider;
import com.hiword9.rprenames.mod.util.RenamesSearchEngine;
import com.hiword9.rprenames.api.core.rename.Rename;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.hiword9.rprenames.mod.util.Util.*;

public class RPRWidget implements Renderable, GuiEventListener, OffsetableWidget {
    protected static Identifier MENU_TEXTURE = Identifier.fromNamespaceAndPath(RPRenames.MOD_ID, "textures/gui/menu.png");

    public static final int MENU_TEXTURE_WIDTH = 147;
    public static final int MENU_TEXTURE_HEIGHT = 166;
    public static final int WIDGET_WIDTH = 177;
    public static final int WIDGET_HEIGHT = MENU_TEXTURE_HEIGHT;
    protected static int MENU_START_X = WIDGET_WIDTH - MENU_TEXTURE_WIDTH;
    protected static final int TAB_OFFSET_Y = 6;
    protected static final int START_TAB_OFFSET_Y = 6;
    protected static final int BUTTON_X_OFFSET = 10;
    protected static final int SEARCH_FIELD_X_OFFSET = 24;
    protected static final int PAGE_BUTTONS_Y = 136;

    protected int x;
    protected int y;

    protected boolean open;

    protected RPRInteractableScreen screen;
    protected RenamesProvider<?> renamesProvider;
    protected FavoritesManager favoritesManager;

    protected EditBox nameField;
    protected FavoriteButton favoriteButton;
    protected GhostCraft ghostCraft;

    protected TabButton searchTab;
    protected TabButton favoriteTab;
    protected TabButton inventoryTab;
    protected TabButton globalTab;

    protected RandomButton randomButton;

    public EditBox searchField;

    protected PageButton pageDown;
    protected PageButton pageUp;

    protected final List<AbstractWidget> widgets = new ArrayList<>();

    protected Component pageCount = Component.empty();

    protected final List<RenameButton> buttons = new ArrayList<>();

    protected Tab currentTab = Tab.SEARCH;

    protected final List<Rename> unfilteredRenames = new ArrayList<>();
    protected final List<Rename> filteredRenames = new ArrayList<>();

    final Component SEARCH_HINT_TEXT = Component.translatable("rprenames.gui.searchHintText")
            .withStyle(ChatFormatting.ITALIC)
            .withStyle(ChatFormatting.GRAY);

    protected int page = 0;

    public static final int ROWS = 4;
    public static final int COLUMNS = 5;
    public static final int BUTTONS_ON_PAGE = ROWS * COLUMNS;

    protected ItemStack activeItemStack = ItemStack.EMPTY;
    protected final List<ItemStack> inventoryStacks = new ArrayList<>();

    public RPRWidget() {}

    public void init(
            int x, int y,
            @Nullable RPRInteractableScreen parentScreen,
            RenamesProvider<?> renamesProvider,
            FavoritesManager favoritesManager,
            EditBox nameField,
            FavoriteButton favoriteButton,
            GhostCraft ghostCraft
    ) {
        this.renamesProvider = renamesProvider;
        this.favoritesManager = favoritesManager;

        this.nameField = nameField;
        this.favoriteButton = favoriteButton;
        this.ghostCraft = ghostCraft;

        this.screen = parentScreen;

        pageDown = new PageButton(
                this,
                MENU_START_X + BUTTON_X_OFFSET,
                PAGE_BUTTONS_Y,
                PageButton.Type.DOWN
        );
        pageUp = new PageButton(
                this,
                WIDGET_WIDTH - BUTTON_X_OFFSET - PageButton.BUTTON_WIDTH,
                PAGE_BUTTONS_Y,
                PageButton.Type.UP
        );

        int tabsOffset = TabButton.BUTTON_HEIGHT + TAB_OFFSET_Y;
        searchTab = new TabButton(this, 0, START_TAB_OFFSET_Y + tabsOffset * Tab.SEARCH.displayIndex, Tab.SEARCH);
        favoriteTab = new TabButton(this, 0, START_TAB_OFFSET_Y + tabsOffset * Tab.FAVORITE.displayIndex, Tab.FAVORITE);
        inventoryTab = new TabButton(this, 0, START_TAB_OFFSET_Y + tabsOffset * Tab.INVENTORY.displayIndex, Tab.INVENTORY);
        globalTab = new TabButton(this, 0, START_TAB_OFFSET_Y + tabsOffset * Tab.GLOBAL.displayIndex, Tab.GLOBAL);

        randomButton = new RandomButton(
                this,
                WIDGET_WIDTH - 14 - RandomButton.BUTTON_WIDTH,
                14, randomNumber() % RandomButton.SIDES
        );

        searchField = new EditBox(
                textRenderer(),
                MENU_START_X + SEARCH_FIELD_X_OFFSET,
                15,
                MENU_TEXTURE_WIDTH - 53, 10,
                Component.nullToEmpty("")
        );
        searchField.setResponder(this::onSearch);
        searchField.setBordered(false);
        searchField.setMaxLength(1024);

        widgets.addAll(List.of(
                randomButton, searchField,
                searchTab, favoriteTab,
                inventoryTab, globalTab,
                pageDown, pageUp
        ));

        setPosition(x, y);

        refreshFavoriteButton();
    }

// General Managing

    public boolean isOpen() {
        return open;
    }

    public void toggleOpen() {
        if (open) close();
        else open();
    }

    public void open() {
        open = true;

        openTab(getCraftItem() == Items.AIR
                ? Tab.GLOBAL
                : Tab.SEARCH
        );

        nameField.setFocused(false);
        nameField.setCanLoseFocus(true);

        screen.updateMenuShift();
    }

    public void close() {
        open = false;

        searchField.setFocused(false);
        searchField.setCanLoseFocus(false);
        searchField.setValue("");

        nameField.setFocused(true);
        nameField.setCanLoseFocus(false);

        screen.updateMenuShift();
    }

    public void openTab(Tab tab) {
        currentTab = tab;
        resetPageContent();
    }

    public Tab getCurrentTab() {
        return currentTab;
    }

    public void openPage(int page) {
        int maxPage = getMaxPageIndex();
        if (page < 0) page = maxPage + page + 1;
        this.page = Math.max(0, Math.min(page, maxPage));
        refreshPageContent();
    }

    public int getPage() {
        return this.page;
    }

    public void prevPage() {
        openPage(hasShiftDown() ? 0 : page - 1);
    }

    public void nextPage() {
        openPage(hasShiftDown() ? -1 : page + 1);
    }

// Execution

    public void doRename(Rename rename) {
        ghostCraft.reset();

        int indexInInventory = inventoryStacks.indexOf(pickItemStackForRename(rename));
        boolean forCraftItem = isRenameForCraftItem(rename);

        if (indexInInventory != -1 || forCraftItem) {
            if (forCraftItem) { //in work slot
                for (int s = 1; s < screen.getCraftSlotsAmount() - 1; s++)
                    screen.moveToInventory(s);
            } else { //in inventory
                Tab tab = getCurrentTab();
                int page = getPage();

                screen.moveToCraft(indexInInventory, 0);

                if (!tab.forCraftItemOnly) {
                    openTab(tab);
                    openPage(page);
                }
            }
        } else { //not in inventory
            for (int s = 0; s < screen.getCraftSlotsAmount() - 1; s++)
                screen.moveToInventory(s);
        }

        if (rename instanceof GhostCraft.Loader ghostCraftLoader) {
            ghostCraftLoader.loadGhostCraft(ghostCraft, getActiveItemStack());
        }

        setNameText(rename.getName().getString());
    }

    public void addOrRemoveFavorite(boolean add, List<Item> items, String name) {
        if (add) favoritesManager.addRenames(items, name);
        else favoritesManager.removeRenames(items, name);

        refreshFavoriteButton();
        if (!isOpen()) return;

        if (getCurrentTab() == Tab.FAVORITE) {
            updateRenames();
            refreshPageContent();
        } else refreshFavorite();
    }

// External Event Triggers

    public void updatedName() {
        refreshFavoriteButton();
        refreshSelected();
    }

    public void updatedItem(int slotId, ItemStack stack) {
        if (slotId == 0) {
            activeItemStack = stack.copy();
            if (stack.isEmpty()) {
                if (currentScreen() != null && currentScreen().getFocused() == searchField) {
                    currentScreen().setFocused(null);
                }
                searchField.setFocused(false);

                if (getCurrentTab() == Tab.GLOBAL) refreshPageContent();
                else resetPageContent();
            } else {
                openTab(Tab.SEARCH);
            }

            refreshFavoriteButton();
        }
        if (slotId < screen.getCraftSlotsAmount() - 1) {
            ghostCraft.reset();
            if (getActiveItemStack().isEmpty()) setNameText("");
        }
    }

    protected void onSearch(String s) {
        updateFilteredRenames();
        openPage(0);
    }

// Implementations

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!open) {
            searchField.setFocused(false);
            return;
        }

        checkForInvChanges();

        Graphics.renderGuiTexture(
                graphics,
                MENU_TEXTURE,
                getX() + MENU_START_X, getY(),
                0,0,
                MENU_TEXTURE_WIDTH, MENU_TEXTURE_HEIGHT,
                MENU_TEXTURE_WIDTH, MENU_TEXTURE_HEIGHT
        );

        if (searchField != null && !searchField.isFocused() && searchField.getValue().isEmpty()) {
            Graphics.renderText(
                    graphics, SEARCH_HINT_TEXT,
                    getX() + MENU_START_X + SEARCH_FIELD_X_OFFSET,
                    getY() + 15,
                    true, false
            );
        }

        int menuCenterX = getX() + MENU_START_X + (MENU_TEXTURE_WIDTH / 2);
        if (filteredRenames.isEmpty()) {
            String key = "noRenamesFound";
            if (getCraftItem() == Items.AIR && currentTab.forCraftItemOnly)
                key = "putItem";
            else if (currentTab == Tab.FAVORITE && getSearchText().isEmpty())
                key = "noFavoriteRenamesFound";

            Graphics.renderText(
                    graphics,
                    Component.translatable("rprenames.gui.%s".formatted(key))
                            .withStyle(Style.EMPTY
                                    .withItalic(true)
                                    .withColor(ChatFormatting.GRAY)
                            ),
                    menuCenterX, getY() + 37,
                    true, true
            );
        } else {
            Graphics.renderText(
                    graphics, pageCount,
                    menuCenterX, getY() + 140,
                    false, true
            );
        }

        RenameButton focusedButton = null;
        for (RenameButton renameButton : buttons) {
            renameButton.extractRenderState(graphics, mouseX, mouseY, 0);
            if (renameButton.isHovered()) focusedButton = renameButton;
        }

        for (RenameButton renameButton : buttons) {
            renameButton.renderForeground(graphics, mouseX, mouseY);
        }

        for (Renderable widget : widgets) {
            widget.extractRenderState(graphics, mouseX, mouseY, 0);
        }

        if (focusedButton != null) focusedButton.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!open) return false;

        for (GuiEventListener widget : widgets) {
            if (widget.mouseClicked(click, doubled)) {
                if (widget == searchField && currentScreen() != null) {
                    currentScreen().setFocused(searchField);
                }
                return true;
            } else if (
                    widget == searchField
                            && currentScreen() != null
                            && currentScreen().getFocused() == searchField
            ) {
                currentScreen().setFocused(null);
            }
        }
        for (RenameButton renameButton : buttons) {
            if (renameButton.mouseClicked(click, doubled)) return true;
        }

        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (var l : List.of(widgets, buttons)) for (GuiEventListener element : l) {
            element.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        for (var l : List.of(widgets, buttons)) for (GuiEventListener element : l) {
            if (element.mouseDragged(click, offsetX, offsetY))
                return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (var l : List.of(widgets, buttons)) for (GuiEventListener element : l) {
            if (element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        for (var l : List.of(widgets, buttons)) for (GuiEventListener element : l) {
            if (element.keyPressed(input))
                return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        for (var l : List.of(widgets, buttons)) for (GuiEventListener element : l) {
            if (element.keyReleased(input))
                return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() { return false; }

    protected void checkForInvChanges() {
        if (!inventoryStacks.isEmpty()) {
            var newStacks = inventoryCopy();

            boolean equal = true;

            if (inventoryStacks.size() != newStacks.size()) {
                equal = false;
            } else {
                for (int i = 0; i < newStacks.size(); i++) {
                    if (!ItemStack.matches(inventoryStacks.get(i), newStacks.get(i))) {
                        equal = false;
                        break;
                    }
                }
            }

            if (equal) return;

            inventoryStacks.clear();
            inventoryStacks.addAll(newStacks);
        } else {
            inventoryStacks.addAll(inventoryCopy());
        }

        updateRenames();
        refreshPageContent();
    }

    @Override
    public void setX(int x) {
        var prevX = getX();
        var dif = x - prevX;
        visitWidgets(w -> w.setX(w.getX() + dif));
        this.x += dif;
    }

    @Override
    public void setY(int y) {
        var prevY = getY();
        var dif = y - prevY;
        visitWidgets(w -> w.setY(w.getY() + dif));
        this.y += dif;
    }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public int getWidth() { return WIDGET_WIDTH; }

    @Override
    public int getHeight() { return WIDGET_HEIGHT; }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        for (AbstractWidget widget : widgets)
            consumer.accept(widget);
        for (RenameButton renameButton : buttons)
            if (renameButton != null)
                consumer.accept(renameButton);
    }

    @Override
    public ScreenRectangle getRectangle() {
        return OffsetableWidget.super.getRectangle();
    }

// Updating Data

    protected void updateRenames() {
        updateUnfilteredRenames();
        updateFilteredRenames();
    }

    protected void updateUnfilteredRenames() {
        unfilteredRenames.clear();
        switch (currentTab) {
            case SEARCH -> unfilteredRenames.addAll(renamesProvider.getRenames(getCraftItem()));
            case FAVORITE -> unfilteredRenames.addAll(favoritesManager.getRenames(getCraftItem()));
            case INVENTORY -> unfilteredRenames.addAll(getInventoryRenames());
            case GLOBAL -> unfilteredRenames.addAll(renamesProvider.getAllRenames());
        }
    }

    protected List<Rename> getInventoryRenames() {
        var checked = new HashSet<Item>();
        var renames = new ArrayList<Rename>();
        for (Item item : getAvailableItems())
            if (item != Items.AIR && checked.add(item))
                for (Rename r : renamesProvider.getRenames(item))
                    if (!renames.contains(r))
                        renames.add(r);
        return renames;
    }

    protected void updateFilteredRenames() {
        filteredRenames.clear();
        filteredRenames.addAll(RenamesSearchEngine.search(
                unfilteredRenames,
                getSearchText(),
                favoritesManager
        ));
    }

// Widgets Update Triggering

    public void resetPageContent() {
        updateRenames();
        openPage(0);
    }

    protected void refreshPageContent() {
        refreshButtons();
        refreshSelected();
        refreshPageWidgets();
    }

    protected void refreshButtons() {
        int maxIndex = filteredRenames.size() - 1;
        int offset = page * BUTTONS_ON_PAGE;

        buttons.clear();
        for (int i = 0; i < BUTTONS_ON_PAGE; i++) {
            int index = i + offset;
            if (index <= maxIndex)
                buttons.add(createButton(i, filteredRenames.get(index)));
        }
    }

    protected void refreshSelected() {
        Item craftItem = getCraftItem();
        String nameText = getNameText();

        for (RenameButton button : buttons)
            button.selected = button.rename.getItems().contains(craftItem)
                            && button.rename.getName().getString().equals(nameText);
    }

    protected void refreshPageWidgets() {
        pageDown.active = page > 0;
        pageUp.active = (page + 1) * BUTTONS_ON_PAGE <= filteredRenames.size() - 1;
        pageCount = Component.nullToEmpty(page + 1 + "/" + (filteredRenames.size() + BUTTONS_ON_PAGE - 1) / BUTTONS_ON_PAGE);
    }

    protected RenameButton createButton(int orderOnPage, Rename rename) {
        boolean favorite = shouldRenameButtonBeFavorite(rename);

        int buttonsZoneX = getX() + MENU_START_X + BUTTON_X_OFFSET;
        int buttonsZoneY = getY() + 30;
        int x = buttonsZoneX + 1 + (orderOnPage % 5 * RenameButton.BUTTON_WIDTH);
        int y = buttonsZoneY + 1 + (orderOnPage / 5 * RenameButton.BUTTON_HEIGHT);

        return new RenameButton(this, rename, x, y, favorite);
    }

    protected void refreshFavorite() {
        for (RenameButton button : buttons)
            button.favorite = shouldRenameButtonBeFavorite(button.rename);
    }

    protected void refreshFavoriteButton() {
        var name = getNameText();
        if (!name.isEmpty()) {
            favoriteButton.active = true;
            favoriteButton.favorite = favoritesManager.isFavorite(getCraftItem(), name);
        } else {
            favoriteButton.active = false;
        }
    }

    protected boolean shouldRenameButtonBeFavorite(Rename rename) {
        return currentTab == Tab.SEARCH
                ? favoritesManager.isFavorite(getCraftItem(), rename.getName().getString())
                : favoritesManager.isFavoriteAny(rename.getItems(), rename.getName().getString());
    }

// Other / Util

    public Item getCraftItem() {
        var item = getActiveItemStack().getItem();
        if (item != Items.AIR) return item;

        var ghostItem = ghostCraft.getStackInFirstSlot();
        if (ghostItem != null) item = ghostItem.getItem();

        return item;
    }

    public ItemStack pickItemStackForRename(Rename rename) {
        if (isRenameForCraftItem(rename))
            return getActiveItemStack();

        for (ItemStack stack : inventoryStacks)
            if (rename.getItems().contains(stack.getItem()))
                return stack;

        return null;
    }

    public boolean isRenameForCraftItem(Rename rename) {
        return rename.getItems().contains(getCraftItem());
    }

    public ItemStack getActiveItemStack() {
        return activeItemStack;
    }

    public String getNameText() {
        return nameField.getValue();
    }

    public void setNameText(String text) {
        nameField.setValue(text);
    }

    protected String getSearchText() {
        return searchField.getValue();
    }

    public List<Item> getAvailableItems() {
        var items = inventoryStacks.stream()
                .map(ItemStack::getItem)
                .collect(Collectors.toList());
        items.add(getActiveItemStack().getItem());
        return items;
    }

    protected int getMaxPageIndex() {
        return Math.max(0, (filteredRenames.size() - 1)) / BUTTONS_ON_PAGE;
    }

    public enum Tab {
        SEARCH(0, true),
        FAVORITE(1, true),
        INVENTORY(2, false),
        GLOBAL(4, false);

        public final int displayIndex;
        public final boolean forCraftItemOnly;

        Tab(int displayIndex, boolean forCraftItemOnly) {
            this.displayIndex = displayIndex;
            this.forCraftItemOnly = forCraftItemOnly;
        }
    }
}
