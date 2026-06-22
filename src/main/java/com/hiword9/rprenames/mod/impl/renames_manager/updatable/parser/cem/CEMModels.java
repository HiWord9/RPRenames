package com.hiword9.rprenames.mod.impl.renames_manager.updatable.parser.cem;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import java.util.Arrays;

public class CEMModels {
    public static final ModelData[] data = new ModelData[]{
            new ModelData("allay", new String[]{"allay/allay"}, EntityTypes.ALLAY),
            new ModelData("armadillo", new String[]{"armadillo"}, EntityTypes.ARMADILLO),
            new ModelData("armor_stand", new String[]{"armorstand/wood"}, EntityTypes.ARMOR_STAND),
            new ModelData("axolotl",
                    new String[]{
                            "axolotl/axolotl_blue", "axolotl/axolotl_cyan", "axolotl/axolotl_gold",
                            "axolotl/axolotl_lucy", "axolotl/axolotl_wild"
                    }, EntityTypes.AXOLOTL),
            new ModelData("bat", new String[]{"bat"}, EntityTypes.BAT),
            new ModelData("polar_bear", new String[]{"bear/polarbear"}, EntityTypes.POLAR_BEAR),
            new ModelData("bee",
                    new String[]{
                            "bee/bee", "bee/bee_angry", "bee/bee_angry_nectar", "bee/bee_nectar", "bee/bee_stinger"
                    }, EntityTypes.BEE),
            new ModelData("blaze", new String[]{"blaze"}, EntityTypes.BLAZE),
            new ModelData("bogged", new String[]{"skeleton/bogged"}, EntityTypes.BOGGED),
            new ModelData("breeze", new String[]{"breeze/breeze", "breeze/breeze_wind"}, EntityTypes.BREEZE),
            new ModelData("camel", new String[]{"camel/camel"}, EntityTypes.CAMEL),
            new ModelData("cat",
                    new String[]{
                            "cat/all_black", "cat/black", "cat/british_shorthair", "cat/calico", "cat/jellie",
                            "cat/persian", "cat/ragdoll", "cat/red", "cat/siamese", "cat/tabby", "cat/white"
                    }, EntityTypes.CAT),
            new ModelData("cat_collar", new String[]{"cat/cat_collar"}, EntityTypes.CAT),
            new ModelData("ocelot", new String[]{"cat/ocelot"}, EntityTypes.OCELOT),
            new ModelData("chicken", new String[]{"chicken"}, EntityTypes.CHICKEN),
            new ModelData("mooshroom",
                    new String[]{
                            "cow/brown_mooshroom", "cow/red_mooshroom"
                    }, EntityTypes.MOOSHROOM),
            new ModelData("cow", new String[]{"cow/cow"}, EntityTypes.COW),
            new ModelData("creeper", new String[]{"creeper/creeper"}, EntityTypes.CREEPER),
            new ModelData("creeper_charge", new String[]{"creeper/creeper_armor"}, EntityTypes.CREEPER),
            new ModelData("dolphin", new String[]{"dolphin"}, EntityTypes.DOLPHIN),
            new ModelData("enderman", new String[]{"enderman/enderman"}, EntityTypes.ENDERMAN),
            new ModelData("endermite", new String[]{"endermite"}, EntityTypes.ENDERMITE),
            new ModelData("cod", new String[]{"fish/cod"}, EntityTypes.COD),
            new ModelData("puffer_fish_big", new String[]{"fish/pufferfish"}, EntityTypes.PUFFERFISH),
            new ModelData("puffer_fish_medium", new String[]{"fish/pufferfish"}, EntityTypes.PUFFERFISH),
            new ModelData("puffer_fish_small", new String[]{"fish/pufferfish"}, EntityTypes.PUFFERFISH),
            new ModelData("salmon", new String[]{"fish/salmon"}, EntityTypes.SALMON),
            new ModelData("tropical_fish_a", new String[]{"fish/tropical_a"}, EntityTypes.TROPICAL_FISH),
            new ModelData("tropical_fish_b", new String[]{"fish/tropical_b"}, EntityTypes.TROPICAL_FISH),
            new ModelData("fox",
                    new String[]{
                            "fox/fox", "fox/fox_sleep", "fox/snow_fox", "fox/snow_fox_sleep"
                    }, EntityTypes.FOX),
            new ModelData("frog",
                    new String[]{
                            "frog/cold_frog", "frog/temperate_frog", "frog/warm_frog"
                    }, EntityTypes.FROG),
            new ModelData("ghast", new String[]{"ghast/ghast", "ghast/ghast_shooting"}, EntityTypes.GHAST),
            new ModelData("goat", new String[]{"goat/goat"}, EntityTypes.GOAT),
            new ModelData("guardian", new String[]{"guardian"}, EntityTypes.GUARDIAN),
            new ModelData("elder_guardian", new String[]{"guardian_elder"}, EntityTypes.ELDER_GUARDIAN),
            new ModelData("hoglin", new String[]{"hoglin/hoglin"}, EntityTypes.HOGLIN),
            new ModelData("zoglin", new String[]{"hoglin/zoglin"}, EntityTypes.ZOGLIN),
            new ModelData("donkey", new String[]{"horse/donkey"}, EntityTypes.DONKEY),
            new ModelData("horse",
                    new String[]{
                            "horse/horse_black", "horse/horse_brown", "horse/horse_chestnut", "horse/horse_creamy",
                            "horse/horse_darkbrown", "horse/horse_gray", "horse/horse_markings_blackdots",
                            "horse/horse_markings_white", "horse/horse_markings_whitedots",
                            "horse/horse_markings_whitefield", "horse/horse_white"
                    }, EntityTypes.HORSE),
            new ModelData("skeleton_horse", new String[]{"horse/horse_skeleton"}, EntityTypes.SKELETON_HORSE),
            new ModelData("zombie_horse", new String[]{"horse/horse_zombie"}, EntityTypes.ZOMBIE_HORSE),
            new ModelData("mule", new String[]{"horse/mule"}, EntityTypes.MULE),
            new ModelData("evoker", new String[]{"illager/evoker"}, EntityTypes.EVOKER),
            new ModelData("illusioner", new String[]{"illager/illusioner"}, EntityTypes.ILLUSIONER),
            new ModelData("pillager", new String[]{"illager/pillager"}, EntityTypes.PILLAGER),
            new ModelData("ravager", new String[]{"illager/ravager"}, EntityTypes.RAVAGER),
            new ModelData("vex", new String[]{"illager/vex", "illager/vex_charging"}, EntityTypes.VEX),
            new ModelData("vindicator", new String[]{"illager/vindicator"}, EntityTypes.VINDICATOR),
            new ModelData("iron_golem", new String[]{"iron_golem/iron_golem"}, EntityTypes.IRON_GOLEM),
            new ModelData("llama",
                    new String[]{
                            "llama/brown", "llama/creamy", "llama/gray", "llama/white"
                    }, EntityTypes.LLAMA),
            new ModelData("panda",
                    new String[]{
                            "panda/aggressive_panda", "panda/brown_panda", "panda/lazy_panda", "panda/panda",
                            "panda/playful_panda", "panda/weak_panda", "panda/worried_panda"
                    }, EntityTypes.PANDA),
            new ModelData("parrot",
                    new String[]{
                            "parrot/parrot_blue", "parrot/parrot_green", "parrot/parrot_grey",
                            "parrot/parrot_red_blue", "parrot/parrot_yellow_blue"
                    }, EntityTypes.PARROT),
            new ModelData("phantom", new String[]{"phantom"}, EntityTypes.PHANTOM),
            new ModelData("pig", new String[]{"pig/pig"}, EntityTypes.PIG),
            new ModelData("piglin", new String[]{"piglin/piglin"}, EntityTypes.PIGLIN),
            new ModelData("piglin_brute", new String[]{"piglin/piglin_brute"}, EntityTypes.PIGLIN_BRUTE),
            new ModelData("zombified_piglin", new String[]{"piglin/zombified_piglin"}, EntityTypes.ZOMBIFIED_PIGLIN),
            new ModelData("rabbit",
                    new String[]{
                            "rabbit/black", "rabbit/brown", "rabbit/gold", "rabbit/salt",
                            "rabbit/toast", "rabbit/white", "rabbit/white_splotched"
                    }, EntityTypes.RABBIT),
            new ModelData("sheep", new String[]{"sheep/sheep"}, EntityTypes.SHEEP),
            new ModelData("sheep_wool", new String[]{"sheep/sheep_fur"}, EntityTypes.SHEEP),
            new ModelData("shulker", new String[]{"shulker/shulker"}, EntityTypes.SHULKER),
            new ModelData("silverfish", new String[]{"silverfish"}, EntityTypes.SILVERFISH),
            new ModelData("skeleton", new String[]{"skeleton/skeleton"}, EntityTypes.SKELETON),
            new ModelData("stray", new String[]{"skeleton/stray"}, EntityTypes.STRAY),
            new ModelData("wither_skeleton", new String[]{"skeleton/wither_skeleton"}, EntityTypes.WITHER_SKELETON),
            new ModelData("magma_cube", new String[]{"slime/magmacube"}, EntityTypes.MAGMA_CUBE),
            new ModelData("slime", new String[]{"slime/slime"}, EntityTypes.SLIME),
            new ModelData("sniffer", new String[]{"sniffer/sniffer"}, EntityTypes.SNIFFER),
            new ModelData("snow_golem", new String[]{"snow_golem"}, EntityTypes.SNOW_GOLEM),
            new ModelData("cave_spider", new String[]{"spider/cave_spider"}, EntityTypes.CAVE_SPIDER),
            new ModelData("spider", new String[]{"spider/spider"}, EntityTypes.SPIDER),
            new ModelData("glow_squid", new String[]{"squid/glow_squid"}, EntityTypes.GLOW_SQUID),
            new ModelData("squid", new String[]{"squid/squid"}, EntityTypes.SQUID),
            new ModelData("strider", new String[]{"strider/strider", "strider/strider_cold"}, EntityTypes.STRIDER),
            new ModelData("tadpole", new String[]{"tadpole/tadpole"}, EntityTypes.TADPOLE),
            new ModelData("turtle", new String[]{"turtle/big_sea_turtle"}, EntityTypes.TURTLE),
            new ModelData("villager",
                    new String[]{
                            "villager/profession/armorer", "villager/profession/butcher", "villager/profession/cartographer",
                            "villager/profession/cleric", "villager/profession/farmer", "villager/profession/fisherman",
                            "villager/profession/fletcher", "villager/profession/leatherworker", "villager/profession/librarian",
                            "villager/profession/mason", "villager/profession/nitwit", "villager/profession/shepherd",
                            "villager/profession/toolsmith", "villager/profession/weaponsmith", "villager/type/desert",
                            "villager/type/jungle", "villager/type/plains", "villager/type/savanna", "villager/type/snow",
                            "villager/type/swamp", "villager/type/taiga"
                    }, EntityTypes.VILLAGER),
            new ModelData("wandering_trader", new String[]{"wandering_trader"}, EntityTypes.WANDERING_TRADER),
            new ModelData("warden", new String[]{"warden/warden"}, EntityTypes.WARDEN),
            new ModelData("witch", new String[]{"witch"}, EntityTypes.WITCH),
            new ModelData("wither", new String[]{"wither/wither", "wither/wither_invulnerable"}, EntityTypes.WITHER),
            new ModelData("wither_armor", new String[]{"wither/wither_armor"}, EntityTypes.WITHER),
            new ModelData("wolf", new String[]{"wolf/wolf", "wolf/wolf_angry", "wolf/wolf_tame"}, EntityTypes.WOLF),
            new ModelData("wolf_collar", new String[]{"wolf/wolf_collar"}, EntityTypes.WOLF),
            new ModelData("drowned", new String[]{"zombie/drowned"}, EntityTypes.DROWNED),
            new ModelData("husk", new String[]{"zombie/husk"}, EntityTypes.HUSK),
            new ModelData("zombie", new String[]{"zombie/zombie"}, EntityTypes.ZOMBIE),
            new ModelData("zombie_villager",
                    new String[]{
                            "zombie_villager/profession/armorer", "zombie_villager/profession/butcher",
                            "zombie_villager/profession/cartographer", "zombie_villager/profession/cleric",
                            "zombie_villager/profession/farmer", "zombie_villager/profession/fisherman",
                            "zombie_villager/profession/fletcher", "zombie_villager/profession/leatherworker",
                            "zombie_villager/profession/librarian", "zombie_villager/profession/mason",
                            "zombie_villager/profession/nitwit", "zombie_villager/profession/shepherd",
                            "zombie_villager/profession/toolsmith", "zombie_villager/profession/weaponsmith",
                            "zombie_villager/type/desert", "zombie_villager/type/jungle", "zombie_villager/type/plains",
                            "zombie_villager/type/savanna", "zombie_villager/type/snow", "zombie_villager/type/swamp",
                            "zombie_villager/type/taiga", "zombie_villager/zombie_villager"
                    }, EntityTypes.ZOMBIE_VILLAGER),

            // Mobs added in Minecraft 26.2
            new ModelData("copper_golem",
                    new String[]{
                            "copper_golem/copper_golem", "copper_golem/copper_golem_exposed",
                            "copper_golem/copper_golem_weathered", "copper_golem/copper_golem_oxidized"
                    }, EntityTypes.COPPER_GOLEM),
            new ModelData("creaking", new String[]{"creaking/creaking"}, EntityTypes.CREAKING),
            new ModelData("happy_ghast", new String[]{"ghast/happy_ghast", "ghast/happy_ghast_baby"}, EntityTypes.HAPPY_GHAST),
            new ModelData("nautilus", new String[]{"nautilus/nautilus", "nautilus/nautilus_baby"}, EntityTypes.NAUTILUS),
            new ModelData("zombie_nautilus", new String[]{"nautilus/zombie_nautilus", "nautilus/zombie_nautilus_coral"}, EntityTypes.ZOMBIE_NAUTILUS),
            new ModelData("camel_husk", new String[]{"camel/camel_husk"}, EntityTypes.CAMEL_HUSK),
            new ModelData("parched", new String[]{"skeleton/parched"}, EntityTypes.PARCHED),
            new ModelData("sulfur_cube", new String[]{"sulfur_cube/sulfur_cube_outer", "sulfur_cube/sulfur_cube_inner"}, EntityTypes.SULFUR_CUBE)
    };

    public static boolean modelExists(String model) {
        return Arrays.stream(data).anyMatch(data -> data.model().equals(model));
    }

    public static ModelData find(String model) {
        return Arrays.stream(data)
                .filter(d -> d.model().equals(model))
                .findFirst()
                .orElse(null);
    }

    public record ModelData(
            String model,
            String[] textures,
            EntityType<?> mob
    ) {}
}
