package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.Tabs;
import com.HiWord9.RPRenames.configGeneration.CEMList;
import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.Rename;
import com.HiWord9.RPRenames.configGeneration.ConfigManager;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.google.gson.Gson;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends Screen {

	private static final ModConfig config = ModConfig.INSTANCE;
	boolean isOnServer = !MinecraftClient.getInstance().isInSingleplayer();

	@Shadow private TextFieldWidget nameField;

	protected AnvilScreenMixin(Text title) {
		super(title);
	}

	boolean open = config.openByDefault;

	private static final Identifier MENU_TEXTURE = new Identifier(RPRenames.MOD_ID,"textures/gui/menu.png");
	int menuTextureWidth = 204;
	int menuTextureHeight = 166;
	int menuWidth = 138;
	int menuHeight = 166;
	int tabWidth = 33;
	int tabHeight = 26;
	int tabOffsetY = 5;
	private static final Identifier OPENER_TEXTURE = new Identifier(RPRenames.MOD_ID,"textures/gui/opener.png");
	int openerTextureWidth = 22;
	int openerTextureHeight = 110;
	int openerWidth = 22;
	int openerHeight = 22;
	private static final Identifier BUTTON_TEXTURE = new Identifier(RPRenames.MOD_ID,"textures/gui/button.png");
	int buttonTextureWidth = 118;
	int buttonTextureHeight = 80;
	int buttonWidth = 118;
	int buttonHeight = 20;
	int buttonOffsetY = 2;
	private static final Identifier PAGE_ARROWS_TEXTURE = new Identifier(RPRenames.MOD_ID,"textures/gui/page_arrows.png");
	int pageArrowsTextureWidth = 60;
	int pageArrowsTextureHeight = 48;
	int pageArrowsWidth = 30;
	int pageArrowsHeight = 16;
	private static final Identifier FAVORITE_BUTTON_TEXTURE = new Identifier(RPRenames.MOD_ID,"textures/gui/favorite_button.png");
	int favoriteButtonTextureWidth = 9;
	int favoriteButtonTextureHeight = 18;
	int favoriteButtonWidth = 9;
	int favoriteButtonHeight = 9;

	int slotSize = 18;
	int rowSize = 9;
	int firstSlotX = 7;
	int firstSlotY = 83;
	int highlightColor = config.getSlotHighlightRGBA();

	int page = 0;
	int currentRenameListSize;

	String currentItem = "air";
	ArrayList<String> currentItemList = new ArrayList<>();
	ArrayList<Integer> currentInvOrder = new ArrayList<>();
	boolean afterInventoryTab = false;
	int tempPage;
	int citSize;

	TexturedButtonWidget background;
	TexturedButtonWidget opener;
	TexturedButtonWidget openerOpened;
	TexturedButtonWidget openerPlus;
	TexturedButtonWidget openerMinus;
	TexturedButtonWidget button1;
	TexturedButtonWidget button2;
	TexturedButtonWidget button3;
	TexturedButtonWidget button4;
	TexturedButtonWidget button5;
	TexturedButtonWidget searchTab;
	TexturedButtonWidget favoriteTab;
	TexturedButtonWidget inventoryTab;
	TexturedButtonWidget searchTabActive;
	TexturedButtonWidget favoriteTabActive;
	TexturedButtonWidget inventoryTabActive;
	Tabs currentTab = Tabs.SEARCH;
	TexturedButtonWidget addToFavorite;
	TexturedButtonWidget removeFromFavorite;
	WLabel button1text = new WLabel(Text.of(""),0xffffff);
	WLabel button2text = new WLabel(Text.of(""),0xffffff);
	WLabel button3text = new WLabel(Text.of(""),0xffffff);
	WLabel button4text = new WLabel(Text.of(""),0xffffff);
	WLabel button5text = new WLabel(Text.of(""),0xffffff);
	WLabel button1textShadow = new WLabel(Text.of(""),0x3f3f3f);
	WLabel button2textShadow = new WLabel(Text.of(""),0x3f3f3f);
	WLabel button3textShadow = new WLabel(Text.of(""),0x3f3f3f);
	WLabel button4textShadow = new WLabel(Text.of(""),0x3f3f3f);
	WLabel button5textShadow = new WLabel(Text.of(""),0x3f3f3f);
	TexturedButtonWidget pageDown;
	TexturedButtonWidget pageUp;
	WLabel pageCount = new WLabel(Text.of(""),0xffffff);
	Rename currentRenameList;

	TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

	TextFieldWidget searchField;
	Text SEARCH_HINT_TEXT = Text.translatable("rprenames.gui.searchHintText").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);

	String searchTag = "";

	WItemSlot iconSlot1 = new WItemSlot(new PlayerInventory(MinecraftClient.getInstance().player),51,1,1,false);
	WItemSlot iconSlot2 = new WItemSlot(new PlayerInventory(MinecraftClient.getInstance().player),52,1,1,false);
	WItemSlot iconSlot3 = new WItemSlot(new PlayerInventory(MinecraftClient.getInstance().player),53,1,1,false);
	WItemSlot iconSlot4 = new WItemSlot(new PlayerInventory(MinecraftClient.getInstance().player),54,1,1,false);
	WItemSlot iconSlot5 = new WItemSlot(new PlayerInventory(MinecraftClient.getInstance().player),55,1,1,false);

	ItemStack icon1;
	ItemStack icon2;
	ItemStack icon3;
	ItemStack icon4;
	ItemStack icon5;

	ItemStack iconAfterUpdate1;
	ItemStack iconAfterUpdate2;
	ItemStack iconAfterUpdate3;
	ItemStack iconAfterUpdate4;
	ItemStack iconAfterUpdate5;

	ArrayList<ArrayList<String>> mobName = new ArrayList<>();

	boolean currentItemHasRenames = true;

	@Inject(at = @At("RETURN"), method = "setup")
	private void init(CallbackInfo ci) {
		if (config.enableAnvilModification) {
			RPRenames.LOGGER.info("Starting RPRenames modification on AnvilScreen");

			background = new TexturedButtonWidget(this.width / 2 - 228, this.height / 2 - 83, menuWidth, menuHeight, tabWidth * 2, 0, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, null);
			background.active = false;

			pageDown = new TexturedButtonWidget(this.width / 2 - 218, this.height / 2 + 57, pageArrowsWidth, pageArrowsHeight, 0, 0, pageArrowsHeight, PAGE_ARROWS_TEXTURE, pageArrowsTextureWidth, pageArrowsTextureHeight, (button -> {
				if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
					page = 0;
				} else {
					page--;
				}
				hideButtons();
				buttonsDefine();
				addDrawableChild(background);
				showButtons();
				tabsUpdate();
				updatePageWidgets();
				if (page == 0) {
					button.active = false;
				}
				pageUp.active = true;
			}));
			pageUp = new TexturedButtonWidget(this.width / 2 - 130, this.height / 2 + 57, pageArrowsWidth, pageArrowsHeight, pageArrowsWidth, 0, pageArrowsHeight, PAGE_ARROWS_TEXTURE, pageArrowsTextureWidth, pageArrowsTextureHeight, (button -> {
				if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
					page = ((currentRenameList.getName().length + 4) / 5 - 1);
				} else {
					page++;
				}
				hideButtons();
				buttonsDefine();
				addDrawableChild(background);
				showButtons();
				tabsUpdate();
				updatePageWidgets();
				pageDown.active = true;
				if (5 + page * 5 > currentRenameListSize - 1) {
					button.active = false;
				}
			}));

			boolean clientConfigReadable = RPRenames.configClientFolder.exists();
			boolean serverConfigReadable = RPRenames.configServerFolder.exists();
			if (!isOnServer) {
				serverConfigReadable = false;
			}

			if (!clientConfigReadable && !serverConfigReadable) {
				Text noConfigText = Text.translatable("rprenames.configrenames.notfound", RPRenames.configPath);
				opener = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, 0, 0, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, (button) -> switchOpen(), new ButtonWidget.TooltipSupplier() {
					public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int i, int j) {
						renderTooltip(matrixStack, noConfigText, i, j);
					}

					public void supply(Consumer<Text> consumer) {
						consumer.accept(noConfigText);
					}
				}, noConfigText);
			} else {
				opener = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, 0, 0, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, (button) -> switchOpen());
			}

			openerOpened = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, openerHeight * 3, openerHeight, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, null);
			openerPlus = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, 0, openerHeight, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, null);
			openerMinus = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, openerHeight * 2, 0, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, null);

			addDrawableChild(opener);

			searchTab = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78, tabWidth, tabHeight, 0, 0, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
				currentTab = Tabs.SEARCH;
				remove(searchTabActive);
				remove(favoriteTabActive);
				remove(inventoryTabActive);
				addDrawableChild(searchTabActive);
				screenUpdate();
			});
			favoriteTab = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78 + (tabHeight + tabOffsetY), tabWidth, tabHeight, 0, tabHeight, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
				currentTab = Tabs.FAVORITE;
				remove(searchTabActive);
				remove(favoriteTabActive);
				remove(inventoryTabActive);
				addDrawableChild(favoriteTabActive);
				screenUpdate();
			});
			inventoryTab = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78 + (tabHeight + tabOffsetY) * 2, tabWidth, tabHeight, 0, tabHeight * 2, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
				currentTab = Tabs.INVENTORY;
				remove(searchTabActive);
				remove(favoriteTabActive);
				remove(inventoryTabActive);
				addDrawableChild(inventoryTabActive);
				screenUpdate();
			});
			searchTabActive = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78, tabWidth, tabHeight, tabWidth, 0, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, null);
			favoriteTabActive = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78 + (tabHeight + tabOffsetY), tabWidth, tabHeight, tabWidth, tabHeight, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, null);
			inventoryTabActive = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78 + (tabHeight + tabOffsetY) * 2, tabWidth, tabHeight, tabWidth, tabHeight * 2, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, null);
			searchTabActive.active = false;
			favoriteTabActive.active = false;
			inventoryTabActive.active = false;

			addToFavorite = new TexturedButtonWidget(this.width / 2 + config.favoritePosX, this.height / 2 + config.favoritePosY, favoriteButtonWidth, favoriteButtonHeight, 0, favoriteButtonHeight, 0, FAVORITE_BUTTON_TEXTURE, favoriteButtonTextureWidth, favoriteButtonTextureHeight, button -> {
				String favoriteName = nameField.getText();

				String item = currentItem;

				File currentFile = new File(RPRenames.configPathFavorite + item + ".json");
				boolean nameExist = false;
				if (currentFile.exists() && favoriteName != null) {
					Rename alreadyExist = ConfigManager.configRead(currentFile);
					String[] ae = alreadyExist.getName();
					for (String s : ae) {
						if (favoriteName.equals(s)) {
							nameExist = true;
							break;
						}
					}
					if (!nameExist) {
						int AEsize = ae.length;
						String[] newConfig = new String[AEsize + 1];
						int h = 0;
						while (h < AEsize) {
							newConfig[h] = ae[h];
							h++;
						}
						newConfig[h] = favoriteName;

						Rename newRename = new Rename(newConfig);
						ArrayList<Rename> listFiles = new ArrayList<>();
						listFiles.add(newRename);

						try {
							FileWriter fileWriter = new FileWriter(currentFile);
							Gson gson = new Gson();
							gson.toJson(listFiles, fileWriter);
							fileWriter.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					if (favoriteName != null) {
						try {
							new File(RPRenames.configPathFavorite).mkdirs();
							System.out.println("[RPR] Created new file for favorites config: " + RPRenames.configPathFavorite + item + ".json");
							ArrayList<Rename> listNames = new ArrayList<>();
							Rename name1 = new Rename(new String[]{favoriteName});
							listNames.add(name1);
							FileWriter fileWriter = new FileWriter(currentFile);
							Gson gson = new Gson();
							gson.toJson(listNames, fileWriter);
							fileWriter.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				featuredButtonsUpdate(nameField.getText());
				if (open) {
					screenUpdate(page);
				}
			});
			removeFromFavorite = new TexturedButtonWidget(this.width / 2 + config.favoritePosX, this.height / 2 + config.favoritePosY, favoriteButtonWidth, favoriteButtonHeight, 0, 0, 0, FAVORITE_BUTTON_TEXTURE, favoriteButtonTextureWidth, favoriteButtonTextureHeight, button -> {
				String favoriteName = nameField.getText();

				String item = currentItem;

				File currentFile = new File(RPRenames.configPathFavorite + item + ".json");
				Rename alreadyExist = ConfigManager.configRead(currentFile);
				String[] ae = alreadyExist.getName();
				int n = 0;
				for (String s : ae) {
					if (favoriteName.equals(s)) {
						ae[n] = null;
					}
					n++;
				}
				int AEsize = ae.length;
				String[] newConfig = new String[AEsize - 1];
				int h = 0;
				int h2 = 0;
				while (h2 < newConfig.length) {
					if (ae[h] != null) {
						newConfig[h2] = ae[h];
						h++;
						h2++;
					} else {
						h++;
					}
				}

				Rename newRename = new Rename(newConfig);
				ArrayList<Rename> listFiles = new ArrayList<>();
				listFiles.add(newRename);

				try {
					FileWriter fileWriter = new FileWriter(currentFile);
					Gson gson = new Gson();
					gson.toJson(listFiles, fileWriter);
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				featuredButtonsUpdate(nameField.getText());
				if (open) {
					screenUpdate(page);
				}
			});

			searchField = new TextFieldWidget(renderer, this.width / 2 - 204, this.height / 2 - 68, 100, 10, Text.of(""));
			searchField.setChangedListener(this::onSearch);
			searchField.setDrawsBackground(false);

			screenUpdate();
		}
	}

	private void switchOpen() {
		if (!open) {
			open = true;
			RPRenames.LOGGER.info("Opening RPRenames Menu");
			screenUpdate();
		} else {
			open = false;
			clearAll();
			searchField.setTextFieldFocused(false);
			searchField.setFocusUnlocked(false);
			searchField.setText("");
			remove(searchField);
			nameField.setTextFieldFocused(true);
			nameField.setFocusUnlocked(false);
			remove(openerOpened);
			remove(searchTab);
			remove(favoriteTab);
			remove(searchTabActive);
			remove(favoriteTabActive);
			currentTab = Tabs.SEARCH;
			RPRenames.LOGGER.info("Closing RPRenames Menu");
		}
	}

	private void screenUpdate() {
		screenUpdate(0);
	}

	private void screenUpdate(Integer savedPage) {
		clearAll();
		page = savedPage;
		opener.active = true;
		mobName.clear();
		if (afterInventoryTab) {
			currentTab = Tabs.INVENTORY;
			page = tempPage;
		}
		boolean showOpenerMinus = false;
		boolean showOpenerPlus = false;
		File jsonRenamesClient = new File(RPRenames.configPathClient + RPRenames.configPathNameCIT + "/" + currentItem + ".json");
		File jsonRenamesServer = new File(RPRenames.configPathServer + RPRenames.configPathNameCIT + "/" + currentItem + ".json");
		File jsonRenamesFavorite = new File(RPRenames.configPathFavorite + currentItem + ".json");
		boolean clientConfigReadable = jsonRenamesClient.exists();
		boolean serverConfigReadable = jsonRenamesServer.exists();
		boolean clientConfigCEMReadable = RPRenames.configClientCEMFolder.exists();
		boolean serverConfigCEMReadable = RPRenames.configServerCEMFolder.exists();
		if (!isOnServer) {
			serverConfigReadable = false;
			serverConfigCEMReadable = false;
		}
		String actualSearch = searchField.getText();
		currentItemHasRenames = true;
		if (!currentItem.equals("air")) {
			showOpenerPlus = true;
			if (clientConfigReadable || serverConfigReadable) {
				if (currentTab == Tabs.SEARCH) {
					List<String> list1 = new ArrayList<>();
					List<String> list2 = new ArrayList<>();
					if (clientConfigReadable) {
						list1 = Arrays.stream(search(ConfigManager.configRead(jsonRenamesClient).getName(), actualSearch)).toList();
					}
					if (serverConfigReadable) {
						list2 = Arrays.stream(search(ConfigManager.configRead(jsonRenamesServer).getName(), actualSearch)).toList();
					}
					String[] list3 = new String[list1.size() + list2.size()];
					int n = 0;
					for (String s : list1) {
						list3[n] = s;
						n++;
					}
					for (String s : list2) {
						list3[n] = s;
						n++;
					}
					currentRenameList = new Rename(list3);
					if (currentItem.equals("name_tag")) {
						citSize = currentRenameList.getName().length;
						List<ArrayList<String>> settings = getCEM(currentRenameList, clientConfigCEMReadable, serverConfigCEMReadable, actualSearch);
						String[] finalList = new String[settings.get(1).size() + settings.get(0).size()];
						int g = 0;
						for (String s : settings.get(1)) {
							finalList[g] = s;
							g++;
						}
						g = 0;
						for (String s : settings.get(0)) {
							finalList[settings.get(1).size() + g] = s;
							g++;
						}
						if (g != 0) {
							currentRenameList = new Rename(finalList);
						}
					}
				}
			} else if ((clientConfigCEMReadable || serverConfigCEMReadable) && currentItem.equals("name_tag")) {
				if (currentTab == Tabs.SEARCH) {
					citSize = 0;
					List<ArrayList<String>> settings = getCEM(currentRenameList, clientConfigCEMReadable, serverConfigCEMReadable, actualSearch);
					String[] finalList = new String[settings.get(0).size()];
					int g = 0;
					for (String s : settings.get(0)) {
						finalList[g] = s;
						g++;
					}
					currentRenameList = new Rename(finalList);
				}
			} else {
				showOpenerMinus = true;
				currentItemHasRenames = false;
				if (currentTab == Tabs.SEARCH) {
					currentTab = Tabs.FAVORITE;
				}
			}
		} else {
			showOpenerMinus = true;
			if (currentTab == Tabs.SEARCH || currentTab == Tabs.FAVORITE) {
				currentTab = Tabs.INVENTORY;
			}
		}

		if (!showOpenerPlus) {
			currentItemList = getInventory();
			currentInvOrder.clear();
			ArrayList<String> checked = new ArrayList<>();
			for (String s : currentItemList) {
				if (!s.equals("air") && !checked.contains(s)) {
					File jsonRenamesClientLocal = new File(RPRenames.configPathClient + RPRenames.configPathNameCIT + "/" + s + ".json");
					File jsonRenamesServerLocal = new File(RPRenames.configPathServer + RPRenames.configPathNameCIT + "/" + s + ".json");
					boolean clientConfigReadableLocal = jsonRenamesClientLocal.exists();
					boolean serverConfigReadableLocal = jsonRenamesServerLocal.exists();
					boolean clientConfigCEMReadableLocal = RPRenames.configClientCEMFolder.exists();
					boolean serverConfigCEMReadableLocal = RPRenames.configServerCEMFolder.exists();
					if (!isOnServer) {
						serverConfigReadableLocal = false;
						serverConfigCEMReadableLocal = false;
					}
					if (!s.equals("name_tag")) {
						clientConfigCEMReadableLocal = false;
						serverConfigCEMReadableLocal = false;
					}
					checked.add(s);
					if (clientConfigReadableLocal || serverConfigReadableLocal || clientConfigCEMReadableLocal || serverConfigCEMReadableLocal) {
						showOpenerPlus = true;
						break;
					}
				}
			}
		}

		if (currentTab == Tabs.FAVORITE) {
			if (jsonRenamesFavorite.exists()) {
				currentRenameList = new Rename(search(ConfigManager.configRead(jsonRenamesFavorite).getName(), actualSearch));
			} else {
				currentRenameList = new Rename(new String[0]);
			}
		}
		if (currentTab == Tabs.INVENTORY) {
			currentItemList = getInventory();
			currentInvOrder.clear();
			ArrayList<String> checked = new ArrayList<>();
			ArrayList<String> names = new ArrayList<>();
			ArrayList<Integer> numInInv = new ArrayList<>();
			int i = 0;
			for (String s : currentItemList) {
				String itemSearchTag = "";
				if (searchField.getText().startsWith("+")) {
					itemSearchTag = searchField.getText().substring(1);
				}
				if (!s.equals("air") && !checked.contains(s) && s.toUpperCase(Locale.ROOT).contains(itemSearchTag.toUpperCase(Locale.ROOT))) {
					File jsonRenamesClientLocal = new File(RPRenames.configPathClient + RPRenames.configPathNameCIT + "/" + s + ".json");
					File jsonRenamesServerLocal = new File(RPRenames.configPathServer + RPRenames.configPathNameCIT + "/" + s + ".json");
					boolean clientConfigReadableLocal = jsonRenamesClientLocal.exists();
					boolean serverConfigReadableLocal = jsonRenamesServerLocal.exists();
					boolean clientConfigCEMReadableLocal = RPRenames.configClientCEMFolder.exists();
					boolean serverConfigCEMReadableLocal = RPRenames.configServerCEMFolder.exists();
					if (!isOnServer) {
						serverConfigReadableLocal = false;
						serverConfigCEMReadableLocal = false;
					}
					checked.add(s);
					if (clientConfigReadableLocal) {
						File a = new File(RPRenames.configPathClient + RPRenames.configPathNameCIT + "/" + s + ".json");
						if (a.exists()) {
							List<String> c = Arrays.stream(new Rename(search(ConfigManager.configRead(a).getName(), searchTag)).getName()).toList();
							for (String name : c) {
								names.add(name);
								numInInv.add(i);
							}
						}
					}
					if (serverConfigReadableLocal) {
						File a = new File(RPRenames.configPathServer + RPRenames.configPathNameCIT + "/" + s + ".json");
						if (a.exists()) {
							List<String> c = Arrays.stream(new Rename(search(ConfigManager.configRead(a).getName(), searchTag)).getName()).toList();
							for (String name : c) {
								names.add(name);
								numInInv.add(i);
							}
						}
					}
					if (s.equals("name_tag")) {
						List<ArrayList<String>> settings = getCEM(currentRenameList, clientConfigCEMReadableLocal, serverConfigCEMReadableLocal, searchTag);
						citSize = names.size();
						for (String name : settings.get(0)) {
							names.add(name);
							numInInv.add(i);
						}
					}
				}
				i++;
			}
			currentRenameList = new Rename(names.toArray(new String[0]));
			currentInvOrder = numInInv;
		}

		if (page >= (currentRenameList.getName().length + 4) / 5) {
			page = ((currentRenameList.getName().length + 4) / 5) - 1;
			if (page == -1) {
				page = 0;
			}
		}
		currentRenameListSize = currentRenameList.getName().length;
		buttonsDefine();
		clearAll();
		if (showOpenerMinus) {
			remove(openerMinus);
			addDrawableChild(openerMinus);
		}
		if (showOpenerPlus) {
			remove(openerPlus);
			addDrawableChild(openerPlus);
		}

		if (open) {
			remove(background);
			addDrawableChild(background);
			remove(openerOpened);
			addDrawableChild(openerOpened);
			showButtons();
			updatePageWidgets();
			addDrawableChild(searchField);
			searchField.setFocusUnlocked(true);
			nameField.setTextFieldFocused(false);
			nameField.setFocusUnlocked(true);
			tabsUpdate();
		}
	}

	private List<ArrayList<String>> getCEM(Rename currentRenameList, boolean clientConfigCEMReadable, boolean serverConfigCEMReadable, String searchTag) {
		ArrayList<String> modelsArray = new ArrayList<>();
		if (clientConfigCEMReadable) {
			try {
				Files.walk(Path.of(RPRenames.configPathClient + RPRenames.configPathNameCEM), new FileVisitOption[0]).filter(path -> path.toString().endsWith(".json")).forEach(jsonFile -> {
					File file = new File(String.valueOf(jsonFile));
					for (String s : ConfigManager.configRead(file).getName()) {
						if (Arrays.stream(search(ConfigManager.configRead(file).getName(), searchTag)).toList().contains(s)) {
							if (!modelsArray.contains(s)) {
								modelsArray.add(s);
								ArrayList<String> nal = new ArrayList<>();
								nal.add(file.getName().substring(0, file.getName().length() - 5));
								mobName.add(nal);
							} else {
								int n = 0;
								for (String s2 : modelsArray) {
									if (s2.equals(s)) {
										break;
									}
									n++;
								}
								ArrayList<String> nal = mobName.get(n);
								nal.add(file.getName().substring(0, file.getName().length() - 5));
								mobName.set(n, nal);
							}
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (serverConfigCEMReadable) {
			try {
				Files.walk(Path.of(RPRenames.configPathServer + RPRenames.configPathNameCEM), new FileVisitOption[0]).filter(path -> path.toString().endsWith(".json")).forEach(jsonFile -> {
					File file = new File(String.valueOf(jsonFile));
					for (String s : ConfigManager.configRead(file).getName()) {
						if (Arrays.stream(search(ConfigManager.configRead(file).getName(), searchTag)).toList().contains(s)) {
							if (!modelsArray.contains(s)) {
								modelsArray.add(s);
								ArrayList<String> nal = new ArrayList<>();
								nal.add(file.getName().substring(0, file.getName().length() - 5));
								mobName.add(nal);
							} else {
								int n = 0;
								for (String s2 : modelsArray) {
									if (s2.equals(s)) {
										break;
									}
									n++;
								}
								ArrayList<String> nal = mobName.get(n);
								nal.add(file.getName().substring(0, file.getName().length() - 5));
								mobName.set(n, nal);
							}
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ArrayList<String> citWithoutModels = new ArrayList<>();
		if (currentRenameList != null) {
			for (String s : currentRenameList.getName()) {
				if (!modelsArray.contains(s)) {
					citWithoutModels.add(s);
				}
			}
		}
		List<ArrayList<String>> settings = new ArrayList<>();
		settings.add(modelsArray);
		settings.add(citWithoutModels);
		return settings;
	}

	@Inject(at = @At("RETURN"), method = "onRenamed")
	private void newNameEntered(String name, CallbackInfo ci) {
		if (config.enableAnvilModification) {
			featuredButtonsUpdate(name);
		}
	}

	private void featuredButtonsUpdate(String name) {
		remove(addToFavorite);
		remove(removeFromFavorite);
		if (!name.isEmpty()) {
			File file = new File(RPRenames.configPathFavorite + currentItem + ".json");
			if (file.exists()) {
				String[] favoriteName = ConfigManager.configRead(file).getName();
				boolean nameExist = false;
				for (String s : favoriteName) {
					if (name.equals(s)) {
						nameExist = true;
						break;
					}
				}
				if (nameExist) {
					addDrawableChild(removeFromFavorite);
				} else {
					addDrawableChild(addToFavorite);
				}
			} else {
				addDrawableChild(addToFavorite);
			}
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AnvilScreen;init(Lnet/minecraft/client/MinecraftClient;II)V"), method = "resize")
	private void onResize(AnvilScreen instance, MinecraftClient client, int width, int height){
		if (config.enableAnvilModification) {
			String tempSearchFieldText = searchField.getText();
			instance.init(client, width, height);
			searchField.setText(tempSearchFieldText);
		} else {
			instance.init(client, width, height);
		}
	}

	@Inject(at = @At("RETURN"), method = "onSlotUpdate")
	private void itemUpdate(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
		if (config.enableAnvilModification) {
			if (slotId == 0) {
				if (stack.isEmpty()) {
					currentItem = "air";
					searchField.setText("");
					searchField.setFocusUnlocked(false);
					remove(searchField);
					searchField.setTextFieldFocused(false);
				} else {
					currentItem = cutTranslationKey(stack.getItem().getTranslationKey());
					icon1 = stack.copy();
					icon2 = stack.copy();
					icon3 = stack.copy();
					icon4 = stack.copy();
					icon5 = stack.copy();
					iconAfterUpdate1 = stack.copy();
					iconAfterUpdate2 = stack.copy();
					iconAfterUpdate3 = stack.copy();
					iconAfterUpdate4 = stack.copy();
					iconAfterUpdate5 = stack.copy();
					searchField.setFocusUnlocked(true);
					currentTab = Tabs.SEARCH;
					featuredButtonsUpdate(nameField.getText());
				}
				screenUpdate();
			}
		}
	}

	private ArrayList<String> getInventory() {
		ArrayList<String> inventoryList = new ArrayList<>();
		assert MinecraftClient.getInstance().player != null;
		PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
		int g = 0;
		while (g < inventory.main.size()) {
			String s = inventory.main.get(g).getItem().getTranslationKey();
			int var1 = 0;
			int var2 = 0;
			while (var2 != 2) {
				if (String.valueOf(s.charAt(var1)).equals(".")) {
					s = s.substring(var1 + 1);
					var2++;
					var1 = 0;
				} else {
					var1++;
				}
			}
			inventoryList.add(s);
			g++;
		}
		inventoryList.add(currentItem);
		return inventoryList;
	}

	ArrayList<String> invChangeHandler = new ArrayList<>();

	@Inject(at = @At("RETURN"), method = "drawForeground")
	private void frameUpdate (MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
		if (config.enableAnvilModification) {
			if (invChangeHandler.size() == 0) {
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
					currentItemList = invChangeHandler;
					screenUpdate(page);
				}
			}
			if (open) {
				iconSlot1.paint(matrices, -129, 31, mouseX, mouseY);
				iconSlot2.paint(matrices, -129, 31 + (buttonHeight + 2), mouseX, mouseY);
				iconSlot3.paint(matrices, -129, 31 + (buttonHeight + 2) * 2, mouseX, mouseY);
				iconSlot4.paint(matrices, -129, 31 + (buttonHeight + 2) * 3, mouseX, mouseY);
				iconSlot5.paint(matrices, -129, 31 + (buttonHeight + 2) * 4, mouseX, mouseY);
				pageCount.setSize(12, 30);
				pageCount.setHorizontalAlignment(HorizontalAlignment.CENTER);
				pageCount.paint(matrices, -76, 144, mouseX, mouseY);
				button1textShadow.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button2textShadow.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button3textShadow.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button4textShadow.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button5textShadow.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button1textShadow.paint(matrices, -70, 38, mouseX, mouseY);
				button2textShadow.paint(matrices, -70, 38 + (buttonHeight + 2), mouseX, mouseY);
				button3textShadow.paint(matrices, -70, 38 + (buttonHeight + 2) * 2, mouseX, mouseY);
				button4textShadow.paint(matrices, -70, 38 + (buttonHeight + 2) * 3, mouseX, mouseY);
				button5textShadow.paint(matrices, -70, 38 + (buttonHeight + 2) * 4, mouseX, mouseY);
				button1text.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button2text.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button3text.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button4text.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button5text.setHorizontalAlignment(HorizontalAlignment.CENTER);
				button1text.paint(matrices, -71, 37, mouseX, mouseY);
				button2text.paint(matrices, -71, 37 + (buttonHeight + 2), mouseX, mouseY);
				button3text.paint(matrices, -71, 37 + (buttonHeight + 2) * 2, mouseX, mouseY);
				button4text.paint(matrices, -71, 37 + (buttonHeight + 2) * 3, mouseX, mouseY);
				button5text.paint(matrices, -71, 37 + (buttonHeight + 2) * 4, mouseX, mouseY);
				if (!searchField.isFocused() && searchField.getText().isEmpty()) {
					drawTextWithShadow(matrices, renderer, SEARCH_HINT_TEXT, -116, 15, -1);
				}
				if (currentTab == Tabs.INVENTORY) {
					if (button1 != null) {
						if (button1.isHovered()) {
							highlightSlot(matrices, 1);
						}
					}
					if (button2 != null) {
						if (button2.isHovered()) {
							highlightSlot(matrices, 2);
						}
					}
					if (button3 != null) {
						if (button3.isHovered()) {
							highlightSlot(matrices, 3);
						}
					}
					if (button4 != null) {
						if (button4.isHovered()) {
							highlightSlot(matrices, 4);
						}
					}
					if (button5 != null) {
						if (button5.isHovered()) {
							highlightSlot(matrices, 5);
						}
					}
				}
			} else {
				searchField.setTextFieldFocused(false);
			}
		}
	}

	private void highlightSlot(MatrixStack matrices, int orderOnPage) {
		int orderInList = (page * 5) + orderOnPage;
		int slotNum = currentInvOrder.get(orderInList - 1);
		int x;
		int y;
		if (!getInventory().get(slotNum).equals(currentItem)) {
			boolean isOnHotBar = false;
			if (slotNum < rowSize) {
				slotNum += rowSize * 3;
				isOnHotBar = true;
			} else {
				slotNum -= rowSize;
			}
			int orderInRow = slotNum % rowSize;
			int row = slotNum / rowSize;
			x = firstSlotX + (slotSize * orderInRow);
			y = firstSlotY + (slotSize * row);
			if (isOnHotBar) {
				y += 4;
			}
		} else {
			x = 26;
			y = 46;
		}
		DrawableHelper.fill(matrices, x, y, x + slotSize, y + slotSize, highlightColor);
	}

	private void showButtons() {
		if (page * 5 <= currentRenameListSize - 1) {
			this.addDrawableChild(button1);
			iconSlot1.setIcon(new ItemIcon(icon1));
			button1text.setText(Text.of(shortText(Text.of(currentRenameList.getName(page * 5)))));
			button1textShadow.setText(Text.of(shortText(Text.of(currentRenameList.getName(page * 5)))));
		}
		if (1 + page * 5 <= currentRenameListSize - 1) {
			this.addDrawableChild(button2);
			iconSlot2.setIcon(new ItemIcon(icon2));
			button2text.setText(Text.of(shortText(Text.of(currentRenameList.getName(1 + page * 5)))));
			button2textShadow.setText(Text.of(shortText(Text.of(currentRenameList.getName(1 + page * 5)))));
		}
		if (2 + page * 5 <= currentRenameListSize - 1) {
			this.addDrawableChild(button3);
			iconSlot3.setIcon(new ItemIcon(icon3));
			button3text.setText(Text.of(shortText(Text.of(currentRenameList.getName(2 + page * 5)))));
			button3textShadow.setText(Text.of(shortText(Text.of(currentRenameList.getName(2 + page * 5)))));
		}
		if (3 + page * 5 <= currentRenameListSize - 1) {
			this.addDrawableChild(button4);
			iconSlot4.setIcon(new ItemIcon(icon4));
			button4text.setText(Text.of(shortText(Text.of(currentRenameList.getName(3 + page * 5)))));
			button4textShadow.setText(Text.of(shortText(Text.of(currentRenameList.getName(3 + page * 5)))));
		}
		if (4 + page * 5 <= currentRenameListSize - 1) {
			this.addDrawableChild(button5);
			iconSlot5.setIcon(new ItemIcon(icon5));
			button5text.setText(Text.of(shortText(Text.of(currentRenameList.getName(4 + page * 5)))));
			button5textShadow.setText(Text.of(shortText(Text.of(currentRenameList.getName(4 + page * 5)))));
		}
	}

	private void hideButtons() {
		this.remove(background);
		this.remove(button1);
		this.remove(button2);
		this.remove(button3);
		this.remove(button4);
		this.remove(button5);
		iconSlot1.setIcon(null);
		iconSlot2.setIcon(null);
		iconSlot3.setIcon(null);
		iconSlot4.setIcon(null);
		iconSlot5.setIcon(null);
		button1text.setText(Text.of(""));
		button2text.setText(Text.of(""));
		button3text.setText(Text.of(""));
		button4text.setText(Text.of(""));
		button5text.setText(Text.of(""));
		button1textShadow.setText(Text.of(""));
		button2textShadow.setText(Text.of(""));
		button3textShadow.setText(Text.of(""));
		button4textShadow.setText(Text.of(""));
		button5textShadow.setText(Text.of(""));
	}

	private void clearAll() {
		this.remove(background);
		this.remove(button1);
		this.remove(button2);
		this.remove(button3);
		this.remove(button4);
		this.remove(button5);
		this.remove(pageDown);
		this.remove(pageUp);
		pageCount.setText(Text.of(""));
		iconSlot1.setIcon(null);
		iconSlot2.setIcon(null);
		iconSlot3.setIcon(null);
		iconSlot4.setIcon(null);
		iconSlot5.setIcon(null);
		button1text.setText(Text.of(""));
		button2text.setText(Text.of(""));
		button3text.setText(Text.of(""));
		button4text.setText(Text.of(""));
		button5text.setText(Text.of(""));
		button1textShadow.setText(Text.of(""));
		button2textShadow.setText(Text.of(""));
		button3textShadow.setText(Text.of(""));
		button4textShadow.setText(Text.of(""));
		button5textShadow.setText(Text.of(""));
		remove(searchTab);
		remove(favoriteTab);
		remove(inventoryTab);
		remove(searchTabActive);
		remove(favoriteTabActive);
		remove(inventoryTabActive);
		remove(openerMinus);
		remove(openerPlus);
	}

	private void sendSwapPackets(int slot1, int slot2, MinecraftClient client) {
		slot2 = slot2 - 9;
		if (slot2 > 27) {
			slot2 -= 27;
		} else if (slot2 < 0) {
			slot2 += 36;
		}
		assert client.player != null;
		int syncId = client.player.currentScreenHandler.syncId;
		assert client.interactionManager != null;
		client.interactionManager.clickSlot(syncId, slot2 + 3, 0, SlotActionType.PICKUP, client.player);
		client.interactionManager.clickSlot(syncId, slot1, 0, SlotActionType.PICKUP, client.player);
		client.interactionManager.clickSlot(syncId, slot2 + 3, 0, SlotActionType.PICKUP, client.player);
	}

	private ArrayList<Object> createButton(int orderOnPage, Text text) {
		ArrayList<Object> settings = new ArrayList<>();
		int orderInList = (page * 5) + orderOnPage;
		assert MinecraftClient.getInstance().player != null;
		PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
		String item;
		if (currentTab == Tabs.INVENTORY && currentInvOrder.get(orderInList - 1) != 36) {
			item = cutTranslationKey(inventory.main.get(currentInvOrder.get(orderInList - 1)).copy().getItem().getTranslationKey());
		} else {
			item = currentItem;
		}

		int u = 0;
		int v = 0;
		File file = new File(RPRenames.configPathFavorite + item + ".json");
		if (file.exists()) {
			boolean favorite = false;
			String[] favoriteList = ConfigManager.configRead(file).getName();
			for (String s : favoriteList) {
				if (text.equals(Text.of(s))) {
					favorite = true;
					break;
				}
			}
			if (favorite) {
				v = buttonHeight * 2;
			}
		}
		int tooltipStartCEM = 1;
		ArrayList<Text> toolTip = new ArrayList<>();
		toolTip.add(text);
		boolean isCEM = false;
		boolean isSameItem = false;
		if (currentTab == Tabs.INVENTORY) {
			toolTip.add(Text.of(item).copy().fillStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)));
			tooltipStartCEM++;
			isSameItem = currentItem.equals(item);
		}
		if (item.equals("name_tag") && orderInList > citSize && orderInList <= citSize + mobName.size()) {
			isCEM = true;
			for (String s : mobName.get(orderInList - 1 - citSize)) {
				toolTip.add(Text.of(s).copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
			}
		}

		boolean finalIsSameItem = isSameItem;
		settings.add(new TexturedButtonWidget(this.width / 2 - 218, this.height / 2 - 53 + ((orderOnPage - 1) * (buttonHeight + buttonOffsetY)), buttonWidth, buttonHeight, u, v, buttonHeight, BUTTON_TEXTURE, buttonTextureWidth, buttonTextureHeight, (button) -> {
			if (currentTab == Tabs.INVENTORY) {
				if (currentInvOrder.get(orderInList - 1) != 36) {
					afterInventoryTab = true;
					tempPage = page;
					if (!finalIsSameItem) {
						sendSwapPackets(0, currentInvOrder.get(orderInList - 1), MinecraftClient.getInstance());
					}
					afterInventoryTab = false;
				}
			}
			nameField.setText(text.getString());
		}, new ButtonWidget.TooltipSupplier() {
			public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int i, int j) {
				renderTooltip(matrixStack, toolTip, i, j);
			}
			public void supply(Consumer<Text> consumer) {
				consumer.accept(text);
			}
		}, text));

		if (isCEM) {
			int n = 0;
			for (String s : CEMList.mobsNames) {
				if (s.equals(toolTip.get(tooltipStartCEM).getString())) {
					break;
				}
				n++;
			}
			settings.add(new ItemStack(CEMList.spawnEggItems[n]));
		} else {
			if (currentTab == Tabs.INVENTORY) {
				if (currentInvOrder.get(orderInList - 1) != 36) {
					settings.add(inventory.main.get(currentInvOrder.get(orderInList - 1)).copy());
				}
			}
		}
		return settings;
	}

	private void buttonsDefine() {
		remove(button1);
		remove(button2);
		remove(button3);
		remove(button4);
		remove(button5);
		icon1 = iconAfterUpdate1;
		icon2 = iconAfterUpdate2;
		icon3 = iconAfterUpdate3;
		icon4 = iconAfterUpdate4;
		icon5 = iconAfterUpdate5;
		if (page * 5 <= currentRenameListSize - 1) {
			Text text = Text.of(currentRenameList.getName(page * 5));
			String shortText = shortText(text);
			ArrayList<Object> settings = createButton(1, text);
			button1 = (TexturedButtonWidget) settings.get(0);
			if (settings.size() > 1) {
				icon1 = (ItemStack) settings.get(1);
			}
			button1text.setText(Text.of(shortText));
			button1textShadow.setText(Text.of(shortText));
			iconSlot1.setIcon(new ItemIcon(icon1));
			icon1.setCustomName(text);
		}

		if (1 + page * 5 <= currentRenameListSize - 1) {
			Text text = Text.of(currentRenameList.getName(1 + page * 5));
			String shortText = shortText(text);
			ArrayList<Object> settings = createButton(2, text);
			button2 = (TexturedButtonWidget) settings.get(0);
			if (settings.size() > 1) {
				icon2 = (ItemStack) settings.get(1);
			}
			button2text.setText(Text.of(shortText));
			button2textShadow.setText(Text.of(shortText));
			iconSlot2.setIcon(new ItemIcon(icon2));
			icon2.setCustomName(text);
		}

		if (2 + page * 5 <= currentRenameListSize - 1) {
			Text text = Text.of(currentRenameList.getName(2 + page * 5));
			String shortText = shortText(text);
			ArrayList<Object> settings = createButton(3, text);
			button3 = (TexturedButtonWidget) settings.get(0);
			if (settings.size() > 1) {
				icon3 = (ItemStack) settings.get(1);
			}
			button3text.setText(Text.of(shortText));
			button3textShadow.setText(Text.of(shortText));
			iconSlot3.setIcon(new ItemIcon(icon3));
			icon3.setCustomName(text);
		}

		if (3 + page * 5 <= currentRenameListSize - 1) {
			Text text = Text.of(currentRenameList.getName(3 + page * 5));
			String shortText = shortText(text);
			ArrayList<Object> settings = createButton(4, text);
			button4 = (TexturedButtonWidget) settings.get(0);
			if (settings.size() > 1) {
				icon4 = (ItemStack) settings.get(1);
			}
			button4text.setText(Text.of(shortText));
			button4textShadow.setText(Text.of(shortText));
			iconSlot4.setIcon(new ItemIcon(icon4));
			icon4.setCustomName(text);
		}

		if (4 + page * 5 <= currentRenameListSize - 1) {
			Text text = Text.of(currentRenameList.getName(4 + page * 5));
			String shortText = shortText(text);
			ArrayList<Object> settings = createButton(5, text);
			button5 = (TexturedButtonWidget) settings.get(0);
			if (settings.size() > 1) {
				icon5 = (ItemStack) settings.get(1);
			}
			button5text.setText(Text.of(shortText));
			button5textShadow.setText(Text.of(shortText));
			iconSlot5.setIcon(new ItemIcon(icon5));
			icon5.setCustomName(text);
		}
	}

	private void updatePageWidgets() {
		remove(pageUp);
		remove(pageDown);
		addDrawableChild(pageUp);
		addDrawableChild(pageDown);
		pageDown.active = page != 0;
		pageUp.active = 5 + page * 5 <= currentRenameListSize - 1;
		pageCount.setText(Text.of(page + 1 + "/" + (currentRenameList.getName().length + 4) / 5));
	}

	private void tabsUpdate() {
		remove(searchTab);
		remove(favoriteTab);
		remove(inventoryTab);
		addDrawableChild(searchTab);
		addDrawableChild(favoriteTab);
		addDrawableChild(inventoryTab);
		remove(searchTabActive);
		remove(favoriteTabActive);
		remove(inventoryTabActive);
		searchTab.active = true;
		favoriteTab.active = true;
		inventoryTab.active = true;
		if (currentTab == Tabs.SEARCH) {
			addDrawableChild(searchTabActive);
			searchTab.active = false;
		} else if (currentTab == Tabs.FAVORITE) {
			addDrawableChild(favoriteTabActive);
			favoriteTab.active = false;
		} else if (currentTab == Tabs.INVENTORY) {
			addDrawableChild(inventoryTabActive);
			inventoryTab.active = false;
		}
		if (!currentItemHasRenames) {
			searchTab.active = false;
		}
		if (currentItem.equals("air")) {
			searchTab.active = false;
			favoriteTab.active = false;
		}
	}

	private void onSearch(String search) {
		if (search.startsWith("+")) {
			searchTag = "";
		} else {
			searchTag = search;
		}
		screenUpdate();
	}

	private String shortText(Text text) {
		String shortText;
		shortText = text.getString();
		if (renderer.getWidth(shortText) > 92 - 5) {
			while (renderer.getWidth(shortText) > 92 - 5) {
				shortText = shortText.substring(0, shortText.length() - 1);
			}
			return shortText + "...";
		}
		return shortText;
	}

	private String cutTranslationKey(String translationKey) {
		int i = 0;
		int t = 0;
		while (t != 2) {
			if (String.valueOf(translationKey.charAt(i)).equals(".")) {
				translationKey = translationKey.substring(i + 1);
				t++;
				i = 0;
			} else {
				i++;
			}
		}
		return translationKey;
	}

	private String[] search(String[] list, String match) {
		ArrayList<String> arrayList = new ArrayList<>();
		int length = list.length;
		int i = 0;
		int g = 0;
		if (match.contains("*")) {
			while (!String.valueOf(match.charAt(g)).equals("*")) {
				g++;
			}
			int h = g;
			while (true) {
				h++;
				if (String.valueOf(match.charAt(h - 1)).equals("*") && h == match.length()) {
					break;
				}
				if (!String.valueOf(match.charAt(h)).equals("*")) {
					break;
				}
			}
			while (i < length) {
				if (list[i].startsWith(match.substring(0,g)) && list[i].contains(match.substring(h))) {
					arrayList.add(list[i]);
				}
				i++;
			}
		} else {
			while (i < length) {
				if (list[i].toUpperCase(Locale.ROOT).startsWith(match.toUpperCase(Locale.ROOT))) {
					arrayList.add(list[i]);
				}
				i++;
			}
		}

		String[] cutList = new String[arrayList.size()];
		int k = 0;
		while (k < arrayList.size()) {
			cutList[k] = arrayList.get(k);
			k++;
		}
		return cutList;
	}
}