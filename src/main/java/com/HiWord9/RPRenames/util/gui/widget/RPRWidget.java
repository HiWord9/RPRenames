package com.HiWord9.RPRenames.util.gui.widget;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.mod_config.ModConfig;
import com.HiWord9.RPRenames.util.RPRInteractableScreen;
import com.HiWord9.RPRenames.util.config.favorite.FavoritesManager;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.widget.external.FavoriteButton;
import com.HiWord9.RPRenames.util.rename.RenamesHelper;
import com.HiWord9.RPRenames.util.rename.RenamesManager;
import com.HiWord9.RPRenames.util.rename.RenamesSearchEngine;
import com.HiWord9.RPRenames.util.rename.type.Rename;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class RPRWidget implements Drawable, Element/*, Widget*/ {
    protected ModConfig config = ModConfig.INSTANCE;
    protected static Identifier MENU_TEXTURE = Identifier.of(RPRenames.MOD_ID, "textures/gui/menu.png");

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

    protected MinecraftClient client;

    protected RPRInteractableScreen screen;
    protected RenamesManager<?> renamesManager;
    protected FavoritesManager favoritesManager;

    protected TextFieldWidget nameField;
    protected FavoriteButton favoriteButton;
    protected GhostCraft ghostCraft;

    protected TabButton searchTab;
    protected TabButton favoriteTab;
    protected TabButton inventoryTab;
    protected TabButton globalTab;

    protected RandomButton randomButton;

    public TextFieldWidget searchField;

    protected PageButton pageDown;
    protected PageButton pageUp;

    protected final List<ClickableWidget> widgets = new ArrayList<>();

    protected Text pageCount = Text.empty();

    protected final List<RenameButton> buttons = new ArrayList<>();

    protected Tab currentTab = Tab.SEARCH;

    protected final List<Rename> unfilteredRenames = new ArrayList<>();
    protected final List<Rename> filteredRenames = new ArrayList<>();

    final Text SEARCH_HINT_TEXT = Text.translatable("rprenames.gui.searchHintText")
            .formatted(Formatting.ITALIC)
            .formatted(Formatting.GRAY);

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
            RenamesManager<?> renamesManager,
            FavoritesManager favoritesManager,
            TextFieldWidget nameField,
            FavoriteButton favoriteButton,
            GhostCraft ghostCraft
    ) {
        this.client = MinecraftClient.getInstance();

        this.renamesManager = renamesManager;
        this.favoritesManager = favoritesManager;

        this.nameField = nameField;
        this.favoriteButton = favoriteButton;
        this.ghostCraft = ghostCraft;

        this.screen = parentScreen;
        this.x = x;
        this.y = y;

        pageDown = new PageButton(
                this,
                this.x + MENU_START_X + BUTTON_X_OFFSET,
                this.y + PAGE_BUTTONS_Y,
                PageButton.Type.DOWN
        );
        pageUp = new PageButton(
                this,
                this.x + WIDGET_WIDTH - BUTTON_X_OFFSET - PageButton.BUTTON_WIDTH,
                this.y + PAGE_BUTTONS_Y,
                PageButton.Type.UP
        );

        int tabsOffset = TabButton.BUTTON_HEIGHT + TAB_OFFSET_Y;
        int tabsY = this.y + START_TAB_OFFSET_Y;
        searchTab = new TabButton(this, this.x, tabsY + tabsOffset * Tab.SEARCH.displayIndex, Tab.SEARCH);
        favoriteTab = new TabButton(this, this.x, tabsY + tabsOffset * Tab.FAVORITE.displayIndex, Tab.FAVORITE);
        inventoryTab = new TabButton(this, this.x, tabsY + tabsOffset * Tab.INVENTORY.displayIndex, Tab.INVENTORY);
        globalTab = new TabButton(this, this.x, tabsY + tabsOffset * Tab.GLOBAL.displayIndex, Tab.GLOBAL);

        randomButton = new RandomButton(
                this,
                this.x + WIDGET_WIDTH - 14 - RandomButton.BUTTON_WIDTH,
                this.y + 14, RandomButton.randomNumber() % RandomButton.SIDES
        );

        searchField = new TextFieldWidget(
                client.textRenderer,
                this.x + MENU_START_X + SEARCH_FIELD_X_OFFSET,
                this.y + 15,
                MENU_TEXTURE_WIDTH - 53, 10,
                Text.of("")
        );
        searchField.setChangedListener(this::onSearch);
        searchField.setDrawsBackground(false);
        searchField.setMaxLength(1024);

        widgets.addAll(List.of(
                randomButton, searchField,
                searchTab, favoriteTab,
                inventoryTab, globalTab,
                pageDown, pageUp
        ));

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
        nameField.setFocusUnlocked(true);

        screen.updateMenuShift();
    }

    public void close() {
        open = false;

        searchField.setFocused(false);
        searchField.setFocusUnlocked(false);
        searchField.setText("");

        nameField.setFocused(true);
        nameField.setFocusUnlocked(false);

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

            if (rename instanceof CITRename citRename) {
                var craftMatcher = new CITRename.CraftMatcher(citRename, getActiveItemStack());
                if (!craftMatcher.enoughStackSize() || !craftMatcher.enoughDamage()) {
                    ghostCraft.setSpecialHighlight(true, null, true);
                    ghostCraft.setRender(true);
                }
                if (!craftMatcher.hasEnchant() || !craftMatcher.hasEnoughLevels()) {
                    ghostCraft.setStacks(null, RenamesHelper.getGhostCraftEnchant(citRename), null);
                    ghostCraft.setSpecialHighlight(null, null, true);
                    ghostCraft.setRender(true);
                }
            }
        } else { //not in inventory
            for (int s = 0; s < screen.getCraftSlotsAmount() - 1; s++) screen.moveToInventory(s);

            ghostCraft.setStacks(RenamesHelper.getGhostCraftItems(rename));
            ghostCraft.setRender(true);
        }

        setNameText(rename.getName());
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
                Screen screen = client.currentScreen;
                if (screen != null && screen.getFocused() == searchField) {
                    screen.setFocused(null);
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

    public void offsetX(int x) {
        this.x += x;

        for (Widget widget : widgets) {
            widget.setX(widget.getX() + x);
        }
        for (RenameButton renameButton : buttons) {
            if (renameButton == null) continue;
            renameButton.setX(renameButton.getX() + x);
        }
    }

    protected void onSearch(String s) {
        updateFilteredRenames();
        openPage(0);
    }

// Implementations

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!open) {
            searchField.setFocused(false);
            return;
        }

        checkForInvChanges();

        context.drawTexture(
                RenderLayer::getGuiTextured,
                MENU_TEXTURE,
                this.x + MENU_START_X, this.y,
                0,0,
                MENU_TEXTURE_WIDTH, MENU_TEXTURE_HEIGHT,
                MENU_TEXTURE_WIDTH, MENU_TEXTURE_HEIGHT
        );

        if (searchField != null && !searchField.isFocused() && searchField.getText().isEmpty()) {
            Graphics.renderText(
                    context, SEARCH_HINT_TEXT,
                    this.x + MENU_START_X + SEARCH_FIELD_X_OFFSET,
                    this.y + 15,
                    true, false
            );
        }

        int menuCenterX = this.x + MENU_START_X + (MENU_TEXTURE_WIDTH / 2);
        if (filteredRenames.isEmpty()) {
            String key = "noRenamesFound";
            if (getCraftItem() == Items.AIR && currentTab.forCraftItemOnly)
                key = "putItem";
            else if (currentTab == Tab.FAVORITE && getSearchText().isEmpty())
                key = "noFavoriteRenamesFound";

            Graphics.renderText(
                    context,
                    Text.translatable("rprenames.gui.%s".formatted(key))
                            .fillStyle(Style.EMPTY
                                    .withItalic(true)
                                    .withColor(Formatting.GRAY)
                            ),
                    menuCenterX, this.y + 37,
                    true, true
            );
        } else {
            Graphics.renderText(
                    context, pageCount,
                    menuCenterX, this.y + 140,
                    false, true
            );
        }

        RenameButton focusedButton = null;
        for (RenameButton renameButton : buttons) {
            renameButton.render(context, mouseX, mouseY, 0);
            if (renameButton.isHovered()) focusedButton = renameButton;
        }

        for (Drawable widget : widgets) {
            widget.render(context, mouseX, mouseY, 0);
        }

        if (focusedButton != null) focusedButton.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!open) return false;

        var screen = client.currentScreen;
        for (Element widget : widgets) {
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                if (widget == searchField && screen != null) {
                    screen.setFocused(searchField);
                }
                return true;
            } else if (
                    widget == searchField
                            && screen != null
                            && screen.getFocused() == searchField
            ) {
                screen.setFocused(null);
            }
        }
        for (RenameButton renameButton : buttons) {
            if (renameButton.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Element widget : widgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() { return false; }

    protected void checkForInvChanges() {
        if (inventoryStacks.isEmpty()) {
            inventoryStacks.addAll(playersInventory());
            return;
        }

        var newStacks = playersInventory();

        boolean equal = true;

        if (inventoryStacks.size() != newStacks.size()) {
            equal = false;
        } else {
            for (int i = 0; i < newStacks.size(); i++) {
                if (!ItemStack.areEqual(inventoryStacks.get(i), newStacks.get(i))) {
                    equal = false;
                    break;
                }
            }
        }

        if (equal) return;

        inventoryStacks.clear();
        inventoryStacks.addAll(newStacks);

        updateRenames();
        refreshPageContent();
    }

// Updating Data

    protected void updateRenames() {
        updateUnfilteredRenames();
        updateFilteredRenames();
    }

    protected void updateUnfilteredRenames() {
        unfilteredRenames.clear();
        switch (currentTab) {
            case SEARCH -> unfilteredRenames.addAll(renamesManager.getRenames(getCraftItem()));
            case FAVORITE -> unfilteredRenames.addAll(favoritesManager.getRenames(getCraftItem()));
            case INVENTORY -> unfilteredRenames.addAll(getInventoryRenames());
            case GLOBAL -> unfilteredRenames.addAll(renamesManager.getAllRenames());
        }
    }

    protected List<Rename> getInventoryRenames() {
        var checked = new HashSet<Item>();
        var renames = new ArrayList<Rename>();
        for (Item item : getAvailableItems())
            if (item != Items.AIR && checked.add(item))
                for (Rename r : renamesManager.getRenames(item))
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
                            && button.rename.getName().equals(nameText);
    }

    protected void refreshPageWidgets() {
        pageDown.active = page > 0;
        pageUp.active = (page + 1) * BUTTONS_ON_PAGE <= filteredRenames.size() - 1;
        pageCount = Text.of(page + 1 + "/" + (filteredRenames.size() + BUTTONS_ON_PAGE - 1) / BUTTONS_ON_PAGE);
    }

    protected RenameButton createButton(int orderOnPage, Rename rename) {
        boolean favorite = shouldRenameButtonBeFavorite(rename);

        int buttonsZoneX = this.x + MENU_START_X + BUTTON_X_OFFSET;
        int buttonsZoneY = this.y + 30;
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
                ? favoritesManager.isFavorite(getCraftItem(), rename.getName())
                : favoritesManager.isFavoriteAny(rename.getItems(), rename.getName());
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
        return nameField.getText();
    }

    public void setNameText(String text) {
        nameField.setText(text);
    }

    protected String getSearchText() {
        return searchField.getText();
    }

    // todo move to util
    public List<ItemStack> playersInventory() {
        assert client.player != null;
        return client.player.getInventory().getMainStacks()
                .stream().map(ItemStack::copy).toList();
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
