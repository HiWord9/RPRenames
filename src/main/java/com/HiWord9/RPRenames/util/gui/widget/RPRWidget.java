package com.HiWord9.RPRenames.util.gui.widget;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.FavoritesManager;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.widget.button.*;
import com.HiWord9.RPRenames.util.rename.*;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
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

public class RPRWidget implements Drawable {
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
    Screen screen;
    int x;
    int y;

    ConnectionName connectionName = new ConnectionName() {
        public String getText() {return "";}
        public void setText(String name) {}
    };
    ConnectionSlotMovement connectionSlotMovement = new ConnectionSlotMovement() {
        public void putInWorkSlot(int slotInInventory) {}
        public void takeFromWorkSlot(int slotInWorkspace) {}
    };
    ConnectionFavoriteButton connectionFavoriteButton = (name, item) -> {};
    ConnectionGhostCraft connectionGhostCraft = new ConnectionGhostCraft() {
        public void reset() {}
        public void setStacks(ItemStack stack1, ItemStack stack2, ItemStack stack3) {}
        public void setRender(boolean doRender) {}
        public void setSpecialHighlight(Boolean highlightSlot1, Boolean highlightSlot2, Boolean highlightSlot3) {}
        public ItemStack getStackInFirstSlot() {return ItemStack.EMPTY;}
    };

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

    public void init(MinecraftClient minecraftClient, @Nullable Screen parentScreen, int x, int y) {
        this.init = true;

        this.client = minecraftClient;
        this.screen = parentScreen;
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

        screenUpdate();
    }

    public void connect(
            ConnectionName connectionName,
            ConnectionSlotMovement connectionSlotMovement,
            ConnectionFavoriteButton connectionFavoriteButton,
            ConnectionGhostCraft connectionGhostCraft
    ) {
        if (connectionName != null) {
            this.connectionName = connectionName;
        }
        if (connectionSlotMovement != null) {
            this.connectionSlotMovement = connectionSlotMovement;
        }
        if (connectionFavoriteButton != null) {
            this.connectionFavoriteButton = connectionFavoriteButton;
        }
        if (connectionGhostCraft != null) {
            this.connectionGhostCraft = connectionGhostCraft;
        }

        favoriteButtonUpdate(this.connectionName.getText());
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
    }

    public void open() {
        open = true;
        if (currentItem.isEmpty()) {
            currentTab = Tab.GLOBAL;
        } else {
            currentTab = Tab.SEARCH;
        }
        screenUpdate();
    }

    public void close() {
        open = false;
        searchField.setFocused(false);
        searchField.setFocusUnlocked(false);
        searchField.setText("");
        updateWidgets();
        currentTab = Tab.SEARCH;
    }

    public void addOrRemoveFavorite(boolean add) {
        addOrRemoveFavorite(add, connectionName.getText(), getItemInFirstSlot());
    }

    public void addOrRemoveFavorite(boolean add, String favoriteName, Item item) {
        if (item != Items.AIR) {
            if (add) {
                FavoritesManager.addToFavorites(favoriteName, item);
            } else {
                FavoritesManager.removeFromFavorites(favoriteName, item);
            }
            favoriteButtonUpdate(connectionName.getText());
            if (open) {
                screenUpdate(page);
            }
        }
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

    public void onRenameButton(int button, boolean favorite, AbstractRename rename) {
        Item item = firstItemInInventory(rename);
        boolean asCurrentItem = item == getItemInFirstSlot();
        int indexInInventory = inventory.indexOf(item);
        boolean isInInventory = indexInInventory != -1;

        if (button == 1 && rename.getItem() != null) {
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
                    favoriteButtonUpdate(connectionName.getText());
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

            return;
        }

        connectionGhostCraft.reset();
        if (isInInventory) {
            if (indexInInventory != 36) { //in inventory
                shouldNotUpdateTab = getCurrentTab() == Tab.INVENTORY || getCurrentTab() == Tab.GLOBAL;
                tempPage = page;
                if (!asCurrentItem) {
                    connectionSlotMovement.putInWorkSlot(indexInInventory);
                }
                shouldNotUpdateTab = false;
            } else { //in work slot
                connectionSlotMovement.takeFromWorkSlot(1);
            }

            if (rename instanceof CITRename citRename) {
                CITRename.CraftMatcher craftMatcher = new CITRename.CraftMatcher(citRename, currentItem);
                if (!craftMatcher.enoughStackSize() || !craftMatcher.enoughDamage()) {
                    connectionGhostCraft.setSpecialHighlight(true, null, true);
                    connectionGhostCraft.setRender(true);
                }
                if (!craftMatcher.hasEnchant() || !craftMatcher.hasEnoughLevels()) {
                    connectionGhostCraft.setStacks(ItemStack.EMPTY, RenamesHelper.getGhostCraftEnchant(citRename), ItemStack.EMPTY);
                    connectionGhostCraft.setSpecialHighlight(null, null, true);
                    connectionGhostCraft.setRender(true);
                }
            }
        } else { //not in inventory
            for (int s = 0; s < 2; s++) {
                connectionSlotMovement.takeFromWorkSlot(s);
            }

            ItemStack[] ghostCraftItems = RenamesHelper.getGhostCraftItems(rename);

            connectionGhostCraft.setStacks(ghostCraftItems[0], ghostCraftItems[1], ghostCraftItems[2]);
            connectionGhostCraft.setRender(true);
        }

        connectionName.setText(rename.getName());
    }

    public void onPageDown() {
        setPage(hasShiftDown() ? 0 : page - 1);
    }

    public void onPageUp() {
        setPage(hasShiftDown() ? ((currentRenameList.size() + maxPageElements - 1) / maxPageElements - 1) : page + 1);
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

    public Screen getScreen() {
        return screen;
    }

    public ItemStack getCurrentItem() {
        return currentItem;
    }

    public Item getItemInFirstSlot() {
        Item item = currentItem.getItem();
        if (item == Items.AIR && !connectionGhostCraft.getStackInFirstSlot().isEmpty()) {
            item = connectionGhostCraft.getStackInFirstSlot().getItem();
        }
        return item;
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

    public void onNameUpdate(String name) {
        favoriteButtonUpdate(name);
        updateSelected();
    }

    public void onItemUpdate(int slotId, ItemStack stack) {
        if (slotId == 0) {
            currentItem = stack.copy();
            if (stack.isEmpty()) {
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
        }
        if (slotId == 0 || slotId == 1) {
            connectionGhostCraft.reset();
            if (currentItem.isEmpty()) {
                connectionName.setText("");
            }
        }
    }

    public void offsetX(int x) {
        this.x += x;

        for (ClickableWidget widget : widgets) {
            widget.setX(widget.getX() + x);
        }
        for (RenameButton renameButton : buttons) {
            if (renameButton == null) continue;
            renameButton.setX(renameButton.getX() + x);
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!open) {
            searchField.setFocused(false);
            return;
        }
        checkForInvChanges();

        RenderSystem.enableDepthTest();
        context.drawTexture(MENU_TEXTURE, this.x + MENU_START_X, this.y, 0, 0, 0, MENU_TEXTURE_WIDTH, WIDGET_HEIGHT, MENU_TEXTURE_WIDTH, WIDGET_HEIGHT);

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
        for (ClickableWidget widget : widgets) {
            widget.render(context, mouseX, mouseY, 0);
        }
        for (RenameButton renameButton : buttons) {
            renameButton.postRender(context, mouseX, mouseY);
        }
        RenderSystem.disableDepthTest();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (open) {
            for (ClickableWidget widget : widgets) {
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

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ClickableWidget widget : widgets) {
            if (widget.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
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

    private void favoriteButtonUpdate(String name) {
        connectionFavoriteButton.update(name, getItemInFirstSlot());
    }

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
        defineButtons();
        updateSelected();
        updatePageWidgets();
    }

    private void defineButtons() {
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
                    && button.rename.getName().equals(connectionName.getText()));
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

    public interface ConnectionName {
        String getText();
        void setText(String name);

    }
    public interface ConnectionSlotMovement {
        void putInWorkSlot(int slotInInventory);
        void takeFromWorkSlot(int slotInWorkspace);

    }
    public interface ConnectionFavoriteButton {
        void update(String name, Item item);

    }
    public interface ConnectionGhostCraft {
        void reset();
        void setStacks(ItemStack stack1, ItemStack stack2, ItemStack stack3);
        void setRender(boolean doRender);
        void setSpecialHighlight(Boolean highlightSlot1, Boolean highlightSlot2, Boolean highlightSlot3);
        ItemStack getStackInFirstSlot();

    }
}
