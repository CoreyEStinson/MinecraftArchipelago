package com.minecraftarchipelago.apitems;

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

public class APItemsReloadListener implements SimpleSynchronousResourceReloadListener {
    private static final Identifier LISTNER_ID =
            Identifier.of("minecraftarchipelago", "apitems_reload");

    @Override
    public Identifier getFabricId(){
        return LISTNER_ID;
    }

    @Override
    public void reload(ResourceManager manager){
        APItemRegistry.clear();

        Map<Identifier, Resource> found = manager.findResources(
                "apitems",
                id -> id.getPath().endsWith(".json")
        );

        for (var entry : found.entrySet()){
            try (var reader = new InputStreamReader(
                    entry.getValue().getInputStream(), StandardCharsets.UTF_8)){

                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray items = root.getAsJsonArray("items");

                for (JsonElement element : items){
                    JsonObject obj = element.getAsJsonObject();

                    long apItemId = obj.get("ap_item_id").getAsLong();
                    String rawStage = obj.get("stage").getAsString();

                    Identifier stageId = Identifier.tryParse(rawStage);
                    if (stageId == null){
                        MinecraftArchipelago.LOGGER.warn(
                                "[AP] Bad stage identifier in items file: {}", rawStage);
                        continue;
                    }

                    APItemRegistry.put(apItemId, stageId);
                }
            } catch (Exception e){
                MinecraftArchipelago.LOGGER.error(
                        "[AP] Failed to load items file {}: {}", entry.getKey(), e.getMessage());
            }
        }

        MinecraftArchipelago.LOGGER.info(
                "[AP] Loaded {} AP item -> stage mappings.", APItemRegistry.size());
    }
}
