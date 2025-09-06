package com.HiWord9.RPRenames.util.rename.renderer;

import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;

public class DefaultRenameRenderer<T extends AbstractRename> implements RenameRenderer {
    T rename;
    ItemStack stack;
    ArrayList<TooltipComponent> tooltipComponents = new ArrayList<>();

    public DefaultRenameRenderer(T rename) {
        this.rename = rename;
        stack = rename.toStack();

        tooltipComponents.add(nameTooltipComponent());
    }

    public TooltipComponent nameTooltipComponent() {
        return TooltipComponent.of(Text.of(getDisplayName()).asOrderedText());
    }

    public TooltipComponent packNameTooltipComponent() {
        String packName = getDisplayPackName();

        boolean zip = false;
        if (packName.endsWith(".zip")) {
            zip = true;
            packName = packName.substring(0, packName.length() - 4);
        }

        MutableText packNameText = Text.of(packName).copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD));

        return TooltipComponent.of(
                !zip ? packNameText.asOrderedText() : packNameText
                        .append(Text.of(".zip").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .asOrderedText()
        );
    }

    public String getDisplayName() {
        return rename.getName();
    }

    public String getDisplayPackName() {
        return rename.getPackName();
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        Graphics.renderStack(
                context,
                stack,
                buttonX + (buttonWidth - Graphics.STACK_IN_SLOT_SIZE) / 2,
                buttonY + (buttonHeight - Graphics.STACK_IN_SLOT_SIZE) / 2
        );
    }

    @Override
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        Graphics.drawTooltip(
                context,
                MinecraftClient.getInstance().textRenderer,
                tooltipComponents,
                mouseX, mouseY,
                HoveredTooltipPositioner.INSTANCE
        );
    }
}
