package com.HiWord9.RPRenames.util.gui.widget.button;

import com.HiWord9.RPRenames.RPRenames;
import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.Tab;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.AbstractRename;
import com.HiWord9.RPRenames.util.rename.RenameRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class RenameButton extends ClickableWidget {
    private static final ModConfig config = ModConfig.INSTANCE;
    final int highlightColor = config.getSlotHighlightRGBA();

    private static final Identifier TEXTURE = new Identifier(RPRenames.MOD_ID, "textures/gui/button.png");

    RPRWidget rprWidget;

    public static final int BUTTON_WIDTH = 25;
    public static final int BUTTON_HEIGHT = 25;

    static final int TEXTURE_WIDTH = BUTTON_WIDTH * 2;
    static final int TEXTURE_HEIGHT = BUTTON_HEIGHT * 2;

    static final int FOCUSED_OFFSET_V = BUTTON_WIDTH;
    static final int FAVORITE_OFFSET_U = BUTTON_HEIGHT;

    boolean selected = false;

    final public boolean favorite;
    RenameRenderer renameRendered;
    final public AbstractRename rename;

    public RenameButton(RPRWidget instance, AbstractRename rename,
                        int x, int y,
                        boolean favorite) {
        super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, null);
        rprWidget = instance;
        this.favorite = favorite;
        this.rename = rename;

        renameRendered = rename.getNewRenderer(rprWidget, favorite);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int u = favorite ? FAVORITE_OFFSET_U : 0;
        int v = hovered || (selected && config.highlightSelected) ? FOCUSED_OFFSET_V : 0;
        context.drawTexture(TEXTURE, getX(), getY(), u, v, getWidth(), getHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        renameRendered.onRender(context, mouseX, mouseY, getX(), getY(), getWidth() - 1, getHeight() - 1); // -1 cause of shadow
    }

    public void postRender(DrawContext context, int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return;

        Screen screen = rprWidget.getScreen();
        if ((rprWidget.getCurrentTab() == Tab.INVENTORY || rprWidget.getCurrentTab() == Tab.GLOBAL) && (config.slotHighlightColorALPHA > 0 && config.highlightSlot)) {
            if (screen instanceof HandledScreen<?> handledScreen) {
                highlightSlot(context, handledScreen.x, handledScreen.y, handledScreen.getScreenHandler().slots, highlightColor);
            }
        }
        renameRendered.onRenderTooltip(context, mouseX, mouseY, getX(), getY(), getWidth() - 1, getHeight() - 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.clicked(mouseX, mouseY)) {
            return execute(button);
        }
        return false;
    }

    public boolean execute(int button) {
        rprWidget.onRenameButton(button, favorite, rename);
        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void highlightSlot(DrawContext context, int xOffset, int yOffset, DefaultedList<Slot> slots, int highlightColor) {
        for (Item item : rename.getItems()) {
            int slotNum = -1;
            int i = 0;
            Slot slot = null;
            for (Slot s : slots) {
                if ((i != 1 && i != 2) && s.getStack().isOf(item)) {
                    slotNum = i;
                    slot = s;
                    break;
                }
                i++;
            }
            if (slotNum < 0) continue;

            Graphics.highlightSlot(context, xOffset, yOffset, slot, highlightColor);
        }
    }
}
