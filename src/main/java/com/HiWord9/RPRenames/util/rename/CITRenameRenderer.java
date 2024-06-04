package com.HiWord9.RPRenames.util.rename;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.Tab;
import com.HiWord9.RPRenames.util.gui.Graphics;
import com.HiWord9.RPRenames.util.gui.MultiItemTooltipComponent;
import com.HiWord9.RPRenames.util.gui.widget.RPRWidget;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

public class CITRenameRenderer extends DefaultRenameRenderer implements RenameRenderer.Preview {
    private static final ModConfig config = ModConfig.INSTANCE;

    CITRename rename;
    RPRWidget rprWidget;
    boolean favorite;

    EquipmentSlot equipmentSlot;

    CITRenameRenderer(CITRename rename, RPRWidget rprWidget, boolean favorite) {
        super(rename);
        this.rename = rename;
        this.rprWidget = rprWidget;
        this.favorite = favorite;

        int index = 1;

        if (config.showDescription) {
            ArrayList<TooltipComponent> description = descriptionTooltipsComponentsList(rename);
            tooltipComponents.addAll(index, description);
            index += description.size();
        }

        if (rprWidget.getCurrentTab() == Tab.INVENTORY || rprWidget.getCurrentTab() == Tab.GLOBAL) {
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
        if (config.showNamePattern && rprWidget.getCurrentTab() != Tab.FAVORITE) {
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
                Identifier enchant = Identifier.splitOn(citRename.getEnchantment(), ':');
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
        if ((rprWidget.getCurrentTab() == Tab.INVENTORY || rprWidget.getCurrentTab() == Tab.GLOBAL)
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
                if (!config.disablePlayerPreviewTips) {
                    tooltipAddition.add(TooltipComponent.of(
                            Text.translatable("rprenames.gui.tooltipHint.playerPreviewTip.holdShift")
                                    .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true))
                                    .asOrderedText()));
                }
            } else if (hasShiftDown() != config.playerPreviewByDefault) {
                rprWidget.getScreen().setFocused(null);
                if (!config.disablePlayerPreviewTips) {
                    tooltipAddition.add(TooltipComponent.of(
                            Text.translatable("rprenames.gui.tooltipHint.playerPreviewTip.pressF")
                                    .copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(true))
                                    .asOrderedText()));
                }
            }
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
    public void drawPreview(DrawContext context, int mouseX, int mouseY, ArrayList<TooltipComponent> tooltip) { //todo custom tooltip components
        var client = MinecraftClient.getInstance();
        assert client.player != null;

        boolean shouldPreviewPlayer = hasShiftDown() != config.playerPreviewByDefault;

        int width = Graphics.DEFAULT_PREVIEW_WIDTH;
        int height = Graphics.DEFAULT_PREVIEW_HEIGHT;

        int size;
        int newWidth;
        int newHeight;
        if (shouldPreviewPlayer) {
            double scaleFactorEntity = config.scaleFactorEntity;
            size = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ENTITY * scaleFactorEntity);
            newWidth = (int) (width + size * client.player.getWidth() - 1);
            newHeight = (int) (height + size * client.player.getHeight() - 1);
        } else {
            double scaleFactorItem = config.scaleFactorItem;
            size = (int) (Graphics.DEFAULT_PREVIEW_SIZE_ITEM * scaleFactorItem);
            newWidth = (int) (width / 2 * scaleFactorItem);
            newHeight = (int) (height / 2 * scaleFactorItem);
        }

        var screen = rprWidget.getScreen();
        Vector2ic vector2ic = new PreviewTooltipPositioner(tooltip)
                .getPosition(
                        screen.width, screen.height,
                        mouseX, mouseY,
                        newWidth, newHeight
                );
        int x = vector2ic.x();
        int y = vector2ic.y();

        if (shouldPreviewPlayer) {
            playerPreview(
                    context,
                    x, y,
                    newWidth, newHeight,
                    size,
                    config.spinPlayerPreview,
                    client.player,
                    this.stack
            );
        } else {
            itemPreview(
                    context,
                    x, y,
                    newWidth, newHeight,
                    size,
                    this.stack
            );
        }
    }

    private void playerPreview(DrawContext context, int x, int y, int width, int height, int size, boolean spin, ClientPlayerEntity player, ItemStack item) {
        boolean extraSlotAvailable = true;
        EquipmentSlot extraEquipmentSlot = null;

        if (item.getItem() instanceof ArmorItem armorItem) {
            extraEquipmentSlot = armorItem.getSlotType();
        } else if (Block.getBlockFromItem(item.getItem()) == Blocks.CARVED_PUMPKIN) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (Block.getBlockFromItem(item.getItem()) instanceof AbstractSkullBlock) {
            extraEquipmentSlot = EquipmentSlot.HEAD;
        } else if (item.getItem() instanceof ElytraItem) {
            extraEquipmentSlot = EquipmentSlot.CHEST;
        } else {
            extraSlotAvailable = false;
        }

        if (equipmentSlot == null) {
            if (extraSlotAvailable) {
                equipmentSlot = extraEquipmentSlot;
            } else {
                equipmentSlot = EquipmentSlot.MAINHAND;
            }
        }

        if (isFKeyJustPressed()) {
            if (equipmentSlot == EquipmentSlot.HEAD) {
                if (extraSlotAvailable && extraEquipmentSlot != EquipmentSlot.HEAD && config.alwaysAllowPlayerPreviewHead) {
                    equipmentSlot = extraEquipmentSlot;
                } else {
                    equipmentSlot = EquipmentSlot.MAINHAND;
                }
            } else if (equipmentSlot == EquipmentSlot.MAINHAND) {
                equipmentSlot = EquipmentSlot.OFFHAND;
            } else if (equipmentSlot == EquipmentSlot.OFFHAND) {
                if (config.alwaysAllowPlayerPreviewHead) {
                    equipmentSlot = EquipmentSlot.HEAD;
                } else {
                    if (extraSlotAvailable) {
                        equipmentSlot = extraEquipmentSlot;
                    } else {
                        equipmentSlot = EquipmentSlot.MAINHAND;
                    }
                }
            } else if (equipmentSlot == extraEquipmentSlot) {
                equipmentSlot = EquipmentSlot.MAINHAND;
            }
        }

        boolean isArmor = false;
        int armorSlot = 0;
        if (equipmentSlot != EquipmentSlot.MAINHAND && equipmentSlot != EquipmentSlot.OFFHAND) {
            isArmor = true;
            if (equipmentSlot == EquipmentSlot.LEGS) {
                armorSlot = 1;
            } else if (equipmentSlot == EquipmentSlot.CHEST) {
                armorSlot = 2;
            } else if (equipmentSlot == EquipmentSlot.HEAD) {
                armorSlot = 3;
            }
        }

        assert player != null;
        ItemStack temp = player.getEquippedStack(equipmentSlot);

        if (isArmor) {
            player.getInventory().armor.set(armorSlot, item);
        } else {
            player.equipStack(equipmentSlot, item);
        }

        float h = player.bodyYaw;
        float i = player.getYaw();
        float j = player.getPitch();
        float k = player.prevHeadYaw;
        float l = player.headYaw;

        Graphics.drawTooltipBackground(context, x, y, width, height, favorite);
        Graphics.renderEntityInBox(context,
                new ScreenRect(x, y, width, height), Graphics.TOOLTIP_CORNER,
                size, player, spin);

        player.bodyYaw = h;
        player.setYaw(i);
        player.setPitch(j);
        player.prevHeadYaw = k;
        player.headYaw = l;

        if (isArmor) {
            player.getInventory().armor.set(armorSlot, temp);
        } else {
            player.equipStack(equipmentSlot, temp);
        }
    }

    private void itemPreview(DrawContext context, int x, int y, int width, int height, int size, ItemStack itemStack) {
        Graphics.drawTooltipBackground(context, x, y, width, height, favorite, 400);

        int newX = x + width / 2 - size / 2;
        int newY = y + height / 2 - size / 2;
        Graphics.renderStack(context, itemStack, newX, newY, 400, size);
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
