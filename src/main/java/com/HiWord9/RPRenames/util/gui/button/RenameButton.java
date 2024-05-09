package com.HiWord9.RPRenames.util.gui.button;

import com.HiWord9.RPRenames.accessor.AnvilScreenMixinAccessor;
import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.rename.Rename;
import com.HiWord9.RPRenames.util.gui.RenameButtonHolder.ViewMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;

public class RenameButton extends ClickableWidget {
    private static final ModConfig config = ModConfig.INSTANCE;

    private static final Identifier TEXTURE_LIST = new Identifier(RPRenames.MOD_ID, "textures/gui/button.png");
    private static final Identifier TEXTURE_GRID = new Identifier(RPRenames.MOD_ID, "textures/gui/button_grid.png");

    static final int BUTTON_WIDTH_LIST = 127;
    public static final int BUTTON_HEIGHT_LIST = 20;
    public static final int BUTTON_WIDTH_GRID = 25;
    public static final int BUTTON_HEIGHT_GRID = 25;

    static final int TEXTURE_WIDTH_LIST = 127;
    static final int TEXTURE_HEIGHT_LIST = 80;
    static final int TEXTURE_WIDTH_GRID = 50;
    static final int TEXTURE_HEIGHT_GRID = 50;

    static final int FOCUSED_OFFSET_V_LIST = 20;
    static final int FAVORITE_OFFSET_V_LIST = 40;
    static final int FOCUSED_OFFSET_V_GRID = 25;
    static final int FAVORITE_OFFSET_U_GRID = 25;

    boolean selected = false;

    final ViewMode type;
    final public boolean favorite;

    final int indexInInventory;
    final boolean isInInventory;
    final boolean asCurrentItem;
    final PlayerInventory inventory;
    final public Rename rename;
    final boolean enoughStackSize;
    final boolean enoughDamage;
    final boolean hasEnchant;
    final boolean hasEnoughLevels;

    public RenameButton(int x, int y, ViewMode type, boolean favorite,
                        int indexInInventory, boolean isInInventory, boolean asCurrentItem,
                        PlayerInventory inventory, Rename rename,
                        boolean enoughStackSize, boolean enoughDamage,
                        boolean hasEnchant, boolean hasEnoughLevels) {
        super(x, y, type == ViewMode.LIST ? BUTTON_WIDTH_LIST : BUTTON_WIDTH_GRID, type == ViewMode.LIST ? BUTTON_HEIGHT_LIST : BUTTON_HEIGHT_GRID, null);
        this.type = type;
        this.favorite = favorite;

        this.indexInInventory = indexInInventory;
        this.isInInventory = isInInventory;
        this.asCurrentItem = asCurrentItem;
        this.inventory = inventory;
        this.rename = rename;
        this.enoughStackSize = enoughStackSize;
        this.enoughDamage = enoughDamage;
        this.hasEnchant = hasEnchant;
        this.hasEnoughLevels = hasEnoughLevels;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Identifier texture;
        int textureWidth;
        int textureHeight;
        int u = 0;
        int v = 0;
        if (type == ViewMode.LIST) {
            texture = TEXTURE_LIST;
            textureWidth = TEXTURE_WIDTH_LIST;
            textureHeight = TEXTURE_HEIGHT_LIST;
            v += favorite ? FAVORITE_OFFSET_V_LIST : 0;
            v += hovered || (selected && config.highlightSelected) ? FOCUSED_OFFSET_V_LIST : 0;
        } else {
            texture = TEXTURE_GRID;
            textureWidth = TEXTURE_WIDTH_GRID;
            textureHeight = TEXTURE_HEIGHT_GRID;
            u += favorite ? FAVORITE_OFFSET_U_GRID : 0;
            v += hovered || (selected && config.highlightSelected) ? FOCUSED_OFFSET_V_GRID : 0;
        }
        context.drawTexture(texture, getX(), getY(), u, v, getWidth(), getHeight(), textureWidth, textureHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            return execute(button);
        }
        return false;
    }

    public boolean execute(int button) {
        AnvilScreen screen = ((AnvilScreen) MinecraftClient.getInstance().currentScreen);
        if (screen instanceof AnvilScreenMixinAccessor anvilScreenMixinAccessor) {
            anvilScreenMixinAccessor.onRenameButton(button, favorite,
                    indexInInventory, isInInventory, asCurrentItem,
                    inventory, rename,
                    enoughStackSize, enoughDamage,
                    hasEnchant, hasEnoughLevels);
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
