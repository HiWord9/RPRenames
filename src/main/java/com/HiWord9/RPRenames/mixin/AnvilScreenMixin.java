package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.Rename;
import com.HiWord9.RPRenames.configManager;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.ArrayList;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin extends Screen {

	@Shadow private TextFieldWidget nameField;

	protected AnvilScreenMixin(Text title) {
		super(title);
	}

	boolean open = false;

	private static final Identifier RENAMES_MENU = new Identifier(RPRenames.MOD_ID,"textures/gui/renames_menu.png");
	private static final Identifier RENAMES_BUTTON = new Identifier(RPRenames.MOD_ID,"textures/gui/renames_button.png");
	int page = 0;
	int renameListSize;

	String currentItem = null;
	TexturedButtonWidget background;
	TexturedButtonWidget opener;
	TexturedButtonWidget button1;
	TexturedButtonWidget button2;
	TexturedButtonWidget button3;
	TexturedButtonWidget button4;
	TexturedButtonWidget button5;
	WLabel button1text = new WLabel(Text.of(""),0xffffff);
	WLabel button2text = new WLabel(Text.of(""),0xffffff);
	WLabel button3text = new WLabel(Text.of(""),0xffffff);
	WLabel button4text = new WLabel(Text.of(""),0xffffff);
	WLabel button5text = new WLabel(Text.of(""),0xffffff);
	TexturedButtonWidget pageDown;
	TexturedButtonWidget pageUp;
	WLabel pageCount = new WLabel(Text.of(""),0xffffff);
	Rename renameList;

	TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
	TextFieldWidget searchField;

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

	CallbackInfo ci;

	@Inject(at = @At("RETURN"), method = "setup")
	private void init(CallbackInfo ci) {
		this.ci = ci;

		background = new TexturedButtonWidget(this.width / 2 - 200 - 28, this.height / 2 - 83, 110+28, 166, 118, 0, 0, RENAMES_MENU, 256, 166, null);

		pageDown = new TexturedButtonWidget(this.width / 2 - 200 + 10 - 28, this.height / 2 - 83 + 140, 30, 16, 58, 40, 16, RENAMES_MENU, 256, 166, (button -> {
			page--;
			hideButtons();
			buttonsDefine();
			showButtons();
			updatePageWidgets();
			if (page == 0) {
				button.active = false;
			}
			pageUp.active = true;
		}));
		pageUp = new TexturedButtonWidget(this.width / 2 - 200 + 10 + 60, this.height / 2 - 83 + 140, 30, 16, 88, 40, 16, RENAMES_MENU, 256, 166, (button -> {
			page++;
			hideButtons();
			buttonsDefine();
			showButtons();
			updatePageWidgets();
			pageDown.active = true;
			if (5 + page * 5 > renameListSize - 1) {
				button.active = false;
			}
		}));

		opener = new TexturedButtonWidget(this.width / 2 - 83, this.height / 2 - 38 + 1, 20, 19, 0, 0, 19, RENAMES_BUTTON, 20, 57, (button) -> {
			if (!open) {
				open = true;
				showButtons();
				page = 0;
				updatePageWidgets();
				addDrawableChild(searchField);
				searchField.setFocusUnlocked(true);
				searchField.setTextFieldFocused(true);
				nameField.setTextFieldFocused(false);
				nameField.setFocusUnlocked(true);
				System.out.println("Opened RP Renames Menu");
			} else {
				open = false;
				clearAll();
				searchField.setTextFieldFocused(false);
				searchField.setFocusUnlocked(false);
				searchField.setText("");
				remove(searchField);
				nameField.setTextFieldFocused(true);
				nameField.setFocusUnlocked(false);
				System.out.println("Closed RP Renames Menu");
			}
		});
		addDrawableChild(opener);

		searchField = new TextFieldWidget(renderer, this.width / 2 - 200 + 10 + 14 - 28, this.height / 2 - 83 + 30 - 22 + 5, 90 - 14 + 28, 10, Text.of(""));
		searchField.setChangedListener(this::onSearch);
		searchField.setDrawsBackground(false);

		screenUpdate();
	}

	private void screenUpdate() {
		clearAll();
		if (currentItem != null) {
			File jsonRenames = new File(configManager.configPath + currentItem + ".json");
			if (jsonRenames.exists()) {
				renameList = new Rename(search(configManager.configRead(jsonRenames).getName(), searchField.getText()));
				renameListSize = renameList.getName().length;

				buttonsDefine();
				clearAll();

				if (open) {
					showButtons();
					updatePageWidgets();
					addDrawableChild(searchField);
					searchField.setFocusUnlocked(true);
					searchField.setTextFieldFocused(true);
					nameField.setTextFieldFocused(false);
					nameField.setFocusUnlocked(true);
				}

				opener.active = true;
			} else {
				opener.active = false;
			}
		} else {
			opener.active = false;
		}
	}

	@Inject(at = @At("RETURN"), method = "onSlotUpdate")
	private void itemUpdate(ScreenHandler handler, int slotId, ItemStack stack, CallbackInfo ci) {
		if (slotId == 0) {
			if (stack.isEmpty()) {
				currentItem = null;
				clearAll();
				searchField.setText("");
				searchField.setFocusUnlocked(false);
				remove(searchField);
				searchField.setTextFieldFocused(false);
				screenUpdate();
			} else {
				currentItem = stack.getItem().getTranslationKey();
				int i = 0;
				int t = 0;
				while (t != 2) {
					if (String.valueOf(currentItem.charAt(i)).equals(".")) {
						currentItem = currentItem.substring(i+1);
						t++;
						i = 0;
					} else {
						i++;
					}
				}
				System.out.println(currentItem);

				icon1 = new ItemStack(stack.getItem());
				icon2 = new ItemStack(stack.getItem());
				icon3 = new ItemStack(stack.getItem());
				icon4 = new ItemStack(stack.getItem());
				icon5 = new ItemStack(stack.getItem());
				clearAll();
				searchField.setText("");
				searchField.setFocusUnlocked(true);
				screenUpdate();
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "drawForeground")
	private void paintWWidgets(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
		iconSlot1.paint(matrices, -130 + 1, 30 + 1, mouseX, mouseY);
		iconSlot2.paint(matrices, -130 + 1, 52 + 1, mouseX, mouseY);
		iconSlot3.paint(matrices, -130 + 1, 74 + 1, mouseX, mouseY);
		iconSlot4.paint(matrices, -130 + 1, 96 + 1, mouseX, mouseY);
		iconSlot5.paint(matrices, -130 + 1, 118 + 1, mouseX, mouseY);
		pageCount.setHorizontalAlignment(HorizontalAlignment.CENTER);
		pageCount.paint(matrices,83 + 10 - 200 + 10 + 30 + 5 - 14, 140 + 4, mouseX, mouseY);
		pageCount.setSize(12, 30);
		button1text.paint(matrices, -130 + 20 + 49 - 10, 30 + 7, mouseX, mouseY);
		button2text.paint(matrices, -130 + 20 + 49 - 10, 52 + 7, mouseX, mouseY);
		button3text.paint(matrices, -130 + 20 + 49 - 10, 74 + 7, mouseX, mouseY);
		button4text.paint(matrices, -130 + 20 + 49 - 10, 96 + 7, mouseX, mouseY);
		button5text.paint(matrices, -130 + 20 + 49 - 10, 118 + 7, mouseX, mouseY);
		button1text.setHorizontalAlignment(HorizontalAlignment.CENTER);
		button2text.setHorizontalAlignment(HorizontalAlignment.CENTER);
		button3text.setHorizontalAlignment(HorizontalAlignment.CENTER);
		button4text.setHorizontalAlignment(HorizontalAlignment.CENTER);
		button5text.setHorizontalAlignment(HorizontalAlignment.CENTER);
	}

	private void showButtons() {
		this.addDrawableChild(background);
		background.active = false;

		if (page * 5 <= renameListSize - 1) {
			this.addDrawableChild(button1);
			iconSlot1.setIcon(new ItemIcon(icon1));
			button1text.setText(Text.of(renameList.getName(page * 5)));
		}
		if (1 + page * 5 <= renameListSize - 1) {
			this.addDrawableChild(button2);
			iconSlot2.setIcon(new ItemIcon(icon2));
			button2text.setText(Text.of(renameList.getName(1 + page * 5)));
		}
		if (2 + page * 5 <= renameListSize - 1) {
			this.addDrawableChild(button3);
			iconSlot3.setIcon(new ItemIcon(icon3));
			button3text.setText(Text.of(renameList.getName(2 + page * 5)));
		}
		if (3 + page * 5 <= renameListSize - 1) {
			this.addDrawableChild(button4);
			iconSlot4.setIcon(new ItemIcon(icon4));
			button4text.setText(Text.of(renameList.getName(3 + page * 5)));
		}
		if (4 + page * 5 <= renameListSize - 1) {
			this.addDrawableChild(button5);
			iconSlot5.setIcon(new ItemIcon(icon5));
			button5text.setText(Text.of(renameList.getName(4 + page * 5)));
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
		page = 0;
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
	}

	private void buttonsDefine() {
		if (page * 5 <= renameListSize - 1) {
			Text text1 = Text.of(renameList.getName(page * 5));
			button1 = new TexturedButtonWidget(this.width / 2 - 200 + 10 - 28, this.height / 2 - 83 + 30, 118, 20, 0,0, 20,RENAMES_MENU,256,166, (button -> {
				nameField.setText(text1.getString());
			}), text1);
			button1text.setText(text1);
			iconSlot1.setIcon(new ItemIcon(icon1));
			icon1.setCustomName(text1);
		}

		if (1 + page * 5 <= renameListSize - 1) {
			Text text2 = Text.of(renameList.getName(1 + page * 5));
			button2 = new TexturedButtonWidget(this.width / 2 - 200 + 10 - 28, this.height / 2 - 83 + 52, 118, 20, 0,0, 20,RENAMES_MENU,256,166, (button -> {
				nameField.setText(text2.getString());
			}), text2);
			button2text.setText(text2);
			iconSlot2.setIcon(new ItemIcon(icon2));
			icon2.setCustomName(text2);
		}

		if (2 + page * 5 <= renameListSize - 1) {
			Text text3 = Text.of(renameList.getName(2 + page * 5));
			button3 = new TexturedButtonWidget(this.width / 2 - 200 + 10 - 28, this.height / 2 - 83 + 74, 118, 20, 0,0, 20,RENAMES_MENU,256,166, (button -> {
				nameField.setText(text3.getString());
			}), text3);
			button3text.setText(text3);
			iconSlot3.setIcon(new ItemIcon(icon3));
			icon3.setCustomName(text3);
		}

		if (3 + page * 5 <= renameListSize - 1) {
			Text text4 = Text.of(renameList.getName(3 + page * 5));
			button4 = new TexturedButtonWidget(this.width / 2 - 200 + 10 - 28, this.height / 2 - 83 + 96, 118, 20, 0,0, 20,RENAMES_MENU,256,166, (button -> {
				nameField.setText(text4.getString());
			}), text4);
			button4text.setText(text4);
			iconSlot4.setIcon(new ItemIcon(icon4));
			icon4.setCustomName(text4);
		}

		if (4 + page * 5 <= renameListSize - 1) {
			Text text5 = Text.of(renameList.getName(4 + page * 5));
			button5 = new TexturedButtonWidget(this.width / 2 - 200 + 10 - 28, this.height / 2 - 83 + 118, 118, 20, 0,0, 20,RENAMES_MENU,256,166, (button -> {
				nameField.setText(text5.getString());
			}), text5);
			button5text.setText(text5);
			iconSlot5.setIcon(new ItemIcon(icon5));
			icon5.setCustomName(text5);
		}
	}

	private void updatePageWidgets() {
		remove(pageUp);
		remove(pageDown);
		addDrawableChild(pageUp);
		addDrawableChild(pageDown);
		if (page == 0) {
			pageDown.active = false;
		}
		pageUp.active = 5 + page * 5 <= renameListSize - 1;
		pageCount.setText(Text.of(page + 1 + "/" + (renameList.getName().length + 4) / 5));
	}

	private void onSearch(String search) {
		screenUpdate();
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
				if (list[i].startsWith(match)) {
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