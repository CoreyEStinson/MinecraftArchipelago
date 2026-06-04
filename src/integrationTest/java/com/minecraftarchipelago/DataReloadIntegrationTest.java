package com.minecraftarchipelago;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecraftarchipelago.apitems.APGiveItemRegistry;
import com.minecraftarchipelago.apitems.APItemRegistry;
import com.minecraftarchipelago.aplocations.BossKillLocationRegistry;
import com.minecraftarchipelago.aplocations.LocationRegistry;
import com.minecraftarchipelago.apstages.StageRegistry;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DataReloadIntegrationTest {

    @AfterEach
    void tearDown() {
        APSession.resetForTests();
        APItemRegistry.clear();
        APGiveItemRegistry.clear();
        StageRegistry.clear();
        LocationRegistry.clear();
        BossKillLocationRegistry.clear();
    }

    @Test
    void reloadListenersLoadCoherentProgressionData() throws Exception {
        IntegrationTestSupport.loadCoreData();

        assertTrue(StageRegistry.stageCount() > 0);
        assertTrue(StageRegistry.packageCount() > 0);
        assertTrue(APItemRegistry.size() > 0);
        assertTrue(APGiveItemRegistry.size() > 0);
        assertTrue(LocationRegistry.size() > 0);
        assertEquals(4, BossKillLocationRegistry.size());

        assertNotNull(StageRegistry.getStage(Identifier.of("minecraftarchipelago", "base_rules")));
        JsonObject stagedItems = parse("data/minecraftarchipelago/apitems/stages.json");
        for (JsonElement element : stagedItems.getAsJsonArray("items")) {
            JsonObject item = element.getAsJsonObject();
            Identifier stageId = Identifier.tryParse(item.get("stage").getAsString());
            assertNotNull(StageRegistry.getStage(stageId), "Missing stage for AP item " + item);
        }

        JsonObject progressiveItems = parse("data/minecraftarchipelago/apitems/progressive.json");
        for (JsonElement element : progressiveItems.getAsJsonArray("progressive_items")) {
            for (JsonElement tier : element.getAsJsonObject().getAsJsonArray("stages")) {
                Identifier stageId = Identifier.tryParse(tier.getAsString());
                assertNotNull(StageRegistry.getStage(stageId), "Missing progressive stage " + stageId);
            }
        }

        Path stagesRoot = IntegrationTestSupport.mainResourcePath("data/minecraftarchipelago/apstages/stages");
        Files.walk(stagesRoot)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    JsonObject root = uncheckedParse(path);
                    if (!root.has("grants")) return;

                    JsonObject grants = root.getAsJsonObject("grants");
                    if (!grants.has("packages")) return;

                    for (JsonElement pkg : grants.getAsJsonArray("packages")) {
                        Identifier packageId = Identifier.tryParse(pkg.getAsString());
                        assertNotNull(StageRegistry.getPackage(packageId), "Missing package " + packageId);
                    }
                });
    }

    private static JsonObject parse(String relativePath) throws IOException {
        return uncheckedParse(IntegrationTestSupport.mainResourcePath(relativePath));
    }

    private static JsonObject uncheckedParse(Path path) {
        try {
            return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
