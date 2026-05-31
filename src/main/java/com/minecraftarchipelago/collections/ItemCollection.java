package com.minecraftarchipelago.collections;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines one collectible set. "Complete" means every item in requiredItems
 * has been in the player's inventory at least once in this world.
 */
public record ItemCollection(
        String id,
        String displayName,
        List<Identifier> requiredItems
) {
    public int countCollected(Set<Identifier> everHeld) {
        return (int) requiredItems.stream().filter(everHeld::contains).count();
    }

    public boolean isComplete(Set<Identifier> everHeld) {
        return everHeld.containsAll(requiredItems);
    }

    /**
     * Returns sorted display names of items not yet collected
     */
    public List<String> getUncollectedNames(Set<Identifier> everHeld) {
        return requiredItems.stream()
                .filter(id -> !everHeld.contains(id))
                .map(ItemCollection::formatId)
                .sorted()
                .collect(Collectors.toList());
    }

    private static String formatId(Identifier id) {
        String[] parts = id.getPath().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    public int total() {
        return requiredItems.size();
    }
}
