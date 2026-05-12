package com.minecraftarchipelago.apitems;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class APItemRegistry {
    private static final Map<Long, Identifier> ITEM_TO_STAGE = new HashMap<>();
    private static final Map<Long, List<Identifier>> PROGRESSSIVE_ITEMS = new HashMap<>();

    public static void clear(){
        ITEM_TO_STAGE.clear();
        PROGRESSSIVE_ITEMS.clear();
    }

    public static void put(long apItemID, Identifier stageId){
        ITEM_TO_STAGE.put(apItemID, stageId);
    }

    public static void putProgressive(long apItemId, List<Identifier> stageId) {
        PROGRESSSIVE_ITEMS.put(apItemId, stageId);
    }

    // Returns null if this AP item doesn't map to a stage
    public static Identifier getStageId(long apItemId){
        return ITEM_TO_STAGE.get(apItemId);
    }

    public static boolean isProgressive(long apItemId){
        return PROGRESSSIVE_ITEMS.containsKey(apItemId);
    }

    // Returns the next tier stage the player hasn't unlocked yet.
    // Returns null if all tiers are already unlocked
    public static Identifier getNextTier(long apItemId, Set<Identifier> alreadyUnlocked){
        List<Identifier> tiers = PROGRESSSIVE_ITEMS.get(apItemId);
        if (tiers == null) return null;
        for (Identifier stage : tiers) {
            if (!alreadyUnlocked.contains(stage)) return stage;
        }
        return null;
    }

    public static int size(){
        return ITEM_TO_STAGE.size() + PROGRESSSIVE_ITEMS.size();
    }

    private APItemRegistry() {}
}
