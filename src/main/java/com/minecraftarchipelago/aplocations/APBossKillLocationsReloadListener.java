package com.minecraftarchipelago.aplocations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraftarchipelago.MinecraftArchipelago;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class APBossKillLocationsReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier LISTENER_ID =
            Identifier.of("minecraftarchipelago", "boss_kills_reload");

    @Override
    public Identifier getFabricId() { return LISTENER_ID; }

    @Override
    public void reload(ResourceManager manager) {
        BossKillLocationRegistry.clear();

        var found = manager.findResources("aplocations",
                id -> id.getPath().equals("aplocations/boss_kills.json"));

        for (var entry : found.entrySet()) {
            try (var reader = new InputStreamReader(
                    entry.getValue().getInputStream(), StandardCharsets.UTF_8)) {

                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                for (JsonElement el: root.getAsJsonArray("boss_kills")) {
                    JsonObject obj = el.getAsJsonObject();
                    Identifier entityId = Identifier.tryParse(obj.get("entity").getAsString());
                    long locationId = obj.get("ap_location_id").getAsLong();
                    if (entityId != null) BossKillLocationRegistry.put(entityId, locationId);
                }
                MinecraftArchipelago.LOGGER.info(
                        "[AP] Loaded {} boss kill locations.", BossKillLocationRegistry.size()
                );
            } catch (Exception e) {
                MinecraftArchipelago.LOGGER.error("[AP] Failed to load boss_kills.json: {}", e.getMessage());
            }
        }
    }
}
