package com.minecraftarchipelago.apitems;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APProgressiveGiftRegistry {
    private static final Map<Long, List<Identifier>> GIFT_TIERS = new HashMap<>();

    public static void clear() {
        GIFT_TIERS.clear();
    }

    public static void put(long apItemId, List<Identifier> packageIds) {
        GIFT_TIERS.put(apItemId, List.copyOf(packageIds));
    }

    public static boolean isProgressiveGift(long apItemId) {
        return GIFT_TIERS.containsKey(apItemId);
    }

    public static int tierCount(long apItemId) {
        List<Identifier> tiers = GIFT_TIERS.get(apItemId);
        return tiers == null ? 0 : tiers.size();
    }

    @Nullable
    public static Identifier getPackageForTier(long apItemId, int tierIndex) {
        List<Identifier> tiers = GIFT_TIERS.get((apItemId));
        if (tiers == null || tierIndex < 0 || tierIndex >= tiers.size()) {
            return null;
        }

        return tiers.get(tierIndex);
    }

    private APProgressiveGiftRegistry() {}
}
