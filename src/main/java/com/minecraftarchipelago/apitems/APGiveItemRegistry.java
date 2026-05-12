package com.minecraftarchipelago.apitems;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class APGiveItemRegistry {

    public record GiveEntry(Identifier itemId, int count) {}

    private static final Map<Long, GiveEntry> GIVE_ITEMS = new HashMap<>();

    public static void clear() { GIVE_ITEMS.clear(); }

    public static void put(long apItemId, Identifier itemId, int count) {
        GIVE_ITEMS.put(apItemId, new GiveEntry(itemId, count));
    }

    public static GiveEntry getEntry(long apItemId){
        return GIVE_ITEMS.get(apItemId);
    }

    public static boolean isGiveItem(long apItemId){
        return GIVE_ITEMS.containsKey(apItemId);
    }

    public static int size() { return GIVE_ITEMS.size(); }
}
