package com.HiWord9.RPRenames;

import com.HiWord9.RPRenames.util.Tabs;
import com.HiWord9.RPRenames.util.config.Rename;
import net.minecraft.entity.player.PlayerInventory;

public interface AnvilScreenMixinAccessor {
    void switchOpen();

    void addOrRemoveFavorite(boolean add);

    void onPageUp();

    void onPageDown();

    void setTab(Tabs tab);

    Tabs getCurrentTab();

    void onRenameButton(int indexInInventory, boolean isInInventory, boolean asCurrentItem, PlayerInventory inventory, Rename rename, boolean enoughStackSize, boolean enoughDamage, boolean hasEnchant, boolean hasEnoughLevels);
}
