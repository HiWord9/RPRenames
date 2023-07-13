package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.Rename;
import com.HiWord9.RPRenames.RenameButton;
import com.HiWord9.RPRenames.Tabs;
import com.HiWord9.RPRenames.configGeneration.CEMList;
import com.HiWord9.RPRenames.configGeneration.ConfigManager;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.google.gson.Gson;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends Screen {

	private static final ModConfig config = ModConfig.INSTANCE;
	boolean isOnServer = !MinecraftClient.getInstance().isInSingleplayer();

	@Shadow
	private TextFieldWidget nameField;

	AnvilScreenMixin(Text title) {
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

	int highlightColor = config.getSlotHighlightRGBA();

	int backgroundWidth = 176;
	int backgroundHeight = 166;

	int page = 0;
	int currentRenameListSize;

	String currentItem = "air";
	ItemStack itemAfterUpdate;
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

	RenameButton button1 = new RenameButton();
	RenameButton button2 = new RenameButton();
	RenameButton button3 = new RenameButton();
	RenameButton button4 = new RenameButton();
	RenameButton button5 = new RenameButton();
	ArrayList<RenameButton> buttons = new ArrayList<>();

	TexturedButtonWidget searchTab;
	TexturedButtonWidget searchTabActive;
	TexturedButtonWidget favoriteTab;
	TexturedButtonWidget favoriteTabActive;
	TexturedButtonWidget inventoryTab;
	TexturedButtonWidget inventoryTabActive;
	Tabs currentTab = Tabs.SEARCH;

	TexturedButtonWidget addToFavorite;
	TexturedButtonWidget removeFromFavorite;

	TexturedButtonWidget pageDown;
	TexturedButtonWidget pageUp;

	WLabel pageCount = new WLabel(Text.of(""),0xffffff);

	Rename currentRenameList;

	TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

	TextFieldWidget searchField;
	Text SEARCH_HINT_TEXT = Text.translatable("rprenames.gui.searchHintText").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);

	String searchTag = "";

	ArrayList<ArrayList<String>> mobName = new ArrayList<>();

	boolean currentItemHasRenames = true;

	@Inject(at = @At("RETURN"), method = "setup")
	private void init(CallbackInfo ci) {
		if (config.enableAnvilModification) {
			RPRenames.LOGGER.info("Starting RPRenames modification on AnvilScreen");

			buttons.add(button1);
			buttons.add(button2);
			buttons.add(button3);
			buttons.add(button4);
			buttons.add(button5);

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

			opener = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, 0, 0, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, (button) -> switchOpen());

			if (!clientConfigReadable && !serverConfigReadable) {
				Text noConfigText = Text.translatable("rprenames.configrenames.notfound", RPRenames.configPath);
				Tooltip tooltip = Tooltip.of(noConfigText);
				opener.setTooltip(tooltip);
			}

			openerOpened = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, openerHeight * 3, openerHeight, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, null);
			openerPlus = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, 0, openerHeight, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, null);
			openerMinus = new TexturedButtonWidget(this.width / 2 - 85, this.height / 2 - 39, openerWidth, openerHeight, 0, openerHeight * 2, 0, OPENER_TEXTURE, openerTextureWidth, openerTextureHeight, null);

			addDrawableChild(opener);

			searchTab = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78, tabWidth, tabHeight, 0, 0, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
				currentTab = Tabs.SEARCH;
				reloadButton(searchTabActive);
				remove(favoriteTabActive);
				remove(inventoryTabActive);
				screenUpdate();
			});
			favoriteTab = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78 + (tabHeight + tabOffsetY), tabWidth, tabHeight, 0, tabHeight, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
				currentTab = Tabs.FAVORITE;
				remove(searchTabActive);
				reloadButton(favoriteTabActive);
				remove(inventoryTabActive);
				screenUpdate();
			});
			inventoryTab = new TexturedButtonWidget(this.width / 2 - 258, this.height / 2 - 78 + (tabHeight + tabOffsetY) * 2, tabWidth, tabHeight, 0, tabHeight * 2, 0, MENU_TEXTURE, menuTextureWidth, menuTextureHeight, button -> {
				currentTab = Tabs.INVENTORY;
				remove(searchTabActive);
				remove(favoriteTabActive);
				reloadButton(inventoryTabActive);
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

				String item = cutTranslationKey(currentItem);

				File currentFile = new File(RPRenames.configPathFavorite + item + ".json");
				boolean nameExist;
				ArrayList<Rename> listNames = new ArrayList<>();
				if (currentFile.exists()) {
					Rename alreadyExist = ConfigManager.configRead(currentFile);
					nameExist = Arrays.stream(alreadyExist.getName()).toList().contains(favoriteName);
					if (!nameExist) {
						String[] newConfig = new String[alreadyExist.getName().length + 1];
						int h = 0;
						while (h < alreadyExist.getName().length) {
							newConfig[h] = alreadyExist.getName()[h];
							h++;
						}
						newConfig[h] = favoriteName;

						Rename newRename = new Rename(newConfig);
						listNames.add(newRename);
					}
				} else {
					if (new File(RPRenames.configPathFavorite).mkdirs()) {
						System.out.println("[RPR] Created folder for favorites config: " + RPRenames.configPathFavorite);
					}
					System.out.println("[RPR] Created new file for favorites config: " + RPRenames.configPathFavorite + item + ".json");
					Rename name1 = new Rename(new String[]{favoriteName});
					listNames.add(name1);
				}

				try {
					FileWriter fileWriter = new FileWriter(currentFile);
					Gson gson = new Gson();
					gson.toJson(listNames, fileWriter);
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				featuredButtonsUpdate(nameField.getText());
				if (open) {
					screenUpdate(page);
				}
			});
			removeFromFavorite = new TexturedButtonWidget(this.width / 2 + config.favoritePosX, this.height / 2 + config.favoritePosY, favoriteButtonWidth, favoriteButtonHeight, 0, 0, 0, FAVORITE_BUTTON_TEXTURE, favoriteButtonTextureWidth, favoriteButtonTextureHeight, button -> {
				String favoriteName = nameField.getText();

				String item = cutTranslationKey(currentItem);

				File currentFile = new File(RPRenames.configPathFavorite + item + ".json");
				Rename alreadyExist = ConfigManager.configRead(currentFile);
				ArrayList<String> alreadyExistList = new ArrayList<>(Arrays.stream(alreadyExist.getName()).toList());
				alreadyExistList.remove(favoriteName);

				String[] newConfig = new String[alreadyExistList.size()];
				for (int i = 0; i < alreadyExistList.size(); i++) {
					newConfig[i] = alreadyExistList.get(i);
				}

				if (newConfig.length > 0) {
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
				} else {
					try {
						Files.deleteIfExists(currentFile.toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
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
			searchField.setFocused(false);
			searchField.setFocusUnlocked(false);
			searchField.setText("");
			remove(searchField);
			nameField.setFocused(true);
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

	private void screenUpdate(int savedPage) {
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
		File jsonRenamesClient = new File(RPRenames.configPathClient + RPRenames.configPathNameCIT + "/" + cutTranslationKey(currentItem) + ".json");
		File jsonRenamesServer = new File(RPRenames.configPathServer + RPRenames.configPathNameCIT + "/" + cutTranslationKey(currentItem) + ".json");
		File jsonRenamesFavorite = new File(RPRenames.configPathFavorite + cutTranslationKey(currentItem) + ".json");
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
					if (cutTranslationKey(currentItem).equals("name_tag") && !config.ignoreCEM) {
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
			} else if ((clientConfigCEMReadable || serverConfigCEMReadable) && cutTranslationKey(currentItem).equals("name_tag") && !config.ignoreCEM) {
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
				String item = cutTranslationKey(s);
				if (!item.equals("air") && !checked.contains(item)) {
					File jsonRenamesClientLocal = new File(RPRenames.configPathClient + RPRenames.configPathNameCIT + "/" + item + ".json");
					File jsonRenamesServerLocal = new File(RPRenames.configPathServer + RPRenames.configPathNameCIT + "/" + item + ".json");
					boolean clientConfigReadableLocal = jsonRenamesClientLocal.exists();
					boolean serverConfigReadableLocal = jsonRenamesServerLocal.exists();
					boolean clientConfigCEMReadableLocal = RPRenames.configClientCEMFolder.exists();
					boolean serverConfigCEMReadableLocal = RPRenames.configServerCEMFolder.exists();
					if (!isOnServer) {
						serverConfigReadableLocal = false;
						serverConfigCEMReadableLocal = false;
					}
					if (!item.equals("name_tag")) {
						clientConfigCEMReadableLocal = false;
						serverConfigCEMReadableLocal = false;
					}
					checked.add(item);
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
			ArrayList<String> checked = new ArrayList<>();
			ArrayList<String> names = new ArrayList<>();
			ArrayList<Integer> numInInv = new ArrayList<>();
			int i = 0;
			for (String s : currentItemList) {
				String item = cutTranslationKey(s);
				String itemSearchTag = "";
				if (searchField.getText().startsWith("+")) {
					itemSearchTag = searchField.getText().substring(1);
				}
				if (!item.equals("air") && !checked.contains(item) && item.toUpperCase(Locale.ROOT).contains(itemSearchTag.toUpperCase(Locale.ROOT))) {
					File jsonRenamesClientLocal = new File(RPRenames.configPathClient + RPRenames.configPathNameCIT + "/" + item + ".json");
					File jsonRenamesServerLocal = new File(RPRenames.configPathServer + RPRenames.configPathNameCIT + "/" + item + ".json");
					boolean clientConfigReadableLocal = jsonRenamesClientLocal.exists();
					boolean serverConfigReadableLocal = jsonRenamesServerLocal.exists();
					boolean clientConfigCEMReadableLocal = RPRenames.configClientCEMFolder.exists();
					boolean serverConfigCEMReadableLocal = RPRenames.configServerCEMFolder.exists();
					if (!isOnServer) {
						serverConfigReadableLocal = false;
						serverConfigCEMReadableLocal = false;
					}
					checked.add(item);
					if (clientConfigReadableLocal) {
						List<String> c = Arrays.stream(new Rename(search(ConfigManager.configRead(jsonRenamesClientLocal).getName(), searchTag)).getName()).toList();
						for (String name : c) {
							names.add(name);
							numInInv.add(i);
						}
					}
					if (serverConfigReadableLocal) {
						List<String> c = Arrays.stream(new Rename(search(ConfigManager.configRead(jsonRenamesServerLocal).getName(), searchTag)).getName()).toList();
						for (String name : c) {
							names.add(name);
							numInInv.add(i);
						}
					}
					if (item.equals("name_tag") && !config.ignoreCEM) {
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
			reloadButton(openerMinus);
		}
		if (showOpenerPlus) {
			reloadButton(openerPlus);
		}

		if (open) {
			reloadButton(background);
			reloadButton(openerOpened);
			showButtons();
			updatePageWidgets();
			addDrawableChild(searchField);
			searchField.setFocusUnlocked(true);
			nameField.setFocused(false);
			nameField.setFocusUnlocked(true);
			tabsUpdate();
		}
	}

	ArrayList<String> modelsArray = new ArrayList<>();

	private List<ArrayList<String>> getCEM(Rename currentRenameList, boolean clientConfigCEMReadable, boolean serverConfigCEMReadable, String searchTag) {
		if (clientConfigCEMReadable) {
			try {
				Files.walk(Path.of(RPRenames.configPathClient + RPRenames.configPathNameCEM), new FileVisitOption[0]).filter(path -> path.toString().endsWith(".json")).forEach((jsonFile) -> addToMobName(jsonFile, searchTag));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (serverConfigCEMReadable) {
			try {
				Files.walk(Path.of(RPRenames.configPathServer + RPRenames.configPathNameCEM), new FileVisitOption[0]).filter(path -> path.toString().endsWith(".json")).forEach((jsonFile) -> addToMobName(jsonFile, searchTag));
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
		settings.add(new ArrayList<>(modelsArray));
		settings.add(citWithoutModels);
		modelsArray.clear();
		return settings;
	}

	private void addToMobName(Path jsonFile, String searchTag) {
		File file = new File(String.valueOf(jsonFile));
		boolean isValid = false;
		for (EntityType<?> entityType : CEMList.mobs) {
			if (entityType.getUntranslatedName().equals(file.getName().substring(0, file.getName().length() - 5))) {
				isValid = true;
			}
		}
		if (!isValid) {
			return;
		}
		for (String s : search(ConfigManager.configRead(file).getName(), searchTag)) {
			if (!modelsArray.contains(s)) {
				modelsArray.add(s);
				ArrayList<String> mob = new ArrayList<>();
				mob.add(file.getName().substring(0, file.getName().length() - 5));
				mobName.add(mob);
			} else {
				int n = 0;
				for (String s2 : modelsArray) {
					if (s2.equals(s)) {
						break;
					}
					n++;
				}
				ArrayList<String> mob = mobName.get(n);
				mob.add(file.getName().substring(0, file.getName().length() - 5));
				mobName.set(n, mob);
			}
		}
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
			File file = new File(RPRenames.configPathFavorite + cutTranslationKey(currentItem) + ".json");
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

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;isActive()Z"), method = "keyPressed")
	private boolean onKeyPressed(TextFieldWidget instance, int keyCode, int scanCode, int modifiers) {
		if (config.enableAnvilModification) {
			searchField.keyPressed(keyCode, scanCode, modifiers);
			return instance.isActive() || searchField.isActive();
		}
		return instance.isActive();
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
					currentItemList = invChangeHandler;
					screenUpdate(page);
				}
			}
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
					searchField.setFocused(false);
				} else {
					currentItem = stack.getItem().getTranslationKey();
					itemAfterUpdate = stack.copy();
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
		for (ItemStack itemStack : inventory.main) {
			inventoryList.add(itemStack.getItem().getTranslationKey());
		}
		inventoryList.add(currentItem);
		return inventoryList;
	}

	@Inject(at = @At("RETURN"), method = "drawForeground")
	private void frameUpdate (MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
		if (config.enableAnvilModification) {
			if (open) {
				int xScreenOffset = (this.width - backgroundWidth) / 2;
				int yScreenOffset = (this.height - backgroundHeight) / 2;
				for (int n = 0; n < 5; n++) {
					buttons.get(n).drawElements(matrices, mouseX, mouseY);
				}
				pageCount.setSize(12, 30);
				pageCount.setHorizontalAlignment(HorizontalAlignment.CENTER).paint(matrices, -76, 144, mouseX, mouseY);
				if (searchField != null) {
					if (!searchField.isFocused() && searchField.getText().isEmpty()) {
						drawTextWithShadow(matrices, renderer, SEARCH_HINT_TEXT, -116, 15, -1);
					}
				}

				if (!config.disablePageArrowsTips && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
					if (pageUp.isMouseOver(mouseX, mouseY) && pageUp.isHovered()) {
						renderTooltip(matrices, Text.translatable("rprenames.gui.pageUp.toLast.tooltip").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)), mouseX - xScreenOffset, mouseY - yScreenOffset);
					} else if (pageDown.isMouseOver(mouseX, mouseY) && pageDown.isHovered()) {
						renderTooltip(matrices, Text.translatable("rprenames.gui.pageDown.toFirst.tooltip").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)), mouseX - xScreenOffset, mouseY - yScreenOffset);
					}
				}

				for (int n = 0; n < 5; n++) {
					if (buttons.get(n).getButton() != null) {
						if (buttons.get(n).getButton().isMouseOver(mouseX, mouseY) && buttons.get(n).getButton().isHovered() && buttons.get(n).isActive()) {
							ArrayList<Text> lines = new ArrayList<>(buttons.get(n).getTooltip());
							if (!buttons.get(n).isCEM() && config.enablePreview) {
								if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) && !config.playerPreviewByDefault) {
									if (!config.disablePlayerPreviewTips) {
										lines.add(Text.translatable("rprenames.gui.playerPreviewTip.holdShift").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)));
									}
								} else if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) != config.playerPreviewByDefault) {
									if (!config.disablePlayerPreviewTips) {
										lines.add(Text.translatable("rprenames.gui.playerPreviewTip.pressF").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true)));
									}
								}
							}
							renderTooltip(matrices, lines, mouseX - xScreenOffset, mouseY - yScreenOffset);
							if (currentTab == Tabs.INVENTORY && config.slotHighlightColorALPHA > 0) {
								buttons.get(n).highlightSlot(matrices, currentInvOrder, getInventory(), currentItem, highlightColor);
							}
							if (config.enablePreview) {
								buttons.get(n).drawPreview(matrices, mouseX - xScreenOffset, mouseY - yScreenOffset, 52, 52, config.scaleFactorItem, config.scaleFactorEntity);
							}
						}
					}
				}
			} else {
				searchField.setFocused(false);
			}
		}
	}

	private void showButtons() {
		for (int n = 0; n < 5; n++) {
			if (n + page * 5 <= currentRenameListSize - 1) {
				addDrawableChild(buttons.get(n).getButton());
				buttons.get(n).setActive(true);
			}
		}
	}

	private void hideButtons() {
		this.remove(background);
		for (int n = 0; n < 5; n++) {
			remove(buttons.get(n).getButton());
			buttons.get(n).setActive(false);
		}
	}

	private void clearAll() {
		this.remove(background);
		this.remove(pageDown);
		this.remove(pageUp);
		for (int n = 0; n < 5; n++) {
			remove(buttons.get(n).getButton());
			buttons.get(n).setActive(false);
		}
		pageCount.setText(Text.of(""));
		remove(searchTab);
		remove(favoriteTab);
		remove(inventoryTab);
		remove(searchTabActive);
		remove(favoriteTabActive);
		remove(inventoryTabActive);
		remove(openerMinus);
		remove(openerPlus);
	}

	private void reloadButton(ClickableWidget button) {
		remove(button);
		addDrawableChild(button);
	}

	private void putInAnvil(int slotInInventory, MinecraftClient client) {
		slotInInventory = slotInInventory - 9;
		if (slotInInventory > 27) {
			slotInInventory -= 27;
		} else if (slotInInventory < 0) {
			slotInInventory += 36;
		}
		assert client.player != null;
		int syncId = client.player.currentScreenHandler.syncId;
		assert client.interactionManager != null;
		client.interactionManager.clickSlot(syncId, slotInInventory + 3, 0, SlotActionType.PICKUP, client.player);
		client.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, client.player);
		client.interactionManager.clickSlot(syncId, slotInInventory + 3, 0, SlotActionType.PICKUP, client.player);
	}

	private void createButton(int orderOnPage, Text text) {
		int orderInList = (page * 5) + orderOnPage;
		assert MinecraftClient.getInstance().player != null;
		PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
		String item = currentTab == Tabs.INVENTORY && currentInvOrder.get(orderInList) != 36 ? inventory.main.get(currentInvOrder.get(orderInList)).copy().getItem().getTranslationKey() : currentItem;
		boolean favorite = RenameButton.calcFavorite(cutTranslationKey(item), text.getString());
		int v = favorite ? buttonHeight * 2 : 0;

		ArrayList<Text> tooltip = new ArrayList<>();
		tooltip.add(text);
		boolean isCEM = false;
		String firstMob = null;
		if (currentTab == Tabs.INVENTORY) {
			tooltip.add(Text.of(config.translateItemNames ? Text.translatable(item).getString() : cutTranslationKey(item)).copy().fillStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)));
		}
		if (cutTranslationKey(item).equals("name_tag") && orderInList + 1 > citSize && orderInList + 1 <= citSize + mobName.size()) {
			isCEM = true;
			firstMob = mobName.get(orderInList - citSize).get(0);
			for (String s : mobName.get(orderInList - citSize)) {
				tooltip.add(Text.of(config.translateMobNames ? Text.translatable("entity.minecraft." + s).getString() : s).copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)));
			}
		}

		TexturedButtonWidget texturedButtonWidget = new TexturedButtonWidget(this.width / 2 - 218, this.height / 2 - 53 + (orderOnPage * (buttonHeight + buttonOffsetY)), buttonWidth, buttonHeight, 0, v, buttonHeight, BUTTON_TEXTURE, buttonTextureWidth, buttonTextureHeight, (button) -> {
			if (currentTab == Tabs.INVENTORY) {
				if (currentInvOrder.get(orderInList) != 36) {
					afterInventoryTab = true;
					tempPage = page;
					if (!currentItem.equals(item)) {
						putInAnvil(currentInvOrder.get(orderInList), MinecraftClient.getInstance());
					}
					afterInventoryTab = false;
				}
			}
			nameField.setText(text.getString());
		});

		ItemStack itemStack = itemAfterUpdate == null ? null : itemAfterUpdate.copy();
		int mobInList = -1;
		if (isCEM) {
			int n = 0;
			for (EntityType<?> entityType : CEMList.mobs) {
				String s = entityType.getUntranslatedName();
				if (s.equals(firstMob)) {
					break;
				}
				n++;
			}
			mobInList = n;
 			itemStack = new ItemStack(CEMList.spawnEggItems[n]);
		} else {
			if (currentTab == Tabs.INVENTORY) {
				if (currentInvOrder.get(orderInList) != 36) {
					itemStack = inventory.main.get(currentInvOrder.get(orderInList)).copy();
				}
			}
		}

		buttons.get(orderOnPage).setParameters(texturedButtonWidget, text, itemStack, page, orderOnPage, isCEM, mobInList, tooltip);
	}

	private void buttonsDefine() {
		for (int n = 0; n < 5; n++) {
			remove(buttons.get(n).getButton());
			buttons.get(n).setActive(false);
			if (n + page * 5 <= currentRenameListSize - 1) {
				createButton(n, Text.of(currentRenameList.getName(n + page * 5)));
			}
		}
	}

	private void updatePageWidgets() {
		reloadButton(pageUp);
		reloadButton(pageDown);
		pageDown.active = page != 0;
		pageUp.active = 5 + page * 5 <= currentRenameListSize - 1;
		pageCount.setText(Text.of(page + 1 + "/" + (currentRenameList.getName().length + 4) / 5));
	}

	private void tabsUpdate() {
		reloadButton(searchTab);
		reloadButton(favoriteTab);
		reloadButton(inventoryTab);
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

	private String cutTranslationKey(String translationKey) {
		int i = translationKey.length() - 1;
		if (translationKey.contains(".")) {
			while (true) {
				if (String.valueOf(translationKey.charAt(i)).equals(".")) {
					return translationKey.substring(i + 1);
				} else {
					i--;
				}
			}
		}
		return translationKey;
	}

	private String[] search(String[] list, String match) {
		ArrayList<String> arrayList = new ArrayList<>();
		int i = 0;
		int g = 0;
		if (match.contains("*")) {
			while (!String.valueOf(match.charAt(g)).equals("*")) {
				g++;
			}
			int h = g;
			do {
				h++;
			} while ((!String.valueOf(match.charAt(h - 1)).equals("*") || h != match.length()) && (String.valueOf(match.charAt(h)).equals("*")));
			while (i < list.length) {
				if (list[i].startsWith(match.substring(0,g)) && list[i].contains(match.substring(h))) {
					arrayList.add(list[i]);
				}
				i++;
			}
		} else {
			while (i < list.length) {
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