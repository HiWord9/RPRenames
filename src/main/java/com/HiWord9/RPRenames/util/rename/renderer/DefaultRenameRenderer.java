package com.HiWord9.RPRenames.util.rename.renderer;

import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.tooltipcomponent.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.HiWord9.RPRenames.util.rename.type.Describable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
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

        tooltipComponents.add(nameTooltipComponent(rename));
        if (rename.getPackName() != null) tooltipComponents.add(packNameTooltipComponent(rename));
    }

    public static TooltipComponent nameTooltipComponent(AbstractRename rename) {
        return TooltipComponent.of(Text.of(rename.getName()).asOrderedText());
    }

    public static MultiItemTooltipComponent multiItemTooltipComponent(AbstractRename rename) {
        ArrayList<MultiItemTooltipComponent.TooltipItem> tooltipItems = new ArrayList<>();
        for (int i = 0; i < rename.getItems().size(); i++) {
            ItemStack itemStack = rename.toStack(i);
            itemStack.remove(DataComponentTypes.CUSTOM_NAME);
            tooltipItems.add(new MultiItemTooltipComponent.TooltipItem(itemStack, null));
        }
        return new MultiItemTooltipComponent(tooltipItems);
    }

    public static MultiItemTooltipComponent multiItemTooltipComponent(RPRWidget rprWidget, AbstractRename rename) {
        MultiItemTooltipComponent component = multiItemTooltipComponent(rename);
        int i = 0;
        for (MultiItemTooltipComponent.TooltipItem item : component.items) {
            item.setIsInInventory(rprWidget.getInventory().contains(rename.getItems().get(i++)));
        }
        return component;
    }

    public static ArrayList<TooltipComponent> descriptionTooltipsComponentsList(Describable describable) {
        String description = describable.getDescription();
        ArrayList<TooltipComponent> linesComponents = new ArrayList<>();
        if (description != null) {
            ArrayList<Text> lines = PropertiesHelper.parseCustomDescription(description);
            for (Text line : lines) {
                linesComponents.add(TooltipComponent.of(
                        line.asOrderedText()
                ));
            }
        }
        return linesComponents;
    }

    public static TooltipComponent namePatternTooltipComponent(AbstractRename rename) {
        String pattern = rename.getNamePattern();
        if (pattern != null) {
            return TooltipComponent.of(
                    Text.of("Name Pattern: " + pattern).copy()
                            .fillStyle(Style.EMPTY.withColor(Formatting.BLUE))
                            .asOrderedText());
        }
        return null;
    }

    public static TooltipComponent packNameTooltipComponent(AbstractRename rename) {
        String packName = rename.getPackName();

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
