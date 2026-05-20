package com.minecraftarchipelago.apstages;

import net.minecraft.util.Identifier;

import java.util.Set;

public record BlockStageRules(
        Set<Identifier> lockBlockIds,
        Set<Identifier> lockBlockTags,
        Set<Identifier> unlockBlockIds,
        Set<Identifier> unlockBlockTags
) {
    public static final BlockStageRules EMPTY =
            new BlockStageRules(Set.of(), Set.of(), Set.of(), Set.of());
}
