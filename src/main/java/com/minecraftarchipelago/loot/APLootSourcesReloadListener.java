package com.minecraftarchipelago.loot;

import com.google.gson.JsonParser;
import com.minecraftarchipelago.MinecraftArchipelago;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class APLootSourcesReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final Identifier LISTENER_ID =
            Identifier.of("minecraftarchipelago", "aploot_sources_reload");

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        APLootSourceRegistry.clear();

        var found = manager.findResources(
                "aploot_sources",
                id -> id.getPath().equals("aploot_sources/sources.json"));

        for (var entry : found.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().getInputStream(), StandardCharsets.UTF_8)) {
                APLootSourceRegistry.replaceAll(
                        APLootSource.parseAll(JsonParser.parseReader(reader).getAsJsonObject()));
            } catch (Exception e) {
                MinecraftArchipelago.LOGGER.error(
                        "[AP] Failed to load loot sources file {}: {}", entry.getKey(), e.getMessage());
            }
        }

        MinecraftArchipelago.LOGGER.info("[AP] Loaded {} AP loot source groups.", APLootSourceRegistry.getAll().size());
    }
}
