package com.minecraftarchipelago.apstages;

import com.minecraftarchipelago.apstages.model.PackageDef;
import com.minecraftarchipelago.apstages.model.StageDef;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StageRegistryTest {

    @Test
    void testStageRegistryStaticMethods() {
        // Test that StageRegistry static methods work
        assertEquals(0, StageRegistry.stageCount());
        assertEquals(0, StageRegistry.packageCount());
        assertEquals(0, StageRegistry.stageIds().size());
        assertEquals(0, StageRegistry.packageIds().size());
        
        // Test putting and getting stages
        Identifier stageId = Identifier.of("test:stage1");
        StageDef stageDef = new StageDef(
            List.of("check1", "check2"),
            null,
            null,
            Map.of(),
            List.of()
        );
        StageRegistry.putStage(stageId, stageDef);
        assertEquals(1, StageRegistry.stageCount());
        assertEquals(stageDef, StageRegistry.getStage(stageId));
        
        // Test putting and getting packages
        Identifier packageId = Identifier.of("test:package1");
        PackageDef packageDef = new PackageDef(List.of());
        StageRegistry.putPackage(packageId, packageDef);
        assertEquals(1, StageRegistry.packageCount());
        assertEquals(packageDef, StageRegistry.getPackage(packageId));
        
        // Test clearing
        StageRegistry.clear();
        assertEquals(0, StageRegistry.stageCount());
        assertEquals(0, StageRegistry.packageCount());
        assertNull(StageRegistry.getStage(stageId));
        assertNull(StageRegistry.getPackage(packageId));
    }
}