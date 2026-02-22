package com.minecraftarchipelago.apstages;

import com.minecraftarchipelago.apstages.model.PackageDef;
import com.minecraftarchipelago.apstages.model.StageDef;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StageRegistry {
    private static final Map<Identifier, StageDef> STAGES = new HashMap<>();
    private static final Map<Identifier, PackageDef> PACKAGES = new HashMap<>();

    public static void clear() {
        STAGES.clear();
        PACKAGES.clear();
    }

    public static void putStage(Identifier id, StageDef def) { STAGES.put(id, def); }
    public static void putPackage(Identifier id, PackageDef def) { PACKAGES.put(id, def); }

    public static StageDef getStage(Identifier id) { return STAGES.get(id); }
    public static PackageDef getPackage(Identifier id) { return PACKAGES.get(id); }

    public static int stageCount() { return STAGES.size(); }
    public static int packageCount() { return PACKAGES.size(); }

    public static List<Identifier> stageIds() { return new ArrayList<>(STAGES.keySet()); }
    public static List<Identifier> packageIds() { return new ArrayList<>(PACKAGES.keySet()); }

    private StageRegistry() {}
}
