package com.minecraftarchipelago;

import com.minecraftarchipelago.apitems.APItemRegistry;
import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import com.minecraftarchipelago.apstages.StageRegistry;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class APItemFlowIntegrationTest {

    @BeforeEach
    void setUp() throws Exception {
        APSession.resetForTests();
        IntegrationTestSupport.loadCoreData();
    }

    @AfterEach
    void tearDown() {
        APSession.resetForTests();
        APItemRegistry.clear();
        com.minecraftarchipelago.apitems.APGiveItemRegistry.clear();
        StageRegistry.clear();
    }

    @Test
    void giveItemsResolveToInventoryGrantsWithoutStageUnlocks() {
        MinecraftServer server = mock(MinecraftServer.class);
        CheckedLocationsState checkedState = mock(CheckedLocationsState.class);

        when(checkedState.isNewItem(7)).thenReturn(true);

        try (MockedStatic<CheckedLocationsState> checkedStateMock = mockStatic(CheckedLocationsState.class)) {
            checkedStateMock.when(() -> CheckedLocationsState.get(server)).thenReturn(checkedState);

            APEvents.ReceivedItemDecision decision =
                    APEvents.decideReceivedItem(server, UUID.randomUUID(), 43051L, 7);

            assertFalse(decision.duplicate());
            assertEquals(Identifier.ofVanilla("beetroot"), decision.giveEntry().itemId());
            assertEquals(8, decision.giveEntry().count());
            assertNull(decision.stageId());
        }
    }

    @Test
    void standardUnlockItemsResolveToTheirMappedStage() {
        MinecraftServer server = mock(MinecraftServer.class);
        CheckedLocationsState checkedState = mock(CheckedLocationsState.class);
        Identifier bowStage = Identifier.of("minecraftarchipelago", "items/bow");

        when(checkedState.isNewItem(1)).thenReturn(true);

        try (MockedStatic<CheckedLocationsState> checkedStateMock = mockStatic(CheckedLocationsState.class)) {
            checkedStateMock.when(() -> CheckedLocationsState.get(server)).thenReturn(checkedState);

            APEvents.ReceivedItemDecision decision =
                    APEvents.decideReceivedItem(server, UUID.randomUUID(), 43018L, 1);

            assertFalse(decision.duplicate());
            assertNull(decision.giveEntry());
            assertEquals(bowStage, decision.stageId());
        }
    }

    @Test
    void progressiveItemsAdvanceOneTierPerReceiptAndStopAtMaxTier() {
        MinecraftServer server = mock(MinecraftServer.class);
        CheckedLocationsState checkedState = mock(CheckedLocationsState.class);
        StageUnlockState unlockState = mock(StageUnlockState.class);
        UUID playerId = UUID.randomUUID();
        Identifier stone = Identifier.of("minecraftarchipelago", "tools/stone_tools");
        Identifier iron = Identifier.of("minecraftarchipelago", "tools/iron_tools");
        Identifier diamond = Identifier.of("minecraftarchipelago", "tools/diamond_tools");
        Identifier netherite = Identifier.of("minecraftarchipelago", "tools/netherite_tools");
        var unlockedSnapshots = java.util.List.of(
                Set.<Identifier>of(),
                Set.of(stone),
                Set.of(stone, iron),
                Set.of(stone, iron, diamond),
                Set.of(stone, iron, diamond, netherite)
        );
        AtomicInteger snapshotIndex = new AtomicInteger();

        when(checkedState.isNewItem(anyInt())).thenReturn(true);
        when(unlockState.getUnlocked(playerId)).thenAnswer(invocation ->
                unlockedSnapshots.get(Math.min(snapshotIndex.getAndIncrement(), unlockedSnapshots.size() - 1)));

        try (MockedStatic<CheckedLocationsState> checkedStateMock = mockStatic(CheckedLocationsState.class);
             MockedStatic<StageUnlockState> unlockStateMock = mockStatic(StageUnlockState.class)) {
            checkedStateMock.when(() -> CheckedLocationsState.get(server)).thenReturn(checkedState);
            unlockStateMock.when(() -> StageUnlockState.get(server)).thenReturn(unlockState);

            assertEquals(stone, APEvents.decideReceivedItem(server, playerId, 43000L, 10).stageId());
            assertEquals(iron, APEvents.decideReceivedItem(server, playerId, 43000L, 11).stageId());
            assertEquals(diamond, APEvents.decideReceivedItem(server, playerId, 43000L, 12).stageId());
            assertEquals(netherite, APEvents.decideReceivedItem(server, playerId, 43000L, 13).stageId());
            assertNull(APEvents.decideReceivedItem(server, playerId, 43000L, 14).stageId());
        }
    }

    @Test
    void duplicateItemIndexesAreIgnoredBeforeAnyUnlockLogicRuns() {
        MinecraftServer server = mock(MinecraftServer.class);
        CheckedLocationsState checkedState = mock(CheckedLocationsState.class);

        when(checkedState.isNewItem(3)).thenReturn(false);

        try (MockedStatic<CheckedLocationsState> checkedStateMock = mockStatic(CheckedLocationsState.class);
             MockedStatic<StageUnlockState> unlockStateMock = mockStatic(StageUnlockState.class)) {
            checkedStateMock.when(() -> CheckedLocationsState.get(server)).thenReturn(checkedState);

            APEvents.ReceivedItemDecision decision =
                    APEvents.decideReceivedItem(server, UUID.randomUUID(), 43018L, 3);

            assertTrue(decision.duplicate());
            assertNull(decision.giveEntry());
            assertNull(decision.stageId());
            unlockStateMock.verifyNoInteractions();
        }
    }
}
