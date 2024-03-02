package com.HiWord9.RPRenames;

import com.HiWord9.RPRenames.modConfig.ModConfig;
import com.HiWord9.RPRenames.util.config.Rename;
import com.mojang.brigadier.CommandDispatcher;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RPRenames implements ClientModInitializer {
    public static final String MOD_ID = "rprenames";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    public static Path configPathFavorite = Path.of(configPath + "/favorite");

    public static Map<String, ArrayList<Rename>> renames = new HashMap<>();
//    public static Map<String, ArrayList<Rename>> renamesServer = new HashMap<>();

    public static ArrayList<ItemStack> renamedItemStacks = new ArrayList<>();

//    public static URL serverResourcePackURL = null;
//    public static boolean joiningServer = false;
//    public static boolean leavingServer = false;

    public static final File MOD_CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "rprenames.json");

    private static ConfigHolder<ModConfig> configHolder;

    @Override
    public void onInitializeClient() {
        LOGGER.info("RPRenames author like coca-cola zero, but don't tell anyone");
        configHolder = AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        configHolder.registerSaveListener((manager, data) -> ActionResult.SUCCESS);
        ClientCommandRegistrationCallback.EVENT.register(RPRenames::registerCommand);
        ModConfig config = ModConfig.INSTANCE;
        if (config.loadModBuiltinResources) {
            LOGGER.info("Loading RPRenames built-in resource packs");
            FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(container -> {
                ResourceManagerHelper.registerBuiltinResourcePack(asId("vanillish"), container, ResourcePackActivationType.NORMAL);
                ResourceManagerHelper.registerBuiltinResourcePack(asId("default_dark_mode"), container, ResourcePackActivationType.NORMAL);
                ResourceManagerHelper.registerBuiltinResourcePack(asId("high_contrasted"), container, ResourcePackActivationType.NORMAL);
            });
        }

        registerItemGroup();
    }

    public static Identifier asId(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        RPRenamesCommand.register(dispatcher, registryAccess);
    }

    public static void registerItemGroup() {
        Registry.register(
                Registries.ITEM_GROUP,
                new Identifier(MOD_ID, "item_group"),
                ((ItemGroupBuilderMixinAccessor) FabricItemGroup.builder()
                        .displayName(Text.translatable("rprenames.item_group"))
                        .icon(RPRenames::getItemGroupIcon))
                        .setType(ItemGroup.Type.SEARCH)
                        .texture("item_search.png")
                        .build()
        );
    }

    private static ItemStack getItemGroupIcon() {
        ItemStack stack = new ItemStack(Items.KNOWLEDGE_BOOK);
        stack.getOrCreateNbt();
        assert stack.getNbt() != null;
        if (!stack.getNbt().contains("Enchantments", 9)) {
            stack.getNbt().put("Enchantments", new NbtList());
        }
        NbtList nbtList = stack.getNbt().getList("Enchantments", NbtElement.COMPOUND_TYPE);
        nbtList.add(EnchantmentHelper.createNbt(new Identifier("mending"), 1));
        stack.getOrCreateSubNbt(MOD_ID);
        return stack;
    }

    public static boolean verifyItemGroup(ItemGroup itemGroup) {
        ItemStack icon = itemGroup.getIcon();
        icon.getOrCreateNbt();
        return icon.getSubNbt(MOD_ID) != null;
    }
}
