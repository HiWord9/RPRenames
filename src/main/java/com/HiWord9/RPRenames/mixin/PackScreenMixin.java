package com.HiWord9.RPRenames.mixin;

import com.HiWord9.RPRenames.configManager;
import net.minecraft.client.gui.screen.pack.PackScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackScreen.class)
public abstract class PackScreenMixin{

	@Inject(at = @At("RETURN"), method = "close")
	private void listCreator(CallbackInfo ci) {
		configManager.jsonManage();
	}
}
