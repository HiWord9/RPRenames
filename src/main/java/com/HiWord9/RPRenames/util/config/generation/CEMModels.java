package com.HiWord9.RPRenames.util.config.generation;

import net.minecraft.entity.EntityType;

import java.util.Arrays;

public class CEMModels {
    public static final ModelData[] data = new ModelData[]{
            new ModelData("allay", new String[]{"allay/allay"}, EntityType.ALLAY),
            new ModelData("armadillo", new String[]{"armadillo"}, EntityType.ARMADILLO),
            new ModelData("armor_stand", new String[]{"armorstand/wood"}, EntityType.ARMOR_STAND),
            new ModelData("axolotl",
                    new String[]{
                            "axolotl/axolotl_blue", "axolotl/axolotl_cyan", "axolotl/axolotl_gold",
                            "axolotl/axolotl_lucy", "axolotl/axolotl_wild"
                    }, EntityType.AXOLOTL),
            new ModelData("bat", new String[]{"bat"}, EntityType.BAT),
            new ModelData("polar_bear", new String[]{"bear/polarbear"}, EntityType.POLAR_BEAR),
            new ModelData("bee",
                    new String[]{
                            "bee/bee", "bee/bee_angry", "bee/bee_angry_nectar", "bee/bee_nectar", "bee/bee_stinger"
                    }, EntityType.BEE),
            new ModelData("blaze", new String[]{"blaze"}, EntityType.BLAZE),
            new ModelData("bogged", new String[]{"skeleton/bogged"}, EntityType.BOGGED),
            new ModelData("breeze", new String[]{"breeze/breeze", "breeze/breeze_wind"}, EntityType.BREEZE),
            new ModelData("camel", new String[]{"camel/camel"}, EntityType.CAMEL),
            new ModelData("cat",
                    new String[]{
                            "cat/all_black", "cat/black", "cat/british_shorthair", "cat/calico", "cat/jellie",
                            "cat/persian", "cat/ragdoll", "cat/red", "cat/siamese", "cat/tabby", "cat/white"
                    }, EntityType.CAT),
            new ModelData("cat_collar", new String[]{"cat/cat_collar"}, EntityType.CAT),
            new ModelData("ocelot", new String[]{"cat/ocelot"}, EntityType.OCELOT),
            new ModelData("chicken", new String[]{"chicken"}, EntityType.CHICKEN),
            new ModelData("mooshroom",
                    new String[]{
                            "cow/brown_mooshroom", "cow/red_mooshroom"
                    }, EntityType.MOOSHROOM),
            new ModelData("cow", new String[]{"cow/cow"}, EntityType.COW),
            new ModelData("creeper", new String[]{"creeper/creeper"}, EntityType.CREEPER),
            new ModelData("creeper_charge", new String[]{"creeper/creeper_armor"}, EntityType.CREEPER),
            new ModelData("dolphin", new String[]{"dolphin"}, EntityType.DOLPHIN),
            new ModelData("enderman", new String[]{"enderman/enderman"}, EntityType.ENDERMAN),
            new ModelData("endermite", new String[]{"endermite"}, EntityType.ENDERMITE),
            new ModelData("cod", new String[]{"fish/cod"}, EntityType.COD),
            new ModelData("puffer_fish_big", new String[]{"fish/pufferfish"}, EntityType.PUFFERFISH),
            new ModelData("puffer_fish_medium", new String[]{"fish/pufferfish"}, EntityType.PUFFERFISH),
            new ModelData("puffer_fish_small", new String[]{"fish/pufferfish"}, EntityType.PUFFERFISH),
            new ModelData("salmon", new String[]{"fish/salmon"}, EntityType.SALMON),
            new ModelData("tropical_fish_a", new String[]{"fish/tropical_a"}, EntityType.TROPICAL_FISH),
            new ModelData("tropical_fish_b", new String[]{"fish/tropical_b"}, EntityType.TROPICAL_FISH),
            new ModelData("fox",
                    new String[]{
                            "fox/fox", "fox/fox_sleep", "fox/snow_fox", "fox/snow_fox_sleep"
                    }, EntityType.FOX),
            new ModelData("frog",
                    new String[]{
                            "frog/cold_frog", "frog/temperate_frog", "frog/warm_frog"
                    }, EntityType.FROG),
            new ModelData("ghast", new String[]{"ghast/ghast", "ghast/ghast_shooting"}, EntityType.GHAST),
            new ModelData("goat", new String[]{"goat/goat"}, EntityType.GOAT),
            new ModelData("guardian", new String[]{"guardian"}, EntityType.GUARDIAN),
            new ModelData("elder_guardian", new String[]{"guardian_elder"}, EntityType.ELDER_GUARDIAN),
            new ModelData("hoglin", new String[]{"hoglin/hoglin"}, EntityType.HOGLIN),
            new ModelData("zoglin", new String[]{"hoglin/zoglin"}, EntityType.ZOGLIN),
            new ModelData("donkey", new String[]{"horse/donkey"}, EntityType.DONKEY),
            new ModelData("horse",
                    new String[]{
                            "horse/horse_black", "horse/horse_brown", "horse/horse_chestnut", "horse/horse_creamy",
                            "horse/horse_darkbrown", "horse/horse_gray", "horse/horse_markings_blackdots",
                            "horse/horse_markings_white", "horse/horse_markings_whitedots",
                            "horse/horse_markings_whitefield", "horse/horse_white"
                    }, EntityType.HORSE),
            new ModelData("skeleton_horse", new String[]{"horse/horse_skeleton"}, EntityType.SKELETON_HORSE),
            new ModelData("zombie_horse", new String[]{"horse/horse_zombie"}, EntityType.ZOMBIE_HORSE),
            new ModelData("mule", new String[]{"horse/mule"}, EntityType.MULE),
            new ModelData("evoker", new String[]{"illager/evoker"}, EntityType.EVOKER),
            new ModelData("illusioner", new String[]{"illager/illusioner"}, EntityType.ILLUSIONER),
            new ModelData("pillager", new String[]{"illager/pillager"}, EntityType.PILLAGER),
            new ModelData("ravager", new String[]{"illager/ravager"}, EntityType.RAVAGER),
            new ModelData("vex", new String[]{"illager/vex", "illager/vex_charging"}, EntityType.VEX),
            new ModelData("vindicator", new String[]{"illager/vindicator"}, EntityType.VINDICATOR),
            new ModelData("iron_golem", new String[]{"iron_golem/iron_golem"}, EntityType.IRON_GOLEM),
            new ModelData("llama",
                    new String[]{
                            "llama/brown", "llama/creamy", "llama/gray", "llama/white"
                    }, EntityType.LLAMA),
            new ModelData("panda",
                    new String[]{
                            "panda/aggressive_panda", "panda/brown_panda", "panda/lazy_panda", "panda/panda",
                            "panda/playful_panda", "panda/weak_panda", "panda/worried_panda"
                    }, EntityType.PANDA),
            new ModelData("parrot",
                    new String[]{
                            "parrot/parrot_blue", "parrot/parrot_green", "parrot/parrot_grey",
                            "parrot/parrot_red_blue", "parrot/parrot_yellow_blue"
                    }, EntityType.PARROT),
            new ModelData("phantom", new String[]{"phantom"}, EntityType.PHANTOM),
            new ModelData("pig", new String[]{"pig/pig"}, EntityType.PIG),
            new ModelData("piglin", new String[]{"piglin/piglin"}, EntityType.PIGLIN),
            new ModelData("piglin_brute", new String[]{"piglin/piglin_brute"}, EntityType.PIGLIN_BRUTE),
            new ModelData("zombified_piglin", new String[]{"piglin/zombified_piglin"}, EntityType.ZOMBIFIED_PIGLIN),
            new ModelData("rabbit",
                    new String[]{
                            "rabbit/black", "rabbit/brown", "rabbit/gold", "rabbit/salt",
                            "rabbit/toast", "rabbit/white", "rabbit/white_splotched"
                    }, EntityType.RABBIT),
            new ModelData("sheep", new String[]{"sheep/sheep"}, EntityType.SHEEP),
            new ModelData("sheep_wool", new String[]{"sheep/sheep_fur"}, EntityType.SHEEP),
            new ModelData("shulker", new String[]{"shulker/shulker"}, EntityType.SHULKER),
            new ModelData("silverfish", new String[]{"silverfish"}, EntityType.SILVERFISH),
            new ModelData("skeleton", new String[]{"skeleton/skeleton"}, EntityType.SKELETON),
            new ModelData("stray", new String[]{"skeleton/stray"}, EntityType.STRAY),
            new ModelData("wither_skeleton", new String[]{"skeleton/wither_skeleton"}, EntityType.WITHER_SKELETON),
            new ModelData("magma_cube", new String[]{"slime/magmacube"}, EntityType.MAGMA_CUBE),
            new ModelData("slime", new String[]{"slime/slime"}, EntityType.SLIME),
            new ModelData("sniffer", new String[]{"sniffer/sniffer"}, EntityType.SNIFFER),
            new ModelData("snow_golem", new String[]{"snow_golem"}, EntityType.SNOW_GOLEM),
            new ModelData("cave_spider", new String[]{"spider/cave_spider"}, EntityType.CAVE_SPIDER),
            new ModelData("spider", new String[]{"spider/spider"}, EntityType.SPIDER),
            new ModelData("glow_squid", new String[]{"squid/glow_squid"}, EntityType.GLOW_SQUID),
            new ModelData("squid", new String[]{"squid/squid"}, EntityType.SQUID),
            new ModelData("strider", new String[]{"strider/strider", "strider/strider_cold"}, EntityType.STRIDER),
            new ModelData("tadpole", new String[]{"tadpole/tadpole"}, EntityType.TADPOLE),
            new ModelData("turtle", new String[]{"turtle/big_sea_turtle"}, EntityType.TURTLE),
            new ModelData("villager",
                    new String[]{
                            "villager/profession/armorer", "villager/profession/butcher", "villager/profession/cartographer",
                            "villager/profession/cleric", "villager/profession/farmer", "villager/profession/fisherman",
                            "villager/profession/fletcher", "villager/profession/leatherworker", "villager/profession/librarian",
                            "villager/profession/mason", "villager/profession/nitwit", "villager/profession/shepherd",
                            "villager/profession/toolsmith", "villager/profession/weaponsmith", "villager/type/desert",
                            "villager/type/jungle", "villager/type/plains", "villager/type/savanna", "villager/type/snow",
                            "villager/type/swamp", "villager/type/taiga"
                    }, EntityType.VILLAGER),
            new ModelData("wandering_trader", new String[]{"wandering_trader"}, EntityType.WANDERING_TRADER),
            new ModelData("warden", new String[]{"warden/warden"}, EntityType.WARDEN),
            new ModelData("witch", new String[]{"witch"}, EntityType.WITCH),
            new ModelData("wither", new String[]{"wither/wither", "wither/wither_invulnerable"}, EntityType.WITHER),
            new ModelData("wither_armor", new String[]{"wither/wither_armor"}, EntityType.WITHER),
            new ModelData("wolf", new String[]{"wolf/wolf", "wolf/wolf_angry", "wolf/wolf_tame"}, EntityType.WOLF),
            new ModelData("wolf_collar", new String[]{"wolf/wolf_collar"}, EntityType.WOLF),
            new ModelData("drowned", new String[]{"zombie/drowned"}, EntityType.DROWNED),
            new ModelData("husk", new String[]{"zombie/husk"}, EntityType.HUSK),
            new ModelData("zombie", new String[]{"zombie/zombie"}, EntityType.ZOMBIE),
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
                    }, EntityType.ZOMBIE_VILLAGER)
    };

    public static boolean modelExists(String model) {
        return Arrays.stream(data).anyMatch(data -> data.model().equals(model));
    }

    public static ModelData find(String model) {
        for (ModelData modelData : data) {
            if (modelData.model().equals(model)) {
                return modelData;
            }
        }
        return null;
    }

    public record ModelData(
            String model,
            String[] textures,
            EntityType<?> mob
    ) {}
}
