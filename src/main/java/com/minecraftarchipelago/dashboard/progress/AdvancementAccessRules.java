package com.minecraftarchipelago.dashboard.progress;

import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AdvancementAccessRules {
    private static final String MOD_ID = "minecraftarchipelago";

    private static final Map<Identifier, Rule> RULES = new HashMap<>();

    static {
        put("story/lava_bucket", rule(Region.OVERWORLD, stage("Bucket", "items/bucket")));
        put("story/enter_the_nether", rule(Region.OVERWORLD, stage("Flint and Steel", "items/flint_and_steel")));
        put("story/cure_zombie_villager", rule(
                Region.OVERWORLD,
                stage("Golden Apple", "items/golden_apple"),
                stage("Potion", "items/potion")
        ));
        put("story/follow_ender_eye", rule(Region.OVERWORLD, stage("Eye of Ender", "items/ender_eye")));
        put("story/enter_the_end", rule(Region.END));

        put("nether/netherite_armor", rule(Region.NETHER, stage("Smithing Table", "blocks/smithing_table")));
        put("nether/brew_potion", rule(Region.NETHER, stage("Brewing Stand", "blocks/brewing_stand")));
        put("nether/create_beacon", rule(Region.NETHER, stage("Beacon", "blocks/beacon")));
        put("nether/create_full_beacon", rule(Region.NETHER, stage("Beacon", "blocks/beacon")));
        put("nether/all_potions", rule(Region.NETHER, stage("Potion", "items/potion")));
        put("nether/all_effects", rule(
                Region.NETHER,
                stage("Beacon", "blocks/beacon"),
                stage("Conduit", "items/conduit"),
                stage("Ominous Bottle", "items/ominous_bottle"),
                stage("Potion", "items/potion"),
                stage("Warden Spawning", "gamerules/warden_spawning")
        ));

        put("story/deflect_arrow", rule(Region.OVERWORLD, stage("Shield", "items/shield")));
        put("adventure/spyglass_at_parrot", rule(Region.OVERWORLD, stage("Spyglass", "items/spyglass")));
        put("adventure/spyglass_at_ghast", rule(
                Region.NETHER,
                stage("Spyglass", "items/spyglass")
        ));
        put("adventure/spyglass_at_dragon", rule(
                Region.END,
                stage("Spyglass", "items/spyglass")
        ));
        put("adventure/trim_with_any_armor_pattern", rule(
                Region.OVERWORLD,
                stage("Smithing Table", "blocks/smithing_table")
        ));
        put("adventure/trim_with_all_exclusive_armor_patterns", rule(
                Region.END,
                stage("Smithing Table", "blocks/smithing_table")
        ));
        put("adventure/ol_betsy", rule(Region.OVERWORLD, stage("Crossbow", "items/crossbow")));
        put("adventure/two_birds_one_arrow", rule(
                Region.OVERWORLD,
                stage("Crossbow", "items/crossbow"),
                stage("Phantom Spawning", "gamerules/phantom_spawning")
        ));
        put("adventure/whos_the_pillager_now", rule(
                Region.OVERWORLD,
                stage("Crossbow", "items/crossbow")
        ));
        put("adventure/arbalistic", rule(Region.OVERWORLD, stage("Crossbow", "items/crossbow")));
        put("adventure/shoot_arrow", rule(
                Region.OVERWORLD,
                anyStage("Bow or Crossbow", "items/bow", "items/crossbow")
        ));
        put("adventure/sniper_duel", rule(
                Region.OVERWORLD,
                anyStage("Bow or Crossbow", "items/bow", "items/crossbow")
        ));
        put("adventure/bullseye", rule(
                Region.OVERWORLD,
                anyStage("Bow or Crossbow", "items/bow", "items/crossbow")
        ));
        put("adventure/throw_trident", rule(Region.OVERWORLD, stage("Trident", "items/trident")));
        put("adventure/very_very_frightening", rule(
                Region.OVERWORLD,
                stage("Trident", "items/trident")
        ));
        put("adventure/lightning_rod_with_villager_no_fire", rule(
                Region.OVERWORLD,
                stage("Trident", "items/trident"),
                stage("Lightning Rod", "items/lightning_rod")
        ));
        put("adventure/sleep_in_bed", rule(Region.OVERWORLD, stage("Bed", "blocks/bed")));
        put("nether/use_lodestone", rule(Region.NETHER));
        put("adventure/read_power_of_chiseled_bookshelf", rule(Region.NETHER));
        put("adventure/summon_iron_golem", rule(Region.OVERWORLD, stage("Shears", "items/shears")));
        put("adventure/walk_on_powder_snow_with_leather_boots", rule(
                Region.OVERWORLD,
                armorTier("Leather Armor", 1)
        ));
        put("adventure/totem_of_undying", rule(
                Region.OVERWORLD,
                stage("Totem of Undying", "items/totem_of_undying")
        ));
        put("adventure/hero_of_the_village", rule(
                Region.OVERWORLD,
                stage("Raids", "gamerules/raids")
        ));
        put("adventure/revaulting", rule(
                Region.OVERWORLD,
                stage("Ominous Bottle", "items/ominous_bottle")
        ));
        put("adventure/overoverkill", rule(Region.OVERWORLD, stage("Mace", "items/mace")));
        put("adventure/kill_all_mobs", rule(Region.END));

        put("husbandry/fishy_business", rule(
                Region.OVERWORLD,
                stage("Fishing Rod", "items/fishing_rod")
        ));
        put("husbandry/tadpole_in_a_bucket", rule(Region.OVERWORLD, stage("Bucket", "items/bucket")));
        put("husbandry/tactical_fishing", rule(Region.OVERWORLD, stage("Bucket", "items/bucket")));
        put("husbandry/axolotl_in_a_bucket", rule(Region.OVERWORLD, stage("Bucket", "items/bucket")));
        put("husbandry/safely_harvest_honey", rule(
                Region.OVERWORLD,
                stage("Campfire", "blocks/campfire")
        ));
        put("husbandry/ride_a_boat_with_a_goat", rule(
                Region.OVERWORLD,
                stage("Boat", "items/boat")
        ));
        put("husbandry/remove_wolf_armor", rule(
                Region.OVERWORLD,
                stage("Shears", "items/shears")
        ));
        put("husbandry/froglights", rule(Region.NETHER));
        put("husbandry/bred_all_animals", rule(Region.NETHER));
        put("husbandry/obtain_netherite_hoe", rule(
                Region.OVERWORLD,
                stage("Flint and Steel", "items/flint_and_steel"),
                stage("Smithing Table", "blocks/smithing_table")
        ));
        put("husbandry/balanced_diet", rule(Region.END));
    }

    public static AccessResult evaluate(
            Identifier advancementId,
            Set<Identifier> unlockedStages
    ) {
        Rule rule = RULES.getOrDefault(
                advancementId,
                new Rule(inferRegion(advancementId), List.of())
        );

        LinkedHashSet<String> missing = new LinkedHashSet<>();

        addRegionRequirements(rule.region(), unlockedStages, missing);

        for (Requirement requirement : rule.requirements()) {
            if (!requirement.isMet(unlockedStages)) {
                missing.add(requirement.label());
            }
        }

        return new AccessResult(missing.isEmpty(), List.copyOf(missing));
    }

    private static void addRegionRequirements(
            Region region,
            Set<Identifier> unlockedStages,
            Set<String> missing
    ) {
        if (region == Region.NETHER || region == Region.END) {
            Identifier flintAndSteel = stageId("items/flint_and_steel");

            if (!unlockedStages.contains(flintAndSteel)) {
                missing.add("Flint and Steel");
            }
        }

        if (region == Region.END) {
            Identifier eyeOfEnder = stageId("items/ender_eye");

            if (!unlockedStages.contains(eyeOfEnder)) {
                missing.add("Eye of Ender");
            }
        }
    }

    private static Region inferRegion(Identifier advancementId) {
        String path = advancementId.getPath();

        if (path.startsWith("nether/")) {
            return Region.NETHER;
        }

        if (path.startsWith("end/")) {
            return Region.END;
        }

        return Region.OVERWORLD;
    }

    private static void put(String advancementPath, Rule rule) {
        RULES.put(Identifier.ofVanilla(advancementPath), rule);
    }

    private static Rule rule(Region region, Requirement... requirements) {
        return new Rule(region, List.of(requirements));
    }

    private static Requirement stage(String label, String stagePath) {
        return new Requirement(label, Set.of(stageId(stagePath)));
    }

    private static Requirement anyStage(String label, String... stagePaths) {
        Set<Identifier> acceptedStages = new LinkedHashSet<>();

        for (String stagePath : stagePaths) {
            acceptedStages.add(stageId(stagePath));
        }

        return new Requirement(label, Set.copyOf(acceptedStages));
    }

    private static Requirement toolTier(String label, int tier) {
        return switch (tier) {
            case 1 -> anyStage(
                    label,
                    "tools/stone_tools",
                    "tools/iron_tools",
                    "tools/diamond_tools",
                    "tools/netherite_tools"
            );
            case 2 -> anyStage(
                    label,
                    "tools/iron_tools",
                    "tools/diamond_tools",
                    "tools/netherite_tools"
            );
            case 3 -> anyStage(
                    label,
                    "tools/diamond_tools",
                    "tools/netherite_tools"
            );
            case 4 -> stage(label, "tools/netherite_tools");
            default -> throw new IllegalArgumentException("Unknown tool tier: " + tier);
        };
    }

    private static Requirement armorTier(String label, int tier) {
        return switch (tier) {
            case 1 -> anyStage(
                    label,
                    "armor/leather_armor",
                    "armor/iron_armor",
                    "armor/diamond_armor",
                    "armor/netherite_armor"
            );
            case 2 -> anyStage(
                    label,
                    "armor/iron_armor",
                    "armor/diamond_armor",
                    "armor/netherite_armor"
            );
            case 3 -> anyStage(
                    label,
                    "armor/diamond_armor",
                    "armor/netherite_armor"
            );
            case 4 -> stage(label, "armor/netherite_armor");
            default -> throw new IllegalArgumentException("Unknown armor tier: " + tier);
        };
    }

    private static Identifier stageId(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public record AccessResult(boolean ready, List<String> missingRequirements) {
    }

    private record Rule(Region region, List<Requirement> requirements) {
    }

    private record Requirement(String label, Set<Identifier> acceptedStages) {
        private boolean isMet(Set<Identifier> unlockedStages) {
            for (Identifier stage : acceptedStages) {
                if (unlockedStages.contains(stage)) {
                    return true;
                }
            }

            return false;
        }
    }

    private enum Region {
        OVERWORLD,
        NETHER,
        END
    }

    private AdvancementAccessRules() {
    }
}
