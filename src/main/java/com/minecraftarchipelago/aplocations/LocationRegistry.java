package com.minecraftarchipelago.aplocations;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class LocationRegistry {
    private static final Map<Identifier, Long> ADVANCEMENT_LOCATIONS = new HashMap<>();

    public static void clear() {
        ADVANCEMENT_LOCATIONS.clear();
    }

    public static void put(Identifier advancementId, long locationId) {
        ADVANCEMENT_LOCATIONS.put(advancementId, locationId);
    }

    public static Long getLocationId(Identifier advancementId) {
        return ADVANCEMENT_LOCATIONS.get(advancementId);
    }

    public static int size() {
        return ADVANCEMENT_LOCATIONS.size();
    }
}
