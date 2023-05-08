package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.config.ConfigManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Mixin(PackScreen.class)
public class PackScreenMixin extends Screen {

	protected PackScreenMixin(Text title) {
		super(title);
	}

	String settingsPath = RPRenames.settingsPath;
	File settingsFile = new File(settingsPath);
	File outputFolder = new File("config");
	CheckboxWidget toggleConfig;
	boolean isToggled = true;

	//close (onClose) method_25419
	@Inject(at = @At("HEAD"), method = "close")
	private void listCreator(CallbackInfo ci) {
		if (toggleConfig.isChecked()) {
			ConfigManager.configUpdate();
		}

		if (!outputFolder.exists() || !outputFolder.isDirectory()) {
			outputFolder.mkdir();
		}

		try {
			Properties p = new Properties();
			p.put("createconfig", String.valueOf(toggleConfig.isChecked()));
			FileOutputStream writer = new FileOutputStream(settingsPath);
			p.store(writer, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//init method_25426
	@Inject(at = @At("HEAD"), method = "init")
	private void toggleButton(CallbackInfo ci) {
		if (settingsFile.exists()) {
			try {
				FileReader settings = new FileReader(settingsPath);
				Properties p = new Properties();
				p.load(settings);
				isToggled = Boolean.parseBoolean(p.getProperty("createconfig"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		toggleConfig = new CheckboxWidget(this.width / 2 + 4 + 150 + 4, this.height - 48, 20, 20, Text.of("Recreate RPR config"),isToggled);
		addDrawableChild(toggleConfig);
	}
}
