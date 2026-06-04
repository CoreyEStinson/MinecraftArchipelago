package com.minecraftarchipelago.loot;

import com.google.gson.JsonParser;
import com.minecraftarchipelago.MinecraftArchipelago;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class APLootSourceRegistry {
    private static final List<APLootSource> SOURCES = new ArrayList<>();

    public static void replaceAll(List<APLootSource> sources) {
        SOURCES.clear();
        SOURCES.addAll(sources);
    }

    public static void clear() {
        SOURCES.clear();
    }

    public static List<APLootSource> getAll() {
        return List.copyOf(SOURCES);
    }

    public static List<APLootSource> getLootTableSources() {
        return SOURCES.stream()
                .filter(source -> source.type() == APLootSource.Type.LOOT_TABLE
                        || source.type() == APLootSource.Type.ENTITY_DROP)
                .toList();
    }

    public static List<APLootSource> getVillagerTradeSourcesForLevel(int level) {
        return SOURCES.stream()
                .filter(source -> source.type() == APLootSource.Type.VILLAGER_TRADE)
                .filter(source -> source.villagerLevel() == level)
                .toList();
    }

    public static Optional<APLootSource> getById(String id) {
        return SOURCES.stream().filter(source -> source.id().equals(id)).findFirst();
    }

    public static Optional<APLootSource> findLootTableSource(Identifier tableId) {
        return getLootTableSources().stream()
                .filter(source -> source.matchesLootTable(tableId))
                .findFirst();
    }

    public static void loadBundledDefaults() {
        try (var stream = APLootSourceRegistry.class.getClassLoader()
                .getResourceAsStream("data/minecraftarchipelago/aploot_sources/sources.json")) {
            if (stream == null) {
                MinecraftArchipelago.LOGGER.warn("[AP] Bundled AP loot source config was not found.");
                return;
            }
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                replaceAll(APLootSource.parseAll(JsonParser.parseReader(reader).getAsJsonObject()));
            }
        } catch (Exception e) {
            MinecraftArchipelago.LOGGER.error("[AP] Failed to load bundled AP loot source config: {}", e.getMessage());
        }
    }

    private APLootSourceRegistry() {
    }
}
