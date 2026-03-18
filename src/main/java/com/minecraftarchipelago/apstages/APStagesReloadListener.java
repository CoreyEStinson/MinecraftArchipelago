package com.minecraftarchipelago.apstages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraftarchipelago.MinecraftArchipelago;
import com.minecraftarchipelago.apstages.model.ItemGrant;
import com.minecraftarchipelago.apstages.model.ItemStageRules;
import com.minecraftarchipelago.apstages.model.PackageDef;
import com.minecraftarchipelago.apstages.model.StageDef;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class APStagesReloadListener  implements SimpleSynchronousResourceReloadListener {
    private static final Identifier LISTENER_ID = Identifier.of("minecraftarchipelago", "apstages_reload");

    @Override
    public Identifier getFabricId(){
        return LISTENER_ID;
    }

    @Override
    public void reload(ResourceManager manager){
        StageRegistry.clear(); // Clears on reload to avoid duplicates

        loadPackages(manager);
        loadStages(manager);

        System.out.println("[AP] Loaded " + StageRegistry.packageCount() + " packages and "
            + StageRegistry.stageCount() + " stages.");
        MinecraftArchipelago.LOGGER.info("[AP] Loaded {} packages and {} stages.", StageRegistry.packageCount(), StageRegistry.stageCount());
    }

    private static void loadPackages(ResourceManager manager) {
        var found = manager.findResources("apstages/packages", id -> id.getPath().endsWith(".json"));

        for (var entry : found.entrySet()) {
            Identifier resId = entry.getKey();
            Identifier packId = Identifier.of(resId.getNamespace(),
                    resId.getPath()
                            .substring("apstages/packages/".length(), resId.getPath().length() - ".json".length())
            ); // becomes minecraftarchipelago:package_name

            Resource resource = entry.getValue();
            try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {

                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray items = root.getAsJsonArray("items");

                List<ItemGrant> grants = new ArrayList<>();
                for(JsonElement el : items){
                    JsonObject obj = el.getAsJsonObject();
                    Identifier itemId = safeId(obj.get("id").getAsString());
                    if (itemId == null) continue;;

                    int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
                    if (count <= 0) continue;

                    grants.add(new ItemGrant(itemId, count));
                }

                StageRegistry.putPackage(packId, new PackageDef(grants));
            } catch (Exception e) {
                System.out.println("[AP] Failed to load package " + resId + ": " + e.getMessage());
                MinecraftArchipelago.LOGGER.error("[AP] Failed to load package {}: {}", resId, e.getMessage());
            }


        }
    }
    private  static void loadStages(ResourceManager manager){
        var found = manager.findResources("apstages/stages", id -> id.getPath().endsWith(".json"));

        for (var entry : found.entrySet()){
            Identifier resId = entry.getKey();
            Identifier stageId= Identifier.of(resId.getNamespace(),
                resId.getPath()
                    .substring("apstages/stages/".length(), resId.getPath().length() - ".json".length())
            ); // becomes minecraftarchipelago:stage_name

            var resource = entry.getValue();
            try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                // requires.checks
                List<String> requiredChecks = new ArrayList<>();
                if (root.has("requires")){
                    JsonObject requires = root.getAsJsonObject("requires");
                    if (requires.has("checks")){
                        for (JsonElement el : requires.getAsJsonArray("checks")){
                            requiredChecks.add(el.getAsString());
                        }
                    }
                }

                // items.lcck/unlock
                Set<Identifier> lockItemIds = new HashSet<>();
                Set<Identifier> lockItemTags = new HashSet<>();
                Set<Identifier> unlockItemIds = new HashSet<>();
                Set<Identifier> unlockItemTags = new HashSet<>();
                
                if (root.has("items")){
                    JsonObject items = root.getAsJsonObject("items");
                    
                    if (items.has("lock")){
                        parseItemRefs(items.getAsJsonArray("lock"), lockItemIds, lockItemTags);
                    }
                    if (items.has("unlock")){
                        parseItemRefs(items.getAsJsonArray("unlock"), unlockItemIds, unlockItemTags);
                    }
                }
                
                ItemStageRules itemRules = new ItemStageRules(
                    lockItemIds,
                    lockItemTags,
                    unlockItemIds,
                    unlockItemTags
                );

                // world.gamerules
                Map<String, String> gamerules = new HashMap<>();
                if (root.has("world")){
                    JsonObject world = root.getAsJsonObject("world");
                    if (world.has("gamerules")){
                        JsonObject gr = world.getAsJsonObject("gamerules");
                        for (var e : gr.entrySet()){
                            gamerules.put(e.getKey(), e.getValue().getAsString());
                        }
                    }
                }

                // grants.packages
                List<Identifier> packages = new ArrayList<>();
                if (root.has("grants")){
                    JsonObject grants = root.getAsJsonObject("grants");
                    if (grants.has("packages")){
                        for (JsonElement el : grants.getAsJsonArray("packages")){
                            Identifier pid = safeId(el.getAsString());
                            if (pid != null) packages.add(pid);
                        }
                    }
                }

                StageRegistry.putStage(stageId, new StageDef(
                        requiredChecks,
                        itemRules,
                        gamerules,
                        packages
                ));
            } catch (Exception e){
                System.out.println("[AP] Failed to load stage " + resId + ": " + e.getMessage());
                MinecraftArchipelago.LOGGER.error("[AP] Failed to load stage {}: {}", resId, e.getMessage());
            }
        }
    }

    @Nullable
    private static Identifier safeId(String raw){
        Identifier id = Identifier.tryParse(raw); // returns null if invalid

        if (id == null){
            System.out.println("[AP] Bad identifier in JSON: " + raw);
            MinecraftArchipelago.LOGGER.error("[AP] Bad identifier in JSON: {}", raw);
        }
        return  id;
    }
    
    private static void parseItemRefs(JsonArray array, Set<Identifier> itemIds, Set<Identifier> tagIds){
        for (JsonElement el : array){
            String raw = el.getAsString();
            boolean isTag = raw.startsWith("#");
            String cleaned = isTag ? raw.substring(1) : raw;
            
            Identifier id = Identifier.tryParse(cleaned);
            if (id == null) continue;
            
            if (isTag)
                tagIds.add(id);
            else
                itemIds.add(id);
        }
    }
}
