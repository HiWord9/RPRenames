package com.HiWord9.RPRenames.util.rename.renderer;

import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.gui.tooltipcomponent.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.type.Rename;
import com.HiWord9.RPRenames.util.rename.type.HasDescription;
import com.HiWord9.RPRenames.util.rename.type.HasNamePattern;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.HiWord9.RPRenames.util.gui.Graphics.tooltipOf;

public class RenameRendererHelper {

    public static MultiItemTooltipComponent multiItemTooltipComponent(Rename rename) {
        ArrayList<MultiItemTooltipComponent.TooltipItem> tooltipItems = new ArrayList<>();
        for (int i = 0; i < rename.getItems().size(); i++) {
            ItemStack itemStack = rename.toStack(i);
            itemStack.remove(DataComponentTypes.CUSTOM_NAME);
            tooltipItems.add(new MultiItemTooltipComponent.TooltipItem(itemStack, null));
        }
        return new MultiItemTooltipComponent(tooltipItems);
    }

    public static MultiItemTooltipComponent multiItemTooltipComponent(RPRWidget rprWidget, Rename rename) {
        MultiItemTooltipComponent component = multiItemTooltipComponent(rename);
        int i = 0;
        for (MultiItemTooltipComponent.TooltipItem item : component.items) {
            item.setIsInInventory(rprWidget.getInventory().contains(rename.getItems().get(i++)));
        }
        return component;
    }

    public static List<TooltipComponent> descriptionTooltipsComponentsList(HasDescription hasDescription) {
        String description = hasDescription.getDescription();
        ArrayList<TooltipComponent> linesComponents = new ArrayList<>();
        if (description != null) {
            var lines = PropertiesHelper.parseCustomDescription(description);
            for (Text line : lines) linesComponents.add(tooltipOf(line));
        }
        return linesComponents;
    }

    public static TooltipComponent namePatternTooltipComponent(HasNamePattern hasNamePattern) {
        String pattern = hasNamePattern.getNamePattern();
        if (pattern != null) {
            return tooltipOf(
                    Text.literal("Name Pattern: " + pattern)
                            .fillStyle(Style.EMPTY.withColor(Formatting.BLUE))
            );
        }
        return null;
    }

    public static TooltipComponent packNameTooltipComponent(String packName) {
        boolean zip = false;
        if (packName.endsWith(".zip")) {
            zip = true;
            packName = packName.substring(0, packName.length() - 4);
        }

        MutableText packNameText = Text.literal(packName).fillStyle(Style.EMPTY.withColor(Formatting.GOLD));

        return tooltipOf(
                !zip ? packNameText : packNameText
                        .append(Text.literal(".zip").fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
        );
    }
}
