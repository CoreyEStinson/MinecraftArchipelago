package com.minecraftarchipelago.apitems;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class APItemRegistry {
    private static final Map<Long, Identifier> ITEM_TO_STAGE = new HashMap<>();

    public static void clear(){
        ITEM_TO_STAGE.clear();
    }

    public static void put(long apItemID, Identifier stageId){
        ITEM_TO_STAGE.put(apItemID, stageId);
    }

    // Returns null if this AP item doesn't map to a stage
    public static Identifier getStageId(long apItemId){
        return ITEM_TO_STAGE.get(apItemId);
    }

    public static int size(){
        return ITEM_TO_STAGE.size();
    }

    private APItemRegistry() {}
}
