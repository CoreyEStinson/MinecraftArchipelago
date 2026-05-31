package com.minecraftarchipelago.collections;

import net.minecraft.util.Identifier;

import java.util.*;

public class ItemCollectionRegistry {

    private static final List<ItemCollection> COLLECTIONS = new ArrayList<>();
    private static final Map<String, ItemCollection> BY_ID = new HashMap<>();

    static {
        // --- All Music Discs (19) ---
        register("all_music_discs", "All Music Discs", List.of(
                v("music_disc_13"),    v("music_disc_cat"),     v("music_disc_blocks"),
                v("music_disc_chirp"), v("music_disc_far"),     v("music_disc_mall"),
                v("music_disc_mellohi"), v("music_disc_stal"),  v("music_disc_strad"),
                v("music_disc_ward"),  v("music_disc_11"),      v("music_disc_wait"),
                v("music_disc_pigstep"), v("music_disc_otherside"), v("music_disc_5"),
                v("music_disc_relic"), v("music_disc_creator"), v("music_disc_creator_music_box"),
                v("music_disc_precipice")
        ));

        // --- All Armor Sets (25 — full 4-piece sets for 6 materials + turtle helmet) ---
        register("all_armor_sets", "All Armor Sets", List.of(
                // Leather (4)
                v("leather_helmet"),    v("leather_chestplate"),
                v("leather_leggings"),  v("leather_boots"),
                // Chainmail (4)
                v("chainmail_helmet"),  v("chainmail_chestplate"),
                v("chainmail_leggings"),v("chainmail_boots"),
                // Iron (4)
                v("iron_helmet"),       v("iron_chestplate"),
                v("iron_leggings"),     v("iron_boots"),
                // Gold (4) — note: "golden" not "gold"
                v("golden_helmet"),     v("golden_chestplate"),
                v("golden_leggings"),   v("golden_boots"),
                // Diamond (4)
                v("diamond_helmet"),    v("diamond_chestplate"),
                v("diamond_leggings"),  v("diamond_boots"),
                // Netherite (4)
                v("netherite_helmet"),  v("netherite_chestplate"),
                v("netherite_leggings"),v("netherite_boots"),
                // Turtle (1)
                v("turtle_helmet")
        ));

        // --- All Pottery Sherds (23) ---
        register("all_pottery_sherds", "All Pottery Sherds", List.of(
                v("angler_pottery_sherd"),    v("archer_pottery_sherd"),
                v("arms_up_pottery_sherd"),   v("blade_pottery_sherd"),
                v("brewer_pottery_sherd"),    v("burn_pottery_sherd"),
                v("danger_pottery_sherd"),    v("explorer_pottery_sherd"),
                v("flow_pottery_sherd"),      v("friend_pottery_sherd"),
                v("guster_pottery_sherd"),    v("heart_pottery_sherd"),
                v("heartbreak_pottery_sherd"),v("howl_pottery_sherd"),
                v("miner_pottery_sherd"),     v("mourner_pottery_sherd"),
                v("plenty_pottery_sherd"),    v("prize_pottery_sherd"),
                v("scrape_pottery_sherd"),    v("sheaf_pottery_sherd"),
                v("shelter_pottery_sherd"),   v("skull_pottery_sherd"),
                v("snort_pottery_sherd")
        ));

        // --- All Smithing Templates (19 — includes netherite upgrade + 18 trims) ---
        register("all_trims", "All Smithing Templates", List.of(
                v("netherite_upgrade_smithing_template"),
                v("bolt_armor_trim_smithing_template"),
                v("coast_armor_trim_smithing_template"),
                v("dune_armor_trim_smithing_template"),
                v("eye_armor_trim_smithing_template"),
                v("flow_armor_trim_smithing_template"),
                v("host_armor_trim_smithing_template"),
                v("raiser_armor_trim_smithing_template"),
                v("rib_armor_trim_smithing_template"),
                v("sentry_armor_trim_smithing_template"),
                v("shaper_armor_trim_smithing_template"),
                v("silence_armor_trim_smithing_template"),
                v("snout_armor_trim_smithing_template"),
                v("spire_armor_trim_smithing_template"),
                v("tide_armor_trim_smithing_template"),
                v("vex_armor_trim_smithing_template"),
                v("ward_armor_trim_smithing_template"),
                v("wayfinder_armor_trim_smithing_template"),
                v("wild_armor_trim_smithing_template")
        ));

        // --- Rare Items (6) ---
        register("rare_items", "Rare Items", List.of(
                v("mace"), v("elytra"), v("trident"),
                v("enchanted_golden_apple"), v("totem_of_undying"), v("conduit")
        ));

        // --- All Flowers (19) ---
        register("all_flowers", "All Flowers", List.of(
                v("dandelion"),         v("poppy"),          v("blue_orchid"),
                v("allium"),            v("azure_bluet"),    v("red_tulip"),
                v("orange_tulip"),      v("white_tulip"),    v("pink_tulip"),
                v("oxeye_daisy"),       v("cornflower"),     v("lily_of_the_valley"),
                v("torchflower"),       v("wither_rose"),    v("sunflower"),
                v("lilac"),             v("rose_bush"),      v("peony"),
                v("pitcher_plant")
        ));

        // --- All Heads (6) ---
        register("all_heads", "All Heads", List.of(
                v("zombie_head"),          v("skeleton_skull"),
                v("creeper_head"),         v("wither_skeleton_skull"),
                v("dragon_head"),          v("piglin_head")
        ));

        // --- All Dyes (16) ---
        register("all_dyes", "All Dyes", List.of(
                v("white_dye"),      v("orange_dye"),    v("magenta_dye"),
                v("light_blue_dye"), v("yellow_dye"),    v("lime_dye"),
                v("pink_dye"),       v("gray_dye"),      v("light_gray_dye"),
                v("cyan_dye"),       v("purple_dye"),    v("blue_dye"),
                v("brown_dye"),      v("green_dye"),     v("red_dye"),
                v("black_dye")
        ));

        // --- Complete Weaponry (6) ---
        register("all_weapons", "Complete Weaponry", List.of(
                v("diamond_sword"), v("bow"), v("crossbow"),
                v("trident"),       v("mace"), v("shield")
        ));
    }

    // --- Helpers ---

    private static Identifier v(String path) {
        return Identifier.ofVanilla(path);
    }

    private static void register(String id, String displayName, List<Identifier> items) {
        ItemCollection c = new ItemCollection(id, displayName, items);
        COLLECTIONS.add(c);
        BY_ID.put(id, c);
    }

    public static List<ItemCollection> getAll() {
        return Collections.unmodifiableList(COLLECTIONS);
    }

    public static ItemCollection get(String id) {
        return BY_ID.get(id);
    }

    private ItemCollectionRegistry() {}

}
