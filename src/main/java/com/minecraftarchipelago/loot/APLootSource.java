package com.minecraftarchipelago.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record APLootSource(
        String id,
        Type type,
        float chance,
        String displayName,
        List<Identifier> tables,
        List<Identifier> entities,
        int villagerLevel
) {
    public enum Type {
        LOOT_TABLE,
        ENTITY_DROP,
        VILLAGER_TRADE,
        ENTITY_DEATH_FALLBACK
    }

    public static List<APLootSource> parseAll(JsonObject root) {
        List<APLootSource> sources = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("sources")) {
            sources.add(parse(element.getAsJsonObject()));
        }
        return sources;
    }

    public static APLootSource parse(JsonObject object) {
        String id = object.get("id").getAsString();
        Type type = Type.valueOf(object.get("type").getAsString().toUpperCase());
        float chance = object.get("chance").getAsFloat();
        String displayName = object.has("display_name")
                ? object.get("display_name").getAsString()
                : id;
        int villagerLevel = object.has("villager_level")
                ? object.get("villager_level").getAsInt()
                : 0;

        return new APLootSource(
                id,
                type,
                chance,
                displayName,
                identifiers(object, "tables"),
                identifiers(object, "entities"),
                villagerLevel
        );
    }

    private static List<Identifier> identifiers(JsonObject object, String key) {
        if (!object.has(key)) return List.of();

        List<Identifier> identifiers = new ArrayList<>();
        for (JsonElement element : object.getAsJsonArray(key)) {
            Identifier identifier = Identifier.tryParse(element.getAsString());
            if (identifier != null) {
                identifiers.add(identifier);
            }
        }
        return List.copyOf(identifiers);
    }

    public boolean matchesLootTable(Identifier tableId) {
        if (type != Type.LOOT_TABLE && type != Type.ENTITY_DROP) return false;
        return tables.contains(tableId);
    }
}
