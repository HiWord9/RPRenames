package com.HiWord9.RPRenames.mod.impl.rename.renderer;

import com.HiWord9.RPRenames.api.rename.renderer.RenameRenderer;
import com.HiWord9.RPRenames.api.rename.renderer.SimpleRenameRenderer;
import com.HiWord9.RPRenames.mod.gui.Graphics;
import com.HiWord9.RPRenames.mod.gui.tooltip_component.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.mod.gui.tooltip_component.preview.ItemPreviewTooltipComponent;
import com.HiWord9.RPRenames.mod.gui.tooltip_component.preview.PlayerPreviewTooltipComponent;
import com.HiWord9.RPRenames.mod.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.mod.gui.widget.RPRWidget.Tab;
import com.HiWord9.RPRenames.mod.impl.rename.CITRename;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.HiWord9.RPRenames.mod.gui.Graphics.tooltipOf;
import static com.HiWord9.RPRenames.mod.util.RenameRendererHelper.*;
import static com.HiWord9.RPRenames.mod.util.Util.*;
import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class CITRenameRenderer extends SimpleRenameRenderer<CITRename> implements RenameRenderer.Preview {
    private static final MutableText playerPreviewHintShift = Text.translatable(
            "rprenames.gui.tooltipHint.playerPreview.holdShift",
            Text.translatable("rprenames.key.shift").formatted(Formatting.GRAY)
    ).formatted(Formatting.DARK_GRAY);

    private static final MutableText playerPreviewHintF = Text.translatable(
            "rprenames.gui.tooltipHint.playerPreview.pressF",
            Text.translatable("rprenames.key.f").formatted(Formatting.GRAY)
    ).formatted(Formatting.DARK_GRAY);

    private static final MutableText favoriteHintAdd = Text.translatable(
            "rprenames.gui.tooltipHint.favorite.add",
            Text.translatable("rprenames.key.rmb").formatted(Formatting.GRAY)
    ).formatted(Formatting.DARK_GRAY);

    private static final MutableText favoriteHintRemove = Text.translatable(
            "rprenames.gui.tooltipHint.favorite.remove",
            Text.translatable("rprenames.key.rmb").formatted(Formatting.GRAY)
    ).formatted(Formatting.DARK_GRAY);

    private static final MutableText disableHint = Text.translatable(
            "rprenames.gui.tooltipHint.disable",
            Text.translatable("rprenames.gui.tooltipHint.disable.command").formatted(Formatting.RED)
    ).formatted(Formatting.DARK_RED);

    RPRWidget rprWidget;
    Supplier<Boolean> favoriteSupplier;

    ItemPreviewTooltipComponent itemPreviewTooltipComponent;
    PlayerPreviewTooltipComponent playerPreviewTooltipComponent;

    public CITRenameRenderer(CITRename rename, RPRWidget rprWidget, Supplier<Boolean> favoriteSupplier) {
        super(rename);
        this.rprWidget = rprWidget;
        this.favoriteSupplier = favoriteSupplier;

        int width = Graphics.DEFAULT_PREVIEW_WIDTH;
        int height = Graphics.DEFAULT_PREVIEW_HEIGHT;

        assert player() != null;

        int playerSize = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * config().scaleFactorEntity);
        int playerWidth = (int) (width + playerSize * player().getWidth() - 1);
        int playerHeight = (int) (height + playerSize * player().getHeight() - 1);

        playerPreviewTooltipComponent = new PlayerPreviewTooltipComponent(
                player(), stack,
                playerWidth, playerHeight,
                playerSize,
                config().spinPlayerPreview
        );

        double scaleFactorItem = config().scaleFactorItem;
        int itemSize = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ITEM * scaleFactorItem);
        int itemWidth = (int) ((double) width / 2 * scaleFactorItem);
        int itemHeight = (int) ((double) height / 2 * scaleFactorItem);

        itemPreviewTooltipComponent = new ItemPreviewTooltipComponent(
                stack,
                itemWidth, itemHeight,
                itemSize
        );

        addTooltips();
    }

    protected void addTooltips() {
        if (config().showDescription) {
            var description = descriptionTooltipsComponentsList(rename);
            tooltipComponents.addAll(description);
        }

        if (!rprWidget.getCurrentTab().forCraftItemOnly) {
            MultiItemTooltipComponent component = multiItemTooltipComponent(rprWidget, rename);
            tooltipComponents.add(component);
        }

        if (config().showExtraProperties) {
            var extraProperties = extraPropertiesTooltipComponentsList(rprWidget, rename, config().showOriginalProperties);
            tooltipComponents.addAll(extraProperties);
        }

        if (config().showPackName && rename.getPackName() != null) {
            tooltipComponents.add(packNameTooltipComponent(rename.getPackName()));
        }

        if (config().showNamePattern && rprWidget.getCurrentTab() != Tab.FAVORITE) {
            TooltipComponent pattern = namePatternTooltipComponent(rename);
            if (pattern != null) tooltipComponents.add(pattern);
        }
    }

    private static List<TooltipComponent> extraPropertiesTooltipComponentsList(RPRWidget rprWidget, CITRename citRename, boolean asOriginal) {
        ArrayList<Text> extraProperties = new ArrayList<>();

        var stack = rprWidget.pickItemStackForRename(citRename);
        if (stack == null) stack = rprWidget.getActiveItemStack();

        var craftMatcher = new CITRename.CraftMatcher(citRename, stack);

        if (asOriginal) {
            if (citRename.getStackSize() > 1) {
                extraProperties.add(rawPropertyText(
                        "stackSize",
                        citRename.getOriginalStackSize(),
                        craftMatcher.enoughStackSize()
                ));
            }
            if (citRename.getDamage() != null && citRename.getDamage().damage > 0) {
                extraProperties.add(rawPropertyText(
                        "damage",
                        citRename.getOriginalDamage(),
                        craftMatcher.enoughDamage()
                ));
            }
            if (citRename.getEnchantment() != null) {
                extraProperties.add(rawPropertyText(
                        "enchantmentIDs",
                        citRename.getOriginalEnchantment(),
                        craftMatcher.hasEnchant()
                ));
                if (citRename.getOriginalEnchantmentLevel() != null) {
                    extraProperties.add(rawPropertyText(
                            "enchantmentLevels",
                            citRename.getOriginalEnchantmentLevel(),
                            craftMatcher.hasEnchant()
                    ));
                }
            }
        } else {
            if (citRename.getStackSize() > 1) {
                extraProperties.add(styledCondition(
                        Text.translatable("rprenames.gui.tooltipHint.stackSize")
                                .append(" " + citRename.getStackSize()),
                        craftMatcher.enoughStackSize(),
                        Formatting.GRAY
                ));
            }
            if (citRename.getDamage() != null && citRename.getDamage().damage > 0) {
                extraProperties.add(styledCondition(
                        Text.translatable("rprenames.gui.tooltipHint.damage")
                                .append(" %s%s".formatted(
                                        citRename.getDamage().damage,
                                        citRename.getDamage().percent ? "%" : ""
                                )),
                        craftMatcher.enoughDamage(),
                        Formatting.GRAY
                ));
            }
            if (citRename.getEnchantment() != null) {
                Identifier enchant = citRename.getEnchantment();
                extraProperties.add(styledCondition(
                        Text.translatable("rprenames.gui.tooltipHint.enchantment")
                                .append(Text.of(" ")).append(Text.translatable(
                                        "enchantment." + enchant.getNamespace() + "." + enchant.getPath()
                                ))
                                .append(Text.of(" ")).append(Text.translatable(
                                        "enchantment.level." + citRename.getEnchantmentLevel()
                                )),
                        craftMatcher.hasEnchant() && craftMatcher.hasEnoughLevels(),
                        Formatting.GRAY
                ));
            }
        }

        ArrayList<TooltipComponent> propertiesComponents = new ArrayList<>();
        for (Text line : extraProperties) propertiesComponents.add(tooltipOf(line));
        return propertiesComponents;
    }

    private static MutableText rawPropertyText(String propertyName, String propertyValue, boolean isGood) {
        return Text
                .literal(propertyName).fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                .append(Text.literal("=").fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                .append(styledCondition(Text.literal(propertyValue), isGood, Formatting.GREEN));
    }

    private static MutableText styledCondition(MutableText text, boolean isGood, Formatting goodColor) {
        return text.fillStyle(
                Style.EMPTY.withColor(isGood ? goodColor : Formatting.DARK_RED)
        );
    }

    @Override
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        ArrayList<TooltipComponent> tooltipAddition = new ArrayList<>();

        if (config().enablePreview) {
            boolean shiftDown = hasShiftDown();

            if (!shiftDown && !config().playerPreviewByDefault) {
                if (!config().disableTooltipHints) tooltipAddition.add(tooltipOf(playerPreviewHintShift));
            } else if (shiftDown != config().playerPreviewByDefault) {
                if (!config().disableTooltipHints) tooltipAddition.add(tooltipOf(playerPreviewHintF));

                if (currentScreen() != null) currentScreen().setFocused(null);
            }
        }

        if (!config().disableTooltipHints) {
            tooltipAddition.add(tooltipOf(favoriteSupplier.get() ? favoriteHintRemove : favoriteHintAdd));
            tooltipAddition.add(tooltipOf(disableHint));
        }

        tooltipComponents.addAll(tooltipAddition);

        super.onRenderTooltip(context, mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
        if (config().enablePreview) {
            drawPreview(context, mouseX, mouseY, tooltipComponents);
        }

        tooltipComponents.removeAll(tooltipAddition);
    }

    @Override
    public void drawPreview(DrawContext context, int mouseX, int mouseY, List<TooltipComponent> mainTooltip) {
        boolean shouldPreviewPlayer = hasShiftDown() != config().playerPreviewByDefault;
        TooltipPositioner positioner = new PreviewTooltipPositioner(mainTooltip);

        if (shouldPreviewPlayer) {
            playerPreview(context, mouseX, mouseY, positioner);
        } else {
            itemPreview(context, mouseX, mouseY, positioner);
        }
    }

    private void playerPreview(DrawContext context, int mouseX, int mouseY, TooltipPositioner positioner) {
        if (isFKeyJustPressed()) {
            playerPreviewTooltipComponent.cycleSlots(config().alwaysAllowPlayerPreviewHead);
        }

        Graphics.drawTooltipWithFixedBorders(
                context,
                textRenderer(),
                playerPreviewTooltipComponent,
                mouseX, mouseY,
                positioner,
                favoriteSupplier.get()
        );
    }

    private void itemPreview(DrawContext context, int mouseX, int mouseY, TooltipPositioner positioner) {
        Graphics.drawTooltipWithFixedBorders(
                context,
                textRenderer(),
                itemPreviewTooltipComponent,
                mouseX, mouseY,
                positioner,
                favoriteSupplier.get()
        );
    }

    private boolean fPressFuse = false;

    private boolean isFKeyJustPressed() {
        if (InputUtil.isKeyPressed(client().getWindow().getHandle(), GLFW.GLFW_KEY_F)) {
            if (!fPressFuse) {
                fPressFuse = true;
                return true;
            }
        } else {
            fPressFuse = false;
        }
        return false;
    }
}
