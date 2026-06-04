package com.minecraftarchipelago.loot;

import com.google.gson.JsonParser;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class APLootSourceRegistryTest {

    @Test
    void bundledSourcesMatchApprovedChances() throws Exception {
        List<APLootSource> sources = readBundledSources();

        assertChance(sources, "structure_chests", 0.33f);
        assertChance(sources, "fishing", 0.04f);
        assertChance(sources, "piglin_bartering", 0.03f);
        assertChance(sources, "trial_vault", 0.20f);
        assertChance(sources, "trial_ominous_vault", 0.35f);
        assertChance(sources, "villager_expert", 0.07f);
        assertChance(sources, "villager_master", 0.15f);
        assertChance(sources, "archaeology", 0.08f);
        assertChance(sources, "mob_common", 0.01f);
        assertChance(sources, "mob_uncommon", 0.03f);
        assertChance(sources, "mob_rare", 0.05f);
        assertChance(sources, "ender_dragon_death_fallback", 0.05f);
    }

    @Test
    void approvedHostileMobsAppearInExactlyOneTier() throws Exception {
        List<APLootSource> sources = readBundledSources();
        Set<Identifier> seen = new HashSet<>();

        assertTier(sources, seen, "mob_common", List.of(
                "minecraft:zombie", "minecraft:zombie_villager", "minecraft:husk", "minecraft:drowned",
                "minecraft:skeleton", "minecraft:stray", "minecraft:bogged",
                "minecraft:spider", "minecraft:cave_spider",
                "minecraft:creeper", "minecraft:slime", "minecraft:magma_cube",
                "minecraft:silverfish", "minecraft:endermite",
                "minecraft:pillager", "minecraft:hoglin"
        ));
        assertTier(sources, seen, "mob_uncommon", List.of(
                "minecraft:guardian", "minecraft:blaze", "minecraft:witch",
                "minecraft:vindicator", "minecraft:vex", "minecraft:shulker",
                "minecraft:ghast", "minecraft:piglin_brute", "minecraft:zoglin",
                "minecraft:wither_skeleton", "minecraft:breeze", "minecraft:phantom"
        ));
        assertTier(sources, seen, "mob_rare", List.of(
                "minecraft:elder_guardian", "minecraft:warden",
                "minecraft:ravager", "minecraft:evoker",
                "minecraft:wither", "minecraft:ender_dragon"
        ));
    }

    @Test
    void structureChestsAreDatapackConfigured() throws Exception {
        APLootSource chests = byId(readBundledSources(), "structure_chests");

        assertEquals(APLootSource.Type.LOOT_TABLE, chests.type());
        assertTrue(chests.tables().contains(Identifier.ofVanilla("chests/village/village_armorer")));
        assertTrue(chests.tables().contains(Identifier.ofVanilla("chests/end_city_treasure")));
        assertTrue(chests.tables().contains(Identifier.ofVanilla("chests/bastion_treasure")));
        assertTrue(chests.tables().size() >= 40);
    }

    @Test
    void enderDragonHasDeathFallbackInsteadOfLootTableEntry() throws Exception {
        List<APLootSource> sources = readBundledSources();
        APLootSource rare = byId(sources, "mob_rare");
        APLootSource fallback = byId(sources, "ender_dragon_death_fallback");

        assertTrue(rare.entities().contains(Identifier.ofVanilla("ender_dragon")));
        assertFalse(rare.tables().contains(Identifier.ofVanilla("entities/ender_dragon")));
        assertEquals(APLootSource.Type.ENTITY_DEATH_FALLBACK, fallback.type());
        assertEquals(List.of(Identifier.ofVanilla("ender_dragon")), fallback.entities());
    }

    private static List<APLootSource> readBundledSources() throws Exception {
        try (var stream = APLootSourceRegistryTest.class.getClassLoader()
                .getResourceAsStream("data/minecraftarchipelago/aploot_sources/sources.json")) {
            assertNotNull(stream, "Bundled AP loot source config should exist.");
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return APLootSource.parseAll(JsonParser.parseReader(reader).getAsJsonObject());
            }
        }
    }

    private static void assertChance(List<APLootSource> sources, String id, float chance) {
        assertEquals(chance, byId(sources, id).chance(), 0.0001f);
    }

    private static void assertTier(List<APLootSource> sources, Set<Identifier> seen, String id, List<String> expected) {
        APLootSource source = byId(sources, id);
        assertEquals(APLootSource.Type.ENTITY_DROP, source.type());
        Set<Identifier> actual = new HashSet<>(source.entities());
        Set<Identifier> expectedIds = new HashSet<>(expected.stream().map(Identifier::of).toList());
        assertEquals(expectedIds, actual);
        for (Identifier entity : actual) {
            assertTrue(seen.add(entity), entity + " should appear in only one mob tier.");
        }
    }

    private static APLootSource byId(List<APLootSource> sources, String id) {
        return sources.stream()
                .filter(source -> source.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
