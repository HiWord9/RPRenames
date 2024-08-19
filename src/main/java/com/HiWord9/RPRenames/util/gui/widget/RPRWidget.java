package com.HiWord9.RPRenames.util.gui.widget;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.RPRInteractableScreen;
import com.HiWord9.RPRenames.util.config.FavoritesManager;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.widget.button.PageButton;
import com.HiWord9.RPRenames.util.gui.widget.button.RandomButton;
import com.HiWord9.RPRenames.util.gui.widget.button.RenameButton;
import com.HiWord9.RPRenames.util.gui.widget.button.TabButton;
import com.HiWord9.RPRenames.util.gui.widget.button.external.FavoriteButton;
import com.HiWord9.RPRenames.util.gui.widget.button.external.OpenerButton;
import com.HiWord9.RPRenames.util.rename.RenamesHelper;
import com.HiWord9.RPRenames.util.rename.RenamesManager;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class RPRWidget implements Drawable, Element/*, Widget*/ {
    protected ModConfig config = ModConfig.INSTANCE;
    protected static Identifier MENU_TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/menu.png");

    public static final int MENU_TEXTURE_WIDTH = 147;
    public static final int MENU_TEXTURE_HEIGHT = 166;
    public static final int WIDGET_WIDTH = 177;
    public static final int WIDGET_HEIGHT = MENU_TEXTURE_HEIGHT;
    static int MENU_START_X = WIDGET_WIDTH - MENU_TEXTURE_WIDTH;
    static final int TAB_OFFSET_Y = 6;
    static final int START_TAB_OFFSET_Y = 6;
    static final int BUTTON_X_OFFSET = 10;
    static final int SEARCH_FIELD_X_OFFSET = 24;
    static final int PAGE_BUTTONS_Y = 136;

    MinecraftClient client;
    RPRInteractableScreen interactableScreen;

    TextFieldWidget nameField;
    OpenerButton openerButton;
    FavoriteButton favoriteButton;
    GhostCraft ghostCraft;

    int x;
    int y;

    public boolean init = false;

    boolean open;

    TabButton searchTab;
    TabButton favoriteTab;
    TabButton inventoryTab;
    TabButton globalTab;

    RandomButton randomButton;

    public TextFieldWidget searchField;

    PageButton pageDown;
    PageButton pageUp;

    public ArrayList<ClickableWidget> widgets = new ArrayList<>();

    Text pageCount = Text.empty();

    final ArrayList<RenameButton> buttons = new ArrayList<>();

    Tab currentTab = Tab.SEARCH;

    ArrayList<AbstractRename> originalRenameList = new ArrayList<>();
    ArrayList<AbstractRename> currentRenameList = new ArrayList<>();

    final TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

    final Text SEARCH_HINT_TEXT = Text.translatable("rprenames.gui.searchHintText").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);

    String searchTag = "";

    int page = 0;
    int rows = 4;
    int columns = 5;
    int maxPageElements = rows * columns;

    ItemStack currentItem = ItemStack.EMPTY;
    public boolean shouldNotUpdateTab = false;
    int tempPage;
    ArrayList<Item> inventory = new ArrayList<>();

    public RPRWidget() {}

    public void init(int x, int y,
                     @Nullable RPRInteractableScreen parentScreen,
                     TextFieldWidget nameField,
                     OpenerButton openerButton,
                     FavoriteButton favoriteButton,
                     GhostCraft ghostCraft) {
        this.init = true;

        this.client = MinecraftClient.getInstance();

        this.nameField = nameField;
        this.openerButton = openerButton;
        this.favoriteButton = favoriteButton;
        this.ghostCraft = ghostCraft;

        this.interactableScreen = parentScreen;
        this.x = x;
        this.y = y;

        pageDown = new PageButton(this, this.x + MENU_START_X + BUTTON_X_OFFSET, this.y + PAGE_BUTTONS_Y, PageButton.Type.DOWN);
        pageUp = new PageButton(this, this.x + WIDGET_WIDTH - BUTTON_X_OFFSET - PageButton.BUTTON_WIDTH, this.y + PAGE_BUTTONS_Y, PageButton.Type.UP);

        searchTab = new TabButton(this, this.x, this.y + START_TAB_OFFSET_Y, Tab.SEARCH);
        favoriteTab = new TabButton(this, this.x, this.y + START_TAB_OFFSET_Y + (TabButton.BUTTON_HEIGHT + TAB_OFFSET_Y), Tab.FAVORITE);
        inventoryTab = new TabButton(this, this.x, this.y + START_TAB_OFFSET_Y + (TabButton.BUTTON_HEIGHT + TAB_OFFSET_Y) * 2, Tab.INVENTORY);
        globalTab = new TabButton(this, this.x, this.y + START_TAB_OFFSET_Y + (TabButton.BUTTON_HEIGHT + TAB_OFFSET_Y) * 4, Tab.GLOBAL);

        randomButton = new RandomButton(this, this.x + WIDGET_WIDTH - 14 - RandomButton.BUTTON_WIDTH, this.y + 14, randomNumber() % RandomButton.SIDES);

        searchField = new TextFieldWidget(renderer, this.x + MENU_START_X + SEARCH_FIELD_X_OFFSET, this.y + 15, MENU_TEXTURE_WIDTH - 53, 10, Text.of(""));
        searchField.setChangedListener(this::onSearch);
        searchField.setDrawsBackground(false);
        searchField.setMaxLength(1024);

        widgets = new ArrayList<>(List.of(
                searchTab, favoriteTab,
                inventoryTab, globalTab,
                pageDown, pageUp,
                randomButton, searchField
        ));

        updateFavoriteButton();
        screenUpdate();
    }

    public boolean isOpen() {
        return open;
    }

    public void toggleOpen() {
        if (!open) {
            open();
        } else {
            close();
        }
        interactableScreen.updateMenuShift();
    }

    public void open() {
        open = true;
        if (currentItem.isEmpty()) {
            currentTab = Tab.GLOBAL;
        } else {
            currentTab = Tab.SEARCH;
        }
        screenUpdate();

        nameField.setFocused(false);
        nameField.setFocusUnlocked(true);
    }

    public void close() {
        open = false;
        searchField.setFocused(false);
        searchField.setFocusUnlocked(false);
        searchField.setText("");
        updateWidgets();
        currentTab = Tab.SEARCH;

        nameField.setFocused(true);
        nameField.setFocusUnlocked(false);
    }

    public String getNameText() {
        return nameField.getText();
    }

    public void setNameText(String text) {
        nameField.setText(text);
    }

    public void setTab(Tab tab) {
        if (tab == currentTab) return;
        currentTab = tab;
        screenUpdate();
    }

    public Tab getCurrentTab() {
        return currentTab;
    }

    public void setPage(int page) {
        this.page = page;
        updateWidgets();
    }

    public int getPage() {
        return this.page;
    }

    public void prevPage() {
        setPage(hasShiftDown() ? 0 : page - 1);
    }

    public void nextPage() {
        setPage(hasShiftDown() ? ((currentRenameList.size() + maxPageElements - 1) / maxPageElements - 1) : page + 1);
    }

    public ItemStack getCurrentItem() {
        return currentItem;
    }

    public Item getItemInFirstSlot() {
        Item item = currentItem.getItem();
        if (item == Items.AIR && !ghostCraft.getStackInFirstSlot().isEmpty()) {
            item = ghostCraft.getStackInFirstSlot().getItem();
        }
        return item;
    }

    public void addOrRemoveFavorite(boolean add) {
        addOrRemoveFavorite(add, getNameText(), getItemInFirstSlot());
    }

    public void addOrRemoveFavorite(boolean add, String favoriteName, Item item) {
        if (item != Items.AIR) {
            if (add) {
                FavoritesManager.addToFavorites(favoriteName, item);
            } else {
                FavoritesManager.removeFromFavorites(favoriteName, item);
            }
            updateFavoriteButton();
            if (open) {
                screenUpdate(page);
            }
        }
    }

    public void onRenameButton(int button, boolean favorite, AbstractRename rename) {
        Item item = firstItemInInventory(rename);
        boolean asCurrentItem = item == getItemInFirstSlot();
        int indexInInventory = inventory.indexOf(item);
        boolean isInInventory = indexInInventory != -1;

        if (button == 1 && rename.getItem() != null) {
            favoriteInGui(favorite, rename, asCurrentItem, isInInventory, indexInInventory);
            return;
        }

        executeRename(rename, isInInventory, indexInInventory, asCurrentItem);
    }

    private void executeRename(AbstractRename rename, boolean isInInventory, int indexInInventory, boolean asCurrentItem) {
        ghostCraft.reset();
        if (isInInventory) {
            if (indexInInventory != 36) { //in inventory
                shouldNotUpdateTab = getCurrentTab() == Tab.INVENTORY || getCurrentTab() == Tab.GLOBAL;
                tempPage = page;
                if (!asCurrentItem) {
                    interactableScreen.moveToCraft(indexInInventory, 0);
                }
                shouldNotUpdateTab = false;
            } else { //in work slot
                interactableScreen.moveToInventory(1);
            }

            if (rename instanceof CITRename citRename) {
                CITRename.CraftMatcher craftMatcher = new CITRename.CraftMatcher(citRename, currentItem);
                if (!craftMatcher.enoughStackSize() || !craftMatcher.enoughDamage()) {
                    ghostCraft.setSpecialHighlight(true, null, true);
                    ghostCraft.setRender(true);
                }
                if (!craftMatcher.hasEnchant() || !craftMatcher.hasEnoughLevels()) {
                    ghostCraft.setStacks(ItemStack.EMPTY, RenamesHelper.getGhostCraftEnchant(citRename), ItemStack.EMPTY);
                    ghostCraft.setSpecialHighlight(null, null, true);
                    ghostCraft.setRender(true);
                }
            }
        } else { //not in inventory
            for (int s = 0; s < 2; s++) {
                interactableScreen.moveToInventory(s);
            }

            ItemStack[] ghostCraftStacks = RenamesHelper.getGhostCraftItems(rename);

            ghostCraft.setStacks(ghostCraftStacks[0], ghostCraftStacks[1], ghostCraftStacks[2]);
            ghostCraft.setRender(true);
        }

        setNameText(rename.getName());
    }

    private void favoriteInGui(boolean favorite, AbstractRename rename, boolean asCurrentItem, boolean isInInventory, int indexInInventory) {
        if (getCurrentTab() == Tab.SEARCH || getCurrentTab() == Tab.FAVORITE || asCurrentItem) {
            addOrRemoveFavorite(
                    !favorite,
                    rename.getName(),
                    getItemInFirstSlot()
            );
        } else {
            if (favorite) {
                for (Item i : rename.getItems()) {
                    if (FavoritesManager.isFavorite(i, rename.getName())) {
                        FavoritesManager.removeFromFavorites(rename.getName(), i);
                    }
                }
                updateFavoriteButton();
                if (isOpen()) {
                    screenUpdate(getPage());
                }
            } else {
                addOrRemoveFavorite(
                        true,
                        rename.getName(),
                        isInInventory ? inventory.get(indexInInventory) : rename.getItem()
                );
            }
        }
    }

    public void chooseRandomRename() {
        int randomNumber = randomNumber();
        int randomSide = randomNumber % RandomButton.SIDES;
        randomButton.setSide(randomSide);
        if (currentRenameList.isEmpty()) return;
        int renameNumber = randomNumber % currentRenameList.size();
        setPage(renameNumber / maxPageElements);
        buttons.get(renameNumber % maxPageElements).execute(0);
    }

    public void screenUpdate() {
        screenUpdate(0);
    }

    public void screenUpdate(int savedPage) {
        page = savedPage;
        if (shouldNotUpdateTab) {
            page = tempPage;
        }
        calcRenameList();

        if (open) {
            updateSearchRequest(page);
            searchField.setFocusUnlocked(true);
        }
    }

    public void updateName() {
        updateFavoriteButton();
        updateSelected();
    }

    public void itemUpdate(int slotId, ItemStack stack) {
        if (slotId == 0) {
            currentItem = stack.copy();
            if (stack.isEmpty()) {
                Screen screen = client.currentScreen;
                if (screen != null && screen.getFocused() == searchField) {
                    screen.setFocused(null);
                }
                searchField.setFocused(false);
            } else {
                if (!shouldNotUpdateTab) currentTab = Tab.SEARCH;
            }
            if (!open || currentTab != Tab.GLOBAL) {
                screenUpdate();
            } else {
                updateSearchRequest(page);
            }
            updateFavoriteButton();
        }
        if (slotId == 0 || slotId == 1) {
            ghostCraft.reset();
            if (currentItem.isEmpty()) {
                setNameText("");
            }
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

    private void checkForInvChanges() {
        if (inventory.isEmpty()) {
            inventory = getInventory();
            return;
        }
        ArrayList<Item> temp = getInventory();
        boolean equal = true;
        if (temp.size() != inventory.size()) {
            equal = false;
        } else {
            for (int i = 0; i < temp.size(); i++) {
                if (!inventory.get(i).equals(temp.get(i))) {
                    equal = false;
                    break;
                }
            }
        }
        if (equal) return;
        inventory = temp;
        screenUpdate(page);
    }

    public ArrayList<Item> getInventory() {
        ArrayList<Item> inventoryList = new ArrayList<>();
        assert MinecraftClient.getInstance().player != null;
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        for (ItemStack itemStack : inventory.main) {
            inventoryList.add(itemStack.getItem());
        }
        inventoryList.add(currentItem.getItem());
        return inventoryList;
    }

    private int randomNumber() {
        assert client != null && client.player != null;
        return client.player.getRandom().nextBetween(0, Integer.MAX_VALUE - 1);
    }

    public Item firstItemInInventory(AbstractRename rename) {
        Item item;
        if (currentTab == Tab.SEARCH) {
            item = getItemInFirstSlot();
        } else {
            item = rename.getItem();
            for (Item i : rename.getItems()) {
                if (inventory.contains(i)) {
                    item = i;
                    break;
                }
            }
        }
        return item;
    }

    private void updateFavoriteButton() {
        updateFavoriteButton(getNameText());
    }

    private void updateFavoriteButton(String name) {
        updateFavoriteButton(name, getItemInFirstSlot());
    }

    private void updateFavoriteButton(String name, Item item) {
        if (!name.isEmpty()) {
            favoriteButton.active = true;
            boolean favorite = FavoritesManager.isFavorite(item, name);
            favoriteButton.setFavorite(favorite);
        } else {
            favoriteButton.active = false;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!open) {
            searchField.setFocused(false);
            return;
        }
        checkForInvChanges();

        RenderSystem.enableDepthTest();
        context.drawTexture(
                MENU_TEXTURE,
                this.x + MENU_START_X, this.y, 0,
                0, 0,
                MENU_TEXTURE_WIDTH, MENU_TEXTURE_HEIGHT,
                MENU_TEXTURE_WIDTH, MENU_TEXTURE_HEIGHT
        );

        if (searchField != null && !searchField.isFocused() && searchField.getText().isEmpty()) {
            Graphics.renderText(context, SEARCH_HINT_TEXT, -1, this.x + MENU_START_X + SEARCH_FIELD_X_OFFSET, this.y + 15, true, false);
        }

        if (currentRenameList.isEmpty()) {
            String key;
            if (getItemInFirstSlot() == Items.AIR && (currentTab == Tab.FAVORITE || currentTab == Tab.SEARCH)) {
                key = "putItem";
            } else {
                key = currentTab == Tab.FAVORITE ? "noFavoriteRenamesFound" : "noRenamesFound";
            }
            Graphics.renderText(context,
                    Text.translatable("rprenames.gui." + key).copy()
                            .fillStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY)),
                    -1,
                    this.x + MENU_START_X + (MENU_TEXTURE_WIDTH / 2), this.y + 37,
                    true, true);
        } else {
            Graphics.renderText(context, pageCount,
                    this.x + MENU_START_X + (MENU_TEXTURE_WIDTH / 2),
                    this.y + 140,
                    false, true);
        }
        for (RenameButton renameButton : buttons) {
            renameButton.render(context, mouseX, mouseY, 0);
        }
        for (Drawable widget : widgets) {
            widget.render(context, mouseX, mouseY, 0);
        }
        for (RenameButton renameButton : buttons) {
            renameButton.postRender(context, mouseX, mouseY);
        }
        RenderSystem.disableDepthTest();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (open) {
            Screen screen =  client.currentScreen;
            for (Element widget : widgets) {
                if (widget.mouseClicked(mouseX, mouseY, button)) {
                    if (widget == searchField && screen != null) {
                        screen.setFocused(searchField);
                    }
                    return true;
                } else {
                    if (widget == searchField && screen != null) {
                        if (screen.getFocused() == searchField) screen.setFocused(null);
                    }
                }
            }
            for (RenameButton renameButton : buttons) {
                if (renameButton.mouseClicked(mouseX, mouseY, button)) return true;
            }
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
    public boolean isFocused() {return false;}

    private void calcRenameList() {
        switch (currentTab) {
            case SEARCH -> originalRenameList = RenamesManager.getRenames(getItemInFirstSlot());
            case FAVORITE -> originalRenameList = FavoritesManager.getFavorites(getItemInFirstSlot());
            case INVENTORY -> {
                ArrayList<Item> checked = new ArrayList<>();
                ArrayList<AbstractRename> names = new ArrayList<>();
                for (Item item : inventory) {
                    if (item != Items.AIR && !checked.contains(item)) {
                        checked.add(item);
                        ArrayList<AbstractRename> renames = RenamesManager.getRenames(item);
                        for (AbstractRename r : renames) {
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
        currentRenameList = RenamesHelper.search(originalRenameList, searchTag);

        this.page = page;
        if (this.page >= (currentRenameList.size() + maxPageElements - 1) / maxPageElements) {
            this.page = ((currentRenameList.size() + maxPageElements - 1) / maxPageElements) - 1;
            if (this.page == -1) {
                this.page = 0;
            }
        }

        updateWidgets();
    }

    private RenameButton createButton(int orderOnPage, AbstractRename rename) {
        boolean favorite = false;
        if (currentTab != Tab.SEARCH) {
            for (Item i : rename.getItems()) {
                if (FavoritesManager.isFavorite(i, rename.getName())) {
                    favorite = true;
                    break;
                }
            }
        } else {
            favorite = FavoritesManager.isFavorite(getItemInFirstSlot(), rename.getName());
        }

        int buttonX = this.x + MENU_START_X + BUTTON_X_OFFSET;
        int buttonY = this.y + 30;
        int x = buttonX + 1 + (orderOnPage % 5 * RenameButton.BUTTON_WIDTH);
        int y = buttonY + 1 + (orderOnPage / 5 * RenameButton.BUTTON_HEIGHT);

        return new RenameButton(
                this, rename,
                x, y,
                favorite);
    }

    private void updateWidgets() {
        updateButtons();
        updateSelected();
        updatePageWidgets();
    }

    private void updateButtons() {
        buttons.clear();
        for (int n = 0; n < maxPageElements; n++) {
            if (n + page * maxPageElements <= currentRenameList.size() - 1) {
                buttons.add(createButton(n, currentRenameList.get(n + page * maxPageElements)));
            }
        }
    }

    private void updateSelected() {
        for (RenameButton button : buttons) {
            if (button == null) continue;
            button.setSelected(button.rename.getItems().contains(getItemInFirstSlot())
                    && button.rename.getName().equals(getNameText()));
        }
    }

    private void updatePageWidgets() {
        pageDown.active = page > 0;
        pageUp.active = (page + 1) * maxPageElements <= currentRenameList.size() - 1;
        pageCount = Text.of(page + 1 + "/" + (currentRenameList.size() + maxPageElements - 1) / maxPageElements);
    }

    private void onSearch(String search) {
        searchTag = search;
        if (open) {
            updateSearchRequest();
        }
    }

    public enum Tab {
        SEARCH,
        FAVORITE,
        INVENTORY,
        GLOBAL
    }
}
