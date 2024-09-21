package com.HiWord9.RPRenames;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.PropertiesHelper;
import com.HiWord9.RPRenames.util.rename.*;
import com.HiWord9.RPRenames.util.config.generation.ParserHelper;
import com.HiWord9.RPRenames.util.rename.type.AbstractRename;
import com.HiWord9.RPRenames.util.rename.type.CEMRename;
import com.HiWord9.RPRenames.util.rename.type.CITRename;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.DynamicOps;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.component.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class RPRenamesCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal("rprenames")
                .then(literal("info")
                        .executes(context -> info(context.getSource())))
                .then(literal("list")
                        .executes(context -> list(context.getSource()))
                        .then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
                                .executes(context -> list(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item").getItem()))))
                .then(literal("solveRegex")
                        .then(ClientCommandManager.argument("regex", StringArgumentType.greedyString())
                                .executes(context -> solveRegex(context.getSource(), StringArgumentType.getString(context, "regex")))))
                .then(literal("disableHints")
                        .executes(context -> disableHints(context.getSource()))));
    }

    private static int disableHints(FabricClientCommandSource source) {
        ModConfig config = ModConfig.INSTANCE;
        config.disableTooltipHints = !config.disableTooltipHints;
        config.write();
        if (config.disableTooltipHints) {
            source.sendFeedback(
                    Text.translatable("rprenames.command.disableHints.disabled")
                    .formatted(Formatting.GOLD)
            );
            source.sendFeedback(
                    Text.translatable(
                            "rprenames.command.disableHints.howToTurnOn",
                            Text.translatable("rprenames.gui.tooltipHint.disable.command")
                                    .formatted(Formatting.GREEN))
                            .formatted(Formatting.GOLD)
            );
        } else {
            source.sendFeedback(
                    Text.translatable("rprenames.command.disableHints.enabled")
                    .formatted(Formatting.GOLD)
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int info(FabricClientCommandSource source) {
        ItemStack itemStack = source.getPlayer().getStackInHand(Hand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            source.sendFeedback(
                    Text.translatable("rprenames.command.info.noItemGiven")
                    .formatted(Formatting.RED)
            );
            return Command.SINGLE_SUCCESS;
        }

        AbstractRename matchRename = null;

        ArrayList<AbstractRename> renames = RenamesManager.getRenames(itemStack.getItem());
        if (!renames.isEmpty()) {
            matchRename = getMatch(renames, itemStack);
        }

        if (matchRename == null) {
            source.sendFeedback(
                    Text.translatable("rprenames.command.info.noRenamesFound")
                    .formatted(Formatting.RED)
            );
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendFeedback(
                    Text.translatable("rprenames.command.info.foundProperties")
                    .formatted(Formatting.YELLOW)
            );
        }

        printAbstractRenameInfo(matchRename, source);

        if (matchRename instanceof CEMRename cemRename) {
            source.sendFeedback(
                    Text.translatable("rprenames.command.info.cemProperties")
                    .formatted(Formatting.LIGHT_PURPLE)
            );
            printCemRenameInfo(cemRename, source);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void printAbstractRenameInfo(AbstractRename rename, FabricClientCommandSource source) {
        printRenameInfo(rename.getPackName(), rename.getPath(), rename.getProperties(), source);
    }

    public static void printCemRenameInfo(CEMRename rename, FabricClientCommandSource source) {
        printRenameInfo(rename.getMob().getPackName(), rename.getMob().getPath(), rename.getMob().getProperties(), source);
    }

    public static void printRenameInfo(String packName, String path, Properties properties, FabricClientCommandSource source) {
        if (properties != null) {
            printProperties(properties, source);
        }

        if (packName != null && path != null) {
            printPath(path, packName, source);
        }

        if (path == null) {
            source.sendFeedback(
                    Text.translatable("rprenames.command.info.noPath")
                            .formatted(Formatting.RED)
            );
        }

        if (packName == null) {
            source.sendFeedback(
                    Text.translatable("rprenames.command.info.noRpName")
                            .formatted(Formatting.RED)
            );
        } else {
            source.sendFeedback(
                    Text.translatable("rprenames.command.info.rpName")
                            .formatted(Formatting.GOLD)
                            .append(Text.of(" = ").copy().formatted(Formatting.GRAY))
                            .append(Text.of(packName).copy().formatted(Formatting.BLUE))
            );
        }
    }

    public static int list(FabricClientCommandSource source) {
        return list(source, source.getPlayer().getStackInHand(Hand.MAIN_HAND).getItem());
    }

    public static int list(FabricClientCommandSource source, Item item) {
        ArrayList<AbstractRename> renames = RenamesManager.getRenames(item);
        if (!renames.isEmpty()) {
            source.sendFeedback(
                    Text.translatable(
                            "rprenames.command.list.foundRenames",
                            Text.translatable(item.getTranslationKey())
                    )
            );
            printRenameList(renames, source);
        } else {
            source.sendFeedback(
                    Text.translatable(
                            "rprenames.command.list.noRenamesFound",
                            Text.translatable(item.getTranslationKey())
                    ).formatted(Formatting.RED)
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int solveRegex(FabricClientCommandSource source, String regex) {
        String result = PropertiesHelper.solveRegex(PropertiesHelper.parseEscapes(regex));
        Pattern pattern = Pattern.compile(regex);
        if (pattern.matcher(result).matches()) {
            source.sendFeedback(
                    Text.of(result).copy()
                    .fillStyle(Style.EMPTY
                            .withColor(Formatting.LIGHT_PURPLE)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(regex)))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, result))
                    )
            );
        } else {
            source.sendError(
                    Text.translatable("rprenames.command.solveRegex.error")
                    .fillStyle(Style.EMPTY
                            .withColor(Formatting.RED)
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.of(regex).copy()
                                            .formatted(Formatting.RED)
                            ))
                    )
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static AbstractRename getMatch(ArrayList<AbstractRename> renames, ItemStack stack) {
        String name = stack.getName().getString();
        for (AbstractRename r : renames) {
            boolean nameValid;
            String nbtName = r.getNamePattern();
            boolean caseInsensitive = false;
            if (nbtName.startsWith("iregex:") || nbtName.startsWith("ipattern:")) {
                nbtName = nbtName.substring(1);
                caseInsensitive = true;
            }
            if (nbtName.startsWith("regex:") || nbtName.startsWith("pattern:")) {
                if (nbtName.startsWith("regex:")) {
                    nbtName = nbtName.substring(6);
                } else if (nbtName.startsWith("pattern:")) {
                    nbtName = nbtName.substring(8);
                    nbtName = nbtName.replace("*", ".*").replace("?", ".+");
                }
                nbtName = PropertiesHelper.parseEscapes(nbtName);
                Pattern pattern = Pattern.compile(caseInsensitive ? nbtName.toUpperCase(Locale.ROOT) : nbtName);
                nameValid = pattern.matcher(caseInsensitive ? name.toUpperCase(Locale.ROOT) : name).matches();
            } else {
                nameValid = name.equals(r.getName());
            }
            if (!nameValid) continue;
            if (r instanceof CITRename citRename) {
                if (!new CITRename.CraftMatcher(citRename, stack).matches()) continue;
            }
            return r;
        }
        return null;
    }

    private static void printRenameList(ArrayList<AbstractRename> renames, FabricClientCommandSource source) {
        RPRenames.LOGGER.warn("Generating give commands with components, this may crash!");
        RPRenames.LOGGER.warn("If it is, please report the accident to https://github.com/HiWord9/RPRenames/issues");
        for (AbstractRename r : renames) {
            ItemStack itemStack = r.toStack();

            String components = getComponentsCommandArgument(source, itemStack);

            String giveCommand = "/give @s "
                    + ParserHelper.getIdAndPath(itemStack.getItem())
                    + components
                    + (r instanceof CITRename citRename ?
                    (citRename.getStackSize() == 1 ? "" : " " + citRename.getStackSize()) : "");

            ClickEvent runGive = new ClickEvent(ClickEvent.Action.RUN_COMMAND, giveCommand);
            source.sendFeedback(
                    Text.translatable("rprenames.command.list.givePrefix")
                    .fillStyle(Style.EMPTY
                            .withColor(Formatting.GRAY)
                            .withClickEvent(runGive)
                            .withInsertion(giveCommand)
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text.translatable("rprenames.command.list.runGive")
                            ))
                    )
                    .append(itemStack.toHoverableText().copy()
                            .styled(style -> style.withClickEvent(new ClickEvent(
                                    ClickEvent.Action.COPY_TO_CLIPBOARD,
                                    r.getName()
                            )))
                    )
            );
        }
    }

    @SuppressWarnings("unchecked") // I am not quiet sure that this will not crash, but let's try as beta
    private static <T> String getComponentsCommandArgument(FabricClientCommandSource source, ItemStack stack) {
        ComponentChanges changes = ((ComponentMapImpl) stack.getComponents()).getChanges();
        if (changes.isEmpty()) return "";

        StringBuilder resultBuilder = new StringBuilder();

        resultBuilder.append("[");

        for (Map.Entry<ComponentType<?>, Optional<?>> entry : changes.entrySet()) {
            ComponentType<T> componentType = (ComponentType<T>) entry.getKey();
            Optional<?> optionalData = entry.getValue();

            if (optionalData.isEmpty()) continue;

            Identifier id = Registries.DATA_COMPONENT_TYPE.getId(componentType);
            T data = (T) optionalData.get();
            DynamicOps<NbtElement> nbtOps = source.getRegistryManager().getOps(NbtOps.INSTANCE);
            Optional<NbtElement> optionalDataResult = componentType.getCodecOrThrow().encodeStart(nbtOps, data).result();

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

    private static void printProperties(Properties properties, FabricClientCommandSource source) {
        for (String s : properties.stringPropertyNames()) {
            source.sendFeedback(
                    Text.of(s).copy().formatted(Formatting.GOLD)
                    .append(Text.of("=").copy().formatted(Formatting.GRAY))
                    .append(Text.of(properties.getProperty(s)).copy().formatted(Formatting.GREEN))
            );
        }
    }

    private static void printPath(String path, String packName, FabricClientCommandSource source) {
        String dirPath = path.substring(0, path.lastIndexOf("/"));
        source.sendFeedback(
                Text.translatable(
                        "rprenames.command.info.located",
                        Text.of(path).copy()
                                .fillStyle(Style.EMPTY
                                        .withColor(Formatting.YELLOW)
                                        .withUnderline(true)
                                        .withClickEvent(new ClickEvent(
                                                ClickEvent.Action.OPEN_FILE,
                                                packName.equals("server") ? "server-resource-packs/" : "resourcepacks/"
                                                        + (packName.endsWith(".zip") ? packName : dirPath)
                                        ))
                                )
                )
        );
    }
}
