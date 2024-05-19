package com.HiWord9.RPRenames.util.gui.widget;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.Tab;
import com.HiWord9.RPRenames.util.config.FavoritesManager;
import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.util.gui.widget.button.*;
import com.HiWord9.RPRenames.util.rename.Rename;
import com.HiWord9.RPRenames.util.rename.RenamesHelper;
import com.HiWord9.RPRenames.util.rename.RenamesManager;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    ArrayList<Rename> originalRenameList = new ArrayList<>();
    ArrayList<Rename> currentRenameList = new ArrayList<>();

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

    public void onRenameButton(int button, boolean favorite,
                               int indexInInventory, boolean isInInventory,
                               boolean asCurrentItem,
                               Rename rename,
                               boolean enoughStackSize, boolean enoughDamage, boolean hasEnchant, boolean hasEnoughLevels) {
        if (button == 1 && !rename.getItems().isEmpty()) {
            if (getCurrentTab() == Tab.SEARCH || getCurrentTab() == Tab.FAVORITE || asCurrentItem) {
                addOrRemoveFavorite(
                        !favorite,
                        rename.getName(),
                        getItemInFirstSlot()
                );
            } else {
                if (favorite) {
                    for (Item item : rename.getItems()) {
                        if (FavoritesManager.isFavorite(item, rename.getName())) {
                            FavoritesManager.removeFromFavorites(rename.getName(), item);
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
                            isInInventory ? inventory.get(indexInInventory) : rename.getItems().get(0)
                    );
                }
            }

            return;
        }

        connectionGhostCraft.reset();
        if (indexInInventory != 36 && isInInventory) {
            shouldNotUpdateTab = getCurrentTab() == Tab.INVENTORY || getCurrentTab() == Tab.GLOBAL;
            tempPage = page;
            if (!asCurrentItem) {
                connectionSlotMovement.putInWorkSlot(indexInInventory);
            }
            shouldNotUpdateTab = false;
        } else if (indexInInventory != 36) {
            for (int s = 0; s < 2; s++) {
                connectionSlotMovement.takeFromWorkSlot(s);
            }

            ItemStack[] ghostCraftItems = RenamesHelper.getGhostCraftItems(rename);

            connectionGhostCraft.setStacks(ghostCraftItems[0], ghostCraftItems[1], ghostCraftItems[2]);
            connectionGhostCraft.setRender(true);
        } else {
            connectionSlotMovement.takeFromWorkSlot(1);
        }
        if (isInInventory) {
            if (!enoughStackSize || !enoughDamage) {
                connectionGhostCraft.setSpecialHighlight(true, null, true);
                connectionGhostCraft.setRender(true);
            }
            if (!hasEnchant || !hasEnoughLevels) {
                connectionGhostCraft.setStacks(ItemStack.EMPTY, RenamesHelper.getGhostCraftEnchant(rename), ItemStack.EMPTY);
                connectionGhostCraft.setSpecialHighlight(null, null, true);
                connectionGhostCraft.setRender(true);
            }
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

    private ArrayList<Item> getInventory() {
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
                ArrayList<Rename> names = new ArrayList<>();
                for (Item item : inventory) {
                    if (item != Items.AIR && !checked.contains(item)) {
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

    private RenameButton createButton(int orderOnPage, Rename rename) {
        if (MinecraftClient.getInstance().player == null) return null;
        PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
        Item item;
        if (currentTab == Tab.SEARCH) {
            item = getItemInFirstSlot();
        } else {
            item = rename.getItems().get(0);
            for (Item i : rename.getItems()) {
                if (inventory.contains(i)) {
                    item = i;
                    break;
                }
            }
        }
        boolean asCurrentItem = item == getItemInFirstSlot();
        boolean isInInventory = inventory.contains(item);
        int indexInInventory = inventory.indexOf(item);
        boolean favorite = false;
        if (currentTab != Tab.SEARCH) {
            for (Item i : rename.getItems()) {
                if (FavoritesManager.isFavorite(i, rename.getName())) {
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

        if ((currentTab == Tab.INVENTORY || currentTab == Tab.GLOBAL)) {
            ArrayList<MultiItemTooltipComponent.TooltipItem> tooltipItems = new ArrayList<>();
            for (int i = 0; i < rename.getItems().size(); i++) {
                ItemStack itemStack = RenamesHelper.createItem(rename, false, i);
                tooltipItems.add(new MultiItemTooltipComponent.TooltipItem(itemStack, inventory.contains(rename.getItems().get(i))));
            }
            tooltip.add(new MultiItemTooltipComponent(tooltipItems));
        }

        if (item == Items.NAME_TAG && rename.isCEM()) {
            Identifier mob = new Identifier(rename.getMob().entity());
            var entityType = Registries.ENTITY_TYPE.get(mob);
            tooltip.add(Text.translatable(entityType.getTranslationKey()).copy().fillStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
        }

        boolean enoughStackSize = true;
        boolean enoughDamage = true;
        boolean hasEnchant = false;
        boolean hasEnoughLevels = false;

        if (rename.getStackSize() > 1) {
            if (!(currentTab == Tab.INVENTORY || currentTab == Tab.GLOBAL) || asCurrentItem) {
                enoughStackSize = PropertiesHelper.matchesRange(currentItem.getCount(), rename.getOriginalStackSize());
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
            if (!(currentTab == Tab.INVENTORY || currentTab == Tab.GLOBAL) || asCurrentItem) {
                enoughDamage = PropertiesHelper.matchesRange(currentItem.getDamage(), rename.getOriginalDamage(), item);
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

            if (!(currentTab == Tab.INVENTORY || currentTab == Tab.GLOBAL) || asCurrentItem) {
                enchantments = EnchantmentHelper.fromNbt(currentItem.getEnchantments());
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
            if (currentTab == Tab.GLOBAL && !isInInventory) {
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
        if (config.showNbtDisplayName && currentTab != Tab.FAVORITE && rename.getOriginalNbtDisplayName() != null) {
            tooltip.add(Text.of("nbt.display.Name=" + rename.getOriginalNbtDisplayName()).copy().fillStyle(Style.EMPTY.withColor(Formatting.BLUE)));
        }

        int buttonX = this.x + MENU_START_X + BUTTON_X_OFFSET;
        int buttonY = this.y + 30;
        int x = buttonX + 1 + (orderOnPage % 5 * RenameButton.BUTTON_WIDTH);
        int y = buttonY + 1 + (orderOnPage / 5 * RenameButton.BUTTON_HEIGHT);

        ArrayList<TooltipComponent> tooltipComponents = new ArrayList<>();
        for (Object o : tooltip) {
            if (o instanceof Text) {
                tooltipComponents.add(TooltipComponent.of(((Text) o).asOrderedText()));
            } else if (o instanceof TooltipComponent tooltipComponent) {
                tooltipComponents.add(tooltipComponent);
            }
        }

        return new RenameButton(
                this, rename, tooltipComponents,
                x, y,
                favorite,
                indexInInventory, isInInventory, asCurrentItem,
                enoughStackSize, enoughDamage,
                hasEnchant, hasEnoughLevels);
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
