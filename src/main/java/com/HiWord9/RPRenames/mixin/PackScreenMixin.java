package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackScreen.class)
public class PackScreenMixin extends Screen {

	protected PackScreenMixin(Text title) {
		super(title);
	}

	private static final ModConfig config = ModConfig.INSTANCE;

	CheckboxWidget toggleConfig;

	@Inject(at = @At("HEAD"), method = "close")
	private void listCreator(CallbackInfo ci) {
		config.createConfig = toggleConfig.isChecked();
		config.write();
	}

	@Inject(at = @At("HEAD"), method = "init")
	private void toggleButton(CallbackInfo ci) {
		boolean isToggled = config.createConfig;
		toggleConfig = new CheckboxWidget(this.width / 2 + config.createConfigCheckboxPosX, this.height - config.createConfigCheckboxPosY, 20, 20, Text.of("Recreate RPR config"),isToggled);
		if (config.showCreateConfigCheckbox) {
			addDrawableChild(toggleConfig);
		}
	}
}
