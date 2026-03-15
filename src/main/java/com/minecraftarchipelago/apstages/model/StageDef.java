package com.minecraftarchipelago.apstages.model;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record StageDef (
    List<String> requiredChecks,    // archipelago checks
    ItemStageRules itemRules,
    Map<String, String> gamerules,  // ex. "keepInventory" -> true
    List<Identifier> grantPackages  // ex. minecraftarchipelago:starter_tools
){}
