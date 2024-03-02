package com.HiWord9.RPRenames;

import com.HiWord9.RPRenames.util.config.ConfigManager;
import com.HiWord9.RPRenames.util.config.Rename;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
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
                                .executes(context -> solveRegex(context.getSource(), StringArgumentType.getString(context, "regex"))))));
    }

    public static int info(FabricClientCommandSource source) {
        ItemStack itemStack = source.getPlayer().getStackInHand(Hand.MAIN_HAND);
        if (itemStack.isEmpty()) {
            source.sendFeedback(Text.of("Hold Renamed Item to view its Rename Properties!").copy()
                    .fillStyle(Style.EMPTY.withColor(Formatting.RED).withItalic(true)));
        }

        Rename matchRename = null;
        String itemId = ConfigManager.getIdAndPath(itemStack.getItem());

        ArrayList<Rename> renames = ConfigManager.getRenames(itemId);
        if (!renames.isEmpty()) {
            matchRename = getMatch(
                    renames,
                    itemStack.getName().getString(),
                    itemStack.getCount(),
                    itemStack.getDamage(),
                    EnchantmentHelper.fromNbt(itemStack.getEnchantments()),
                    itemId
            );
        }

        if (matchRename == null) {
            source.sendFeedback(Text.of("No Renames were found").copy().fillStyle(Style.EMPTY.withColor(Formatting.RED)));
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendFeedback(Text.of("Found following Rename Properties:").copy().fillStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
        }

        Properties properties = matchRename.getProperties();
        if (properties != null) {
            printProperties(properties, source);
        }
        if (matchRename.getPackName() != null) {
            if (matchRename.getPath() != null) {
                printPath(matchRename.getPath(), matchRename.getPackName(), source);
            }
        }
        if (matchRename.isCEM()) {
            source.sendFeedback(Text.of("CEM Properties:").copy().fillStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)));
            printProperties(matchRename.getMob().properties(), source);
            if (matchRename.getPackName() != null) {
                if (matchRename.getMob().path() != null) {
                    printPath(matchRename.getMob().path(), matchRename.getPackName(), source);
                }
            }
        }
        if (matchRename.getPath() == null && (!matchRename.isCEM() || matchRename.getMob().path() == null)) {
            source.sendFeedback(Text.of("Couldn't get Path to Properties File for this Rename").copy().fillStyle(Style.EMPTY.withColor(Formatting.RED)));
            if (matchRename.getPackName() == null) {
                source.sendFeedback(Text.of("Couldn't get Pack Name for this Rename").copy().fillStyle(Style.EMPTY.withColor(Formatting.RED)));
            } else {
                source.sendFeedback(Text.of("Pack Name").copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                        .append(Text.of(" = ").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.of(matchRename.getPackName()).copy().fillStyle(Style.EMPTY.withColor(Formatting.BLUE))));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int list(FabricClientCommandSource source) {
        return list(source, source.getPlayer().getStackInHand(Hand.MAIN_HAND).getItem());
    }

    public static int list(FabricClientCommandSource source, Item item) {
        ArrayList<Rename> renames = ConfigManager.getRenames(ConfigManager.getIdAndPath(item));
        if (!renames.isEmpty()) {
            source.sendFeedback(Text.of("Found following Renames for ").copy()
                    .append(Text.translatable(item.getTranslationKey()))
                    .append(Text.of(":")));
            printRenameList(renames, item, source);
        } else {
            source.sendFeedback(Text.of("No Renames for ").copy().fillStyle(Style.EMPTY.withColor(Formatting.RED))
                    .append(Text.translatable(item.getTranslationKey()))
                    .append(Text.of(" were found").copy()));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int solveRegex(FabricClientCommandSource source, String regex) {
        String result = ConfigManager.solveRegex(ConfigManager.parseEscapes(regex));
        Pattern pattern = Pattern.compile(regex);
        if (pattern.matcher(result).matches()) {
            source.sendFeedback(Text.of(result).copy().fillStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(regex)))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, result))));
        } else {
            source.sendError(Text.of("Error occurred on getting string matching regex").copy()
                    .fillStyle(Style.EMPTY.withColor(Formatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(regex).copy()
                                    .fillStyle(Style.EMPTY.withColor(Formatting.RED))))));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Rename getMatch(ArrayList<Rename> renames, String name, int stackSize, int damage, Map<Enchantment, Integer> enchantments, String damagedItem) {
        for (Rename r : renames) {
            boolean nameValid;
            String nbtName = r.getOriginalNbtDisplayName();
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
                nbtName = ConfigManager.parseEscapes(nbtName);
                Pattern pattern = Pattern.compile(caseInsensitive ? nbtName.toUpperCase(Locale.ROOT) : nbtName);
                nameValid = pattern.matcher(caseInsensitive ? name.toUpperCase(Locale.ROOT) : name).matches();
            } else {
                nameValid = name.equals(r.getName());
            }
            if (!nameValid) continue;
            boolean stackSizeValid = Rename.isInBounds(stackSize, r.getOriginalStackSize());
            if (!stackSizeValid) continue;
            boolean damageValid = Rename.isInBounds(damage, r.getOriginalDamage(), damagedItem);
            if (!damageValid) continue;
            boolean enchantmentValid = false;
            boolean enchantmentLevelValid = false;
            String enchantName = r.getEnchantment();
            if (enchantName != null) {
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    if (!enchantName.contains(":")) {
                        enchantName = new Identifier(enchantName).toString();
                    }
                    Identifier identifier = Registries.ENCHANTMENT.getId(enchantment);
                    if (identifier == null) continue;
                    if (identifier.toString().equals(enchantName)) {
                        enchantmentValid = true;
                        if (Rename.isInBounds(entry.getValue(), r.getOriginalEnchantmentLevel())) {
                            enchantmentLevelValid = true;
                            break;
                        }
                    }
                }
            } else {
                enchantmentValid = true;
                enchantmentLevelValid = true;
            }
            if (enchantmentValid && enchantmentLevelValid) {
                return r;
            }
        }
        return null;
    }

    private static void printRenameList(ArrayList<Rename> renames, Item item, FabricClientCommandSource source) {
        for (Rename r : renames) {
            ItemStack itemStack = new ItemStack(item);
            itemStack.setCustomName(Text.of(r.getName()));
            itemStack.setCount(r.getStackSize());
            if (r.getDamage() != null && r.getDamage().damage != 0) {
                itemStack.setDamage(r.getDamage().getParsedDamage(item));
            }
            if (r.getEnchantment() != null) {
                itemStack.getOrCreateNbt();
                assert itemStack.getNbt() != null;
                if (!itemStack.getNbt().contains("Enchantments", 9)) {
                    itemStack.getNbt().put("Enchantments", new NbtList());
                }
                NbtList nbtList = itemStack.getNbt().getList("Enchantments", 10);
                nbtList.add(EnchantmentHelper.createNbt(new Identifier(r.getEnchantment()), r.getEnchantmentLevel()));
            }

            assert itemStack.getNbt() != null;
            String nbt = itemStack.getNbt().toString();
            if (nbt.contains("Damage:0")) {
                nbt = nbt.replace("Damage:0", "");
                if (nbt.startsWith("{,")) {
                    nbt = "{" + nbt.substring(2);
                } else if (nbt.endsWith(",}")) {
                    nbt = nbt.substring(0, nbt.length() - 2) + "}";
                }
            }

            String giveCommand = "/give @s "
                    + ConfigManager.getIdAndPath(itemStack.getItem())
                    + nbt
                    + (r.getStackSize() == 1 ? "" : " " + r.getStackSize());

            ClickEvent runGive = new ClickEvent(ClickEvent.Action.RUN_COMMAND, giveCommand);
            source.sendFeedback(Text.of("[/give]").copy()
                    .fillStyle(Style.EMPTY.withColor(Formatting.GRAY)
                            .withClickEvent(runGive)
                            .withInsertion(giveCommand)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Run /give Command"))))
                    .append(itemStack.toHoverableText().copy()
                            .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, r.getName())))));
        }
    }

    private static void printProperties(Properties properties, FabricClientCommandSource source) {
        for (String s : properties.stringPropertyNames()) {
            source.sendFeedback(Text.of(s).copy().fillStyle(Style.EMPTY.withColor(Formatting.GOLD))
                    .append(Text.of("=").copy().fillStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                    .append(Text.of(properties.getProperty(s)).copy().fillStyle(Style.EMPTY.withColor(Formatting.GREEN))));
        }
    }

    private static void printPath(String path, String packName, FabricClientCommandSource source) {
        String dirPath = path.substring(0, path.lastIndexOf("/"));
        source.sendFeedback(Text.of("Located in: ").copy()
                .append(Text.of(path).copy()
                .fillStyle(Style.EMPTY.withColor(Formatting.YELLOW).withUnderline(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.OPEN_FILE,
                                (packName.equals("server") ? "server-resource-packs/" : "resourcepacks/" + (packName.endsWith(".zip") ? packName : dirPath))
                        ))
                ))
        );
    }
}
