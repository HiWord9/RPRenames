package com.hiword9.rprenames.mod.util;

import com.hiword9.rprenames.mod.gui.tooltip_component.MultiItemTooltipComponent;
import com.hiword9.rprenames.mod.gui.widget.RPRWidget;
import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.ext.rename.HasDescription;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

import static com.hiword9.rprenames.mod.gui.Graphics.tooltipOf;

public class RenameRendererHelper {

    public static MultiItemTooltipComponent multiItemTooltipComponent(Rename rename) {
        ArrayList<MultiItemTooltipComponent.TooltipItem> tooltipItems = new ArrayList<>();
        for (int i = 0; i < rename.getItems().size(); i++) {
            ItemStack itemStack = rename.toStack(i);
            itemStack.remove(DataComponents.CUSTOM_NAME);
            tooltipItems.add(new MultiItemTooltipComponent.TooltipItem(itemStack, null));
        }
        return new MultiItemTooltipComponent(tooltipItems);
    }

    public static MultiItemTooltipComponent multiItemTooltipComponent(RPRWidget rprWidget, Rename rename) {
        var component = multiItemTooltipComponent(rename);

        for (var tooltipItem : component.items) {
            tooltipItem.isInInventory = rprWidget.getAvailableItems()
                    .contains(tooltipItem.stack.getItem());
        }

        return component;
    }

    public static List<ClientTooltipComponent> descriptionTooltipsComponentsList(HasDescription hasDescription) {
        String description = hasDescription.getDescription();
        ArrayList<ClientTooltipComponent> linesComponents = new ArrayList<>();
        if (description != null) {
            var lines = PropertiesHelper.parseCustomDescription(description);
            for (Component line : lines) linesComponents.add(tooltipOf(line));
        }
        return linesComponents;
    }

    public static ClientTooltipComponent namePatternTooltipComponent(String namePattern) {
        if (namePattern != null) {
            return tooltipOf(
                    Component.literal("Name Pattern: " + namePattern)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE))
            );
        }
        return null;
    }

    public static ClientTooltipComponent packNameTooltipComponent(String packName) {
        boolean zip = false;
        if (packName.endsWith(".zip")) {
            zip = true;
            packName = packName.substring(0, packName.length() - 4);
        }

        MutableComponent packNameText = Component.literal(packName).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));

        return tooltipOf(
                !zip ? packNameText : packNameText
                        .append(Component.literal(".zip").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))
        );
    }
}
