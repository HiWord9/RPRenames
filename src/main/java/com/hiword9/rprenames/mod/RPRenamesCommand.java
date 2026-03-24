package com.hiword9.rprenames.mod;

import com.hiword9.rprenames.api.core.rename.Rename;
import com.hiword9.rprenames.api.ext.rename.Informative;
import com.hiword9.rprenames.mod.util.PropertiesHelper;
import com.hiword9.rprenames.mod.util.Util;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.DynamicOps;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.*;
import java.util.regex.Pattern;

import static com.hiword9.rprenames.mod.util.Util.*;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class RPRenamesCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext commandRegistryAccess) {
        dispatcher.register(literal("rprenames")
                .then(literal("info")
                        .executes(context -> info(context.getSource())))
                .then(literal("list")
                        .executes(context -> list(context.getSource()))
                        .then(ClientCommandManager.argument("item", ItemArgument.item(commandRegistryAccess))
                                .executes(context -> list(context.getSource(), ItemArgument.getItem(context, "item").getItem()))))
                .then(literal("solveRegex")
                        .then(ClientCommandManager.argument("regex", StringArgumentType.greedyString())
                                .executes(context -> solveRegex(context.getSource(), StringArgumentType.getString(context, "regex")))))
                .then(literal("disableHints")
                        .executes(context -> disableHints(context.getSource()))));
    }

    private static int disableHints(FabricClientCommandSource source) {
        config().disableTooltipHints = !config().disableTooltipHints;
        config().write();
        if (config().disableTooltipHints) {
            source.sendFeedback(
                    Component.translatable("rprenames.command.disableHints.disabled")
                    .withStyle(ChatFormatting.GOLD)
            );
            source.sendFeedback(
                    Component.translatable(
                            "rprenames.command.disableHints.howToTurnOn",
                            Component.translatable("rprenames.gui.tooltipHint.disable.command")
                                    .withStyle(ChatFormatting.GREEN))
                            .withStyle(ChatFormatting.GOLD)
            );
        } else {
            source.sendFeedback(
                    Component.translatable("rprenames.command.disableHints.enabled")
                    .withStyle(ChatFormatting.GOLD)
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int info(FabricClientCommandSource source) {
        ItemStack itemStack = source.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            source.sendFeedback(
                    Component.translatable("rprenames.command.info.noItemGiven")
                    .withStyle(ChatFormatting.RED)
            );
            return Command.SINGLE_SUCCESS;
        }

        Rename matchRename = null;

        var renames = RPRenames.renamesProvider.getAllRenames();
        if (!renames.isEmpty()) {
            matchRename = getMatch(renames, itemStack);
        }

        if (!(matchRename instanceof Informative informative)) {
            source.sendFeedback(
                    Component.translatable("rprenames.command.info.noRenamesFound")
                    .withStyle(ChatFormatting.RED)
            );
            return Command.SINGLE_SUCCESS;
        }

        source.sendFeedback(
                Component.translatable("rprenames.command.info.foundProperties")
                .withStyle(ChatFormatting.YELLOW)
        );
        print(informative.getInfo(), source);

        return Command.SINGLE_SUCCESS;
    }

    private static void print(List<Component> lines, FabricClientCommandSource source) {
        for (Component text : lines) {
            source.sendFeedback(text);
        }
    }

    public static int list(FabricClientCommandSource source) {
        return list(source, source.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).getItem());
    }

    public static int list(FabricClientCommandSource source, Item item) {
        var renames = RPRenames.renamesProvider.getRenames(item);
        if (!renames.isEmpty()) {
            source.sendFeedback(
                    Component.translatable(
                            "rprenames.command.list.foundRenames",
                            Component.translatable(item.getDescriptionId())
                    )
            );
            printRenameList(renames, source);
        } else {
            source.sendFeedback(
                    Component.translatable(
                            "rprenames.command.list.noRenamesFound",
                            Component.translatable(item.getDescriptionId())
                    ).withStyle(ChatFormatting.RED)
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int solveRegex(FabricClientCommandSource source, String regex) {
        String result = PropertiesHelper.solveRegex(PropertiesHelper.parseEscapes(regex));
        Pattern pattern = Pattern.compile(regex);
        if (pattern.matcher(result).matches()) {
            source.sendFeedback(
                    Component.nullToEmpty(result).copy()
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.LIGHT_PURPLE)
                            .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(regex)))
                            .withClickEvent(new ClickEvent.CopyToClipboard(result))
                    )
            );
        } else {
            source.sendError(
                    Component.translatable("rprenames.command.solveRegex.error")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Component.nullToEmpty(regex).copy()
                                            .withStyle(ChatFormatting.RED)
                            ))
                    )
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Rename getMatch(List<Rename> renames, ItemStack stack) {
        for (Rename r : renames) if (r.matchesStack(stack)) return r;
        return null;
    }

    private static void printRenameList(List<Rename> renames, FabricClientCommandSource source) {
        RPRenames.LOGGER.warn("Generating give commands with components, this may crash!");
        RPRenames.LOGGER.warn("If it is, please report the accident to https://github.com/HiWord9/RPRenames/issues");
        for (Rename r : renames) {
            ItemStack itemStack = r.toStack();

            String components = getComponentsCommandArgument(source, itemStack);

            String giveCommand = "/give @s "
                    + Util.idFromItem(itemStack.getItem())
                    + components
                    + (itemStack.getCount() > 1 ? " " + itemStack.getCount() : "");

            ClickEvent runGive = new ClickEvent.RunCommand(giveCommand);
            source.sendFeedback(
                    Component.translatable("rprenames.command.list.givePrefix")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.GRAY)
                            .withClickEvent(runGive)
                            .withInsertion(giveCommand)
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Component.translatable("rprenames.command.list.runGive")
                            ))
                    )
                    .append(itemStack.getDisplayName().copy()
                            .withStyle(style -> style.withClickEvent(
                                    new ClickEvent.CopyToClipboard(r.getName().getString())
                            ))
                    )
            );
        }
    }

    @SuppressWarnings("unchecked") // I am not quiet sure that this will not crash, but let's try as beta
    private static <T> String getComponentsCommandArgument(FabricClientCommandSource source, ItemStack stack) {
        if (!(stack.getComponents() instanceof PatchedDataComponentMap)) return "";

        DataComponentPatch changes = ((PatchedDataComponentMap) stack.getComponents()).asPatch();
        if (changes.isEmpty()) return "";

        StringBuilder resultBuilder = new StringBuilder();

        resultBuilder.append("[");

        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : changes.entrySet()) {
            DataComponentType<T> componentType = (DataComponentType<T>) entry.getKey();
            Optional<?> optionalData = entry.getValue();

            if (optionalData.isEmpty()) continue;

            Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
            T data = (T) optionalData.get();
            DynamicOps<Tag> nbtOps = source.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            Optional<Tag> optionalDataResult = componentType.codecOrThrow().encodeStart(nbtOps, data).result();

            if (optionalDataResult.isEmpty()) continue;

            resultBuilder.append(id);
            resultBuilder.append("=");
            resultBuilder.append(optionalDataResult.get());
            resultBuilder.append(",");
        }
        if (resultBuilder.charAt(resultBuilder.length() - 1) == ',') {
            resultBuilder.deleteCharAt(resultBuilder.length() - 1);
        }
        resultBuilder.append("]");

        return resultBuilder.toString();
    }
}
