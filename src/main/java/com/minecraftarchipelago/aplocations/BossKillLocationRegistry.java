package com.minecraftarchipelago.aplocations;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class BossKillLocationRegistry {

    private static final Map<Identifier, Long> BOSS_LOCATIONS = new HashMap<>();

    public static void clear() { BOSS_LOCATIONS.clear(); }

    public static void put(Identifier entityTypeId, long locationId) {
        BOSS_LOCATIONS.put(entityTypeId, locationId);
    }

    public static Long getLocationId(EntityType<?> type) {
        return BOSS_LOCATIONS.get(Registries.ENTITY_TYPE.getId(type));
    }

    public static int size() { return BOSS_LOCATIONS.size(); }

    private BossKillLocationRegistry() {}
}
