package com.minecraftarchipelago.apitems;

import com.google.gson.*;
import com.minecraftarchipelago.MinecraftArchipelago;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class APItemsReloadListener implements SimpleSynchronousResourceReloadListener {

    private static final Identifier LISTENER_ID =
            Identifier.of("minecraftarchipelago", "apitems_reload");

    @Override
    public Identifier getFabricId() { return LISTENER_ID; }

    @Override
    public void reload(ResourceManager manager) {
        APItemRegistry.clear();
        APGiveItemRegistry.clear();

        loadStages(manager);
        loadProgressives(manager);
        loadGiveItems(manager);

        MinecraftArchipelago.LOGGER.info(
                "[AP] Loaded {} stage items, {} give items.",
                APItemRegistry.size(),
                APGiveItemRegistry.size());
    }

    // Loads apitems/stages.json — maps ap_item_id → stage identifier
    private static void loadStages(ResourceManager manager) {
        load(manager, "apitems/stages.json", root -> {
            for (JsonElement el : root.getAsJsonArray("items")) {
                JsonObject obj = el.getAsJsonObject();
                long id = obj.get("ap_item_id").getAsLong();
                Identifier stageId = Identifier.tryParse(obj.get("stage").getAsString());
                if (stageId != null) APItemRegistry.put(id, stageId);
            }
        });
    }

    // Loads apitems/progressive.json — maps ap_item_id → ordered list of tier stages
    private static void loadProgressives(ResourceManager manager) {
        load(manager, "apitems/progressive.json", root -> {
            for (JsonElement el : root.getAsJsonArray("progressive_items")) {
                JsonObject obj = el.getAsJsonObject();
                long id = obj.get("ap_item_id").getAsLong();

                List<Identifier> tiers = new ArrayList<>();
                for (JsonElement tier : obj.getAsJsonArray("stages")) {
                    Identifier stageId = Identifier.tryParse(tier.getAsString());
                    if (stageId != null) tiers.add(stageId);
                }
                if (!tiers.isEmpty()) APItemRegistry.putProgressive(id, tiers);
            }
        });
    }

    // Loads apitems/give.json — maps ap_item_id → minecraft item + count (filler items)
    private static void loadGiveItems(ResourceManager manager) {
        load(manager, "apitems/give.json", root -> {
            for (JsonElement el : root.getAsJsonArray("items")) {
                JsonObject obj = el.getAsJsonObject();
                long id = obj.get("ap_item_id").getAsLong();
                Identifier itemId = Identifier.tryParse(obj.get("minecraft_item").getAsString());
                int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
                if (itemId != null && count > 0) APGiveItemRegistry.put(id, itemId, count);
            }
        });
    }

    // Helper — finds and parses a single named JSON file from resources
    private static void load(ResourceManager manager, String path, JsonConsumer consumer) {
        var found = manager.findResources(
                path.substring(0, path.lastIndexOf('/')),
                id -> id.getPath().equals(path)
        );
        for (var entry : found.entrySet()) {
            try (var reader = new InputStreamReader(
                    entry.getValue().getInputStream(), StandardCharsets.UTF_8)) {
                consumer.accept(JsonParser.parseReader(reader).getAsJsonObject());
            } catch (Exception e) {
                MinecraftArchipelago.LOGGER.error("[AP] Failed to load {}: {}", path, e.getMessage());
            }
        }
    }

    @FunctionalInterface
    interface JsonConsumer {
        void accept(JsonObject root) throws Exception;
    }
}