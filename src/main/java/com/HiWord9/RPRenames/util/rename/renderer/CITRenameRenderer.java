package com.HiWord9.RPRenames.util.rename.renderer;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.tooltipcomponent.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.util.gui.tooltipcomponent.preview.ItemPreviewTooltipComponent;
import com.HiWord9.RPRenames.util.gui.tooltipcomponent.preview.PlayerPreviewTooltipComponent;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class CITRenameRenderer extends DefaultRenameRenderer implements RenameRenderer.Preview {
    private static final ModConfig config = ModConfig.INSTANCE;

    private static final TooltipComponent playerPreviewHintShift = TooltipComponent.of(
            Text.translatable("rprenames.gui.tooltipHint.playerPreview.holdShift",
                            Text.translatable("rprenames.key.shift").formatted(Formatting.GRAY))
                    .formatted(Formatting.DARK_GRAY)
                    .asOrderedText()
    );

    private static final TooltipComponent playerPreviewHintF = TooltipComponent.of(
            Text.translatable("rprenames.gui.tooltipHint.playerPreview.pressF",
                            Text.translatable("rprenames.key.f")
                                    .formatted(Formatting.GRAY))
                    .formatted(Formatting.DARK_GRAY)
                    .asOrderedText()
    );

    private static final TooltipComponent favoriteHintAdd = TooltipComponent.of(
            Text.translatable("rprenames.gui.tooltipHint.favorite.add",
                            Text.translatable("rprenames.key.rmb")
                                    .formatted(Formatting.GRAY))
                    .formatted(Formatting.DARK_GRAY)
                    .asOrderedText()
    );

    private static final TooltipComponent favoriteHintRemove = TooltipComponent.of(
            Text.translatable("rprenames.gui.tooltipHint.favorite.remove",
                            Text.translatable("rprenames.key.rmb")
                                    .formatted(Formatting.GRAY))
                    .formatted(Formatting.DARK_GRAY)
                    .asOrderedText()
    );

    private static final TooltipComponent disableHint = TooltipComponent.of(
            Text.translatable("rprenames.gui.tooltipHint.disable",
                            Text.translatable("rprenames.gui.tooltipHint.disable.command")
                                    .formatted(Formatting.GRAY))
                    .formatted(Formatting.DARK_GRAY)
                    .asOrderedText()
    );

    CITRename rename;
    RPRWidget rprWidget;
    boolean favorite;

    ItemPreviewTooltipComponent itemPreviewTooltipComponent;
    PlayerPreviewTooltipComponent playerPreviewTooltipComponent;

    public CITRenameRenderer(CITRename rename, RPRWidget rprWidget, boolean favorite) {
        super(rename);
        this.rename = rename;
        this.rprWidget = rprWidget;
        this.favorite = favorite;

        int itemWidth;
        int itemHeight;
        int itemSize;

        int playerWidth;
        int playerHeight;
        int playerSize;

        int width = Graphics.DEFAULT_PREVIEW_WIDTH;
        int height = Graphics.DEFAULT_PREVIEW_HEIGHT;

        var client = MinecraftClient.getInstance();
        assert client.player != null;

        playerSize = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * config.scaleFactorEntity);
        playerWidth = (int) (width + playerSize * client.player.getWidth() - 1);
        playerHeight = (int) (height + playerSize * client.player.getHeight() - 1);

        playerPreviewTooltipComponent = new PlayerPreviewTooltipComponent(
                client.player, stack,
                playerWidth, playerHeight,
                playerSize,
                config.spinPlayerPreview
        );

        double scaleFactorItem = config.scaleFactorItem;
        itemSize = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ITEM * scaleFactorItem);
        itemWidth = (int) (width / 2 * scaleFactorItem);
        itemHeight = (int) (height / 2 * scaleFactorItem);

        itemPreviewTooltipComponent = new ItemPreviewTooltipComponent(
                stack,
                itemWidth, itemHeight,
                itemSize
        );

        int index = 1;

        if (config.showDescription) {
            ArrayList<TooltipComponent> description = descriptionTooltipsComponentsList(rename);
            tooltipComponents.addAll(index, description);
            index += description.size();
        }

        if (rprWidget.getCurrentTab() == RPRWidget.Tab.INVENTORY || rprWidget.getCurrentTab() == RPRWidget.Tab.GLOBAL) {
            MultiItemTooltipComponent component = multiItemTooltipComponent(rprWidget, rename);
            tooltipComponents.add(index++, component);
        }

        if (config.showExtraProperties) {
            ArrayList<TooltipComponent> extraProperties = extraPropertiesTooltipComponentsList(rprWidget, rename, config.showOriginalProperties);
            tooltipComponents.addAll(index, extraProperties);
            index += extraProperties.size();
        }

        if (!config.showPackName && rename.getPackName() != null) {
            tooltipComponents.remove(index);
        }
        if (config.showNamePattern && rprWidget.getCurrentTab() != RPRWidget.Tab.FAVORITE) {
            TooltipComponent pattern = namePatternTooltipComponent(rename);
            if (pattern != null) tooltipComponents.add(pattern);
        }
    }

    public static ArrayList<TooltipComponent> extraPropertiesTooltipComponentsList(RPRWidget rprWidget, CITRename citRename, boolean asOriginal) {
        ArrayList<Text> extraProperties = new ArrayList<>();

        Item item = rprWidget.firstItemInInventory(citRename);
        boolean asCurrentItem = item == rprWidget.getItemInFirstSlot();
        int indexInInventory = rprWidget.getInventory().indexOf(item);

        ItemStack stack = getItemStackForStack(rprWidget, indexInInventory, asCurrentItem);
        CITRename.CraftMatcher craftMatcher = new CITRename.CraftMatcher(citRename, stack);

        if (asOriginal) {
            if (citRename.getStackSize() > 1) {
                extraProperties.add(Text.of("stackSize")
                        .copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                        .append(Text.of("=")
                                .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.of(citRename.getOriginalStackSize())
                                .copy().fillStyle(Style.EMPTY.withColor(craftMatcher.enoughStackSize()
                                        ? Formatting.GREEN
                                        : Formatting.DARK_RED))));
            }
            if (citRename.getDamage() != null && citRename.getDamage().damage > 0) {
                extraProperties.add(Text.of("damage")
                        .copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                        .append(Text.of("=")
                                .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.of(citRename.getOriginalDamage())
                                .copy().fillStyle(Style.EMPTY.withColor(craftMatcher.enoughDamage()
                                        ? Formatting.GREEN
                                        : Formatting.DARK_RED))));
            }
            if (citRename.getEnchantment() != null) {
                extraProperties.add(Text.of("enchantmentIDs")
                        .copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                        .append(Text.of("=")
                                .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.of(citRename.getOriginalEnchantment())
                                .copy().fillStyle(Style.EMPTY.withColor(craftMatcher.hasEnchant()
                                        ? Formatting.GREEN
                                        : Formatting.DARK_RED))));
                if (citRename.getOriginalEnchantmentLevel() != null) {
                    extraProperties.add(Text.of("enchantmentLevels")
                            .copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                            .append(Text.of("=")
                                    .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.of(citRename.getOriginalEnchantmentLevel())
                                    .copy().fillStyle(Style.EMPTY.withColor(craftMatcher.hasEnchant()
                                            ? Formatting.GREEN
                                            : Formatting.DARK_RED))));
                }
            }
        } else {
            if (citRename.getStackSize() > 1) {
                extraProperties.add(Text.of(
                                Text.translatable("rprenames.gui.tooltipHint.stackSize").getString()
                                        + " " + citRename.getStackSize())
                        .copy().fillStyle(Style.EMPTY.withColor(craftMatcher.enoughStackSize()
                                ? Formatting.GRAY
                                : Formatting.DARK_RED)));
            }
            if (citRename.getDamage() != null && citRename.getDamage().damage > 0) {
                extraProperties.add(Text.of(
                                Text.translatable("rprenames.gui.tooltipHint.damage").getString()
                                        + " " + citRename.getDamage().damage
                                        + (citRename.getDamage().percent ? "%" : ""))
                        .copy().fillStyle(Style.EMPTY.withColor(craftMatcher.enoughDamage()
                                ? Formatting.GRAY
                                : Formatting.DARK_RED)));
            }
            if (citRename.getEnchantment() != null) {
                Identifier enchant = citRename.getEnchantment();
                String namespace = enchant.getNamespace();
                String path = enchant.getPath();
                Text translatedEnchant = Text.translatable("enchantment." + namespace + "." + path);
                Text translatedEnchantLevel = Text.translatable("enchantment.level." + citRename.getEnchantmentLevel());
                extraProperties.add(Text.of(
                                Text.translatable("rprenames.gui.tooltipHint.enchantment").getString()
                                        + " " + translatedEnchant.getString()
                                        + " " + translatedEnchantLevel.getString())
                        .copy().fillStyle(Style.EMPTY.withColor(craftMatcher.hasEnchant() && craftMatcher.hasEnoughLevels()
                                ? Formatting.GRAY
                                : Formatting.DARK_RED)));
            }
        }

        ArrayList<TooltipComponent> propertiesComponents = new ArrayList<>();
        for (Text line : extraProperties) {
            propertiesComponents.add(TooltipComponent.of(
                    line.asOrderedText()
            ));
        }
        return propertiesComponents;
    }

    private static ItemStack getItemStackForStack(RPRWidget rprWidget, int indexInInventory, boolean asCurrentItem) {
        boolean isInInventory = indexInInventory != -1;

        ItemStack stack = rprWidget.getCurrentItem();
        if ((rprWidget.getCurrentTab() == RPRWidget.Tab.INVENTORY || rprWidget.getCurrentTab() == RPRWidget.Tab.GLOBAL)
                && !asCurrentItem && isInInventory) {
            assert MinecraftClient.getInstance().player != null;
            PlayerInventory playerInventory = MinecraftClient.getInstance().player.getInventory();
            stack = playerInventory.main.get(indexInInventory);
        }
        return stack;
    }

    @Override
    public void onRenderTooltip(DrawContext context, int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        ArrayList<TooltipComponent> tooltipAddition = new ArrayList<>();
        if (config.enablePreview) {
            if (!hasShiftDown() && !config.playerPreviewByDefault) {
                if (!config.disableTooltipHints) tooltipAddition.add(playerPreviewHintShift);
            } else if (hasShiftDown() != config.playerPreviewByDefault) {
                if (!config.disableTooltipHints) tooltipAddition.add(playerPreviewHintF);
                Screen screen = MinecraftClient.getInstance().currentScreen;
                if (screen != null) screen.setFocused(null);
            }
        }
        if (!config.disableTooltipHints) {
            tooltipAddition.add(favorite ? favoriteHintRemove : favoriteHintAdd);
            tooltipAddition.add(disableHint);
        }
        tooltipComponents.addAll(tooltipAddition);
        super.onRenderTooltip(context, mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
        if (config.enablePreview) {
            drawPreview(
                    context,
                    mouseX, mouseY,
                    tooltipComponents
            );
        }
        tooltipComponents.removeAll(tooltipAddition);
    }

    @Override
    public void drawPreview(DrawContext context, int mouseX, int mouseY, ArrayList<TooltipComponent> mainTooltip) {
        boolean shouldPreviewPlayer = hasShiftDown() != config.playerPreviewByDefault;
        TooltipPositioner positioner = new PreviewTooltipPositioner(mainTooltip);

        if (shouldPreviewPlayer) {
            playerPreview(
                    context,
                    mouseX, mouseY,
                    positioner
            );
        } else {
            itemPreview(
                    context,
                    mouseX, mouseY,
                    positioner
            );
        }
    }

    private void playerPreview(DrawContext context, int mouseX, int mouseY, TooltipPositioner positioner) {
        if (isFKeyJustPressed()) {
            playerPreviewTooltipComponent.cycleSlots(config.alwaysAllowPlayerPreviewHead);
        }

        Graphics.drawTooltipWithFixedBorders(
                context,
                MinecraftClient.getInstance().textRenderer,
                playerPreviewTooltipComponent,
                mouseX, mouseY,
                positioner,
                favorite
        );
    }

    private void itemPreview(DrawContext context, int mouseX, int mouseY, TooltipPositioner positioner) {
        Graphics.drawTooltipWithFixedBorders(
                context,
                MinecraftClient.getInstance().textRenderer,
                itemPreviewTooltipComponent,
                mouseX, mouseY,
                positioner,
                favorite
        );
    }

    private boolean fPressFuse = false;

    private boolean isFKeyJustPressed() {
        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F)) {
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
