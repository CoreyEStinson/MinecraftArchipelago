package com.minecraftarchipelago.loot;

import java.util.HashMap;
import java.util.Map;

public class LootableItemNameCache {

    private static final Map<Long, String[]> CACHE = new HashMap<>();

    public static void put(long locationId, String itemName, String playerName) {
        CACHE.put(locationId, new String[]{itemName, playerName});
    }

    public static String[] get(long locationId){
        return CACHE.get(locationId);
    }

    public static boolean has(long locationId) {
        return CACHE.containsKey(locationId);
    }

    public static void clear() {
        CACHE.clear();
    }

    private LootableItemNameCache() {}
}
