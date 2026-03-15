package com.minecraftarchipelago.apstages.model;

import net.minecraft.util.Identifier;

import java.util.Set;

public record ItemStageRules(
    Set<Identifier> lockItemIds,
    Set<Identifier> lockItemTags,
    Set<Identifier> unlockItemIds,
    Set<Identifier> unlockItemTags
) {}