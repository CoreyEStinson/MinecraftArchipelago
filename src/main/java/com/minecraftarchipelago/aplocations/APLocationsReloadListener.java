package com.minecraftarchipelago.aplocations;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraftarchipelago.MinecraftArchipelago;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class APLocationsReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final Identifier LISTENER_ID =
            Identifier.of("minecraftarchipelago", "aplocations_reload");

    @Override
    public Identifier getFabricId() {
        return LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        LocationRegistry.clear();

        Map<Identifier, Resource> found = manager.findResources(
                "aplocations",
                id -> id.getPath().endsWith(".json")
        );

        for (var entry : found.entrySet()) {
            try (var reader = new InputStreamReader(
                    entry.getValue().getInputStream(), StandardCharsets.UTF_8)) {

                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray locations = root.getAsJsonArray("locations");

                for (JsonElement el : locations) {
                    JsonObject loc = el.getAsJsonObject();

                    String rawId = loc.get("advancement").getAsString();
                    long locationId = loc.get("location_id").getAsLong();

                    Identifier advId = Identifier.tryParse(rawId);
                    if (advId == null) {
                        MinecraftArchipelago.LOGGER.warn(
                                "[AP] Bad advancement id in locations file: {}", rawId);
                        continue;
                    }

                    LocationRegistry.put(advId, locationId);
                }
            } catch (Exception e) {
                MinecraftArchipelago.LOGGER.error(
                        "[AP] Failed to load locations file {}: {}", entry.getKey(), e.getMessage());
            }

               }

        MinecraftArchipelago.LOGGER.info(
                "[AP] Loaded {} advancement locations.", LocationRegistry.size());
    }
}
