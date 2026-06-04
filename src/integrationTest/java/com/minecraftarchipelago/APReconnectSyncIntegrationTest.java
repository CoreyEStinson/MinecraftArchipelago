package com.minecraftarchipelago;

import com.minecraftarchipelago.aplocations.CheckedLocationsState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerAdvancementLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Set;

import static io.github.archipelagomw.ClientStatus.CLIENT_GOAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class APReconnectSyncIntegrationTest {

    @AfterEach
    void tearDown() {
        APSession.resetForTests();
    }

    @Test
    void reconnectResendsCheckedLocationsAndGoalStateOnce() {
        MinecraftServer server = mock(MinecraftServer.class);
        PlayerManager playerManager = mock(PlayerManager.class);
        ServerAdvancementLoader advancementLoader = mock(ServerAdvancementLoader.class);
        CheckedLocationsState state = mock(CheckedLocationsState.class);
        IntegrationTestSupport.RecordingClientFacade client = new IntegrationTestSupport.RecordingClientFacade();

        APSession.setClientFacadeForTests(client);
        APSession.setRuntimeFacadeForTests(new IntegrationTestSupport.ImmediateRuntimeFacade(null, null));

        when(server.getPlayerManager()).thenReturn(playerManager);
        when(playerManager.getPlayerList()).thenReturn(List.of());
        when(server.getAdvancementLoader()).thenReturn(advancementLoader);
        when(advancementLoader.getAdvancements()).thenReturn(List.of());
        when(state.getAllChecked()).thenReturn(Set.of(42500L, 42501L));
        when(state.isGoalAchieved()).thenReturn(true);

        try (MockedStatic<CheckedLocationsState> checkedStateMock = mockStatic(CheckedLocationsState.class)) {
            checkedStateMock.when(() -> CheckedLocationsState.get(server)).thenReturn(state);

            APEvents.resendCheckedLocationsOnConnect(server);

            assertEquals(Set.of(42500L, 42501L), Set.copyOf(client.checkedLocations));
            assertEquals(List.of(CLIENT_GOAL), client.gameStates);
        }
    }

    @Test
    void reconnectWithNoCheckedLocationsSendsNothing() {
        MinecraftServer server = mock(MinecraftServer.class);
        PlayerManager playerManager = mock(PlayerManager.class);
        ServerAdvancementLoader advancementLoader = mock(ServerAdvancementLoader.class);
        CheckedLocationsState state = mock(CheckedLocationsState.class);
        IntegrationTestSupport.RecordingClientFacade client = new IntegrationTestSupport.RecordingClientFacade();

        APSession.setClientFacadeForTests(client);
        APSession.setRuntimeFacadeForTests(new IntegrationTestSupport.ImmediateRuntimeFacade(null, null));

        when(server.getPlayerManager()).thenReturn(playerManager);
        when(playerManager.getPlayerList()).thenReturn(List.of());
        when(server.getAdvancementLoader()).thenReturn(advancementLoader);
        when(advancementLoader.getAdvancements()).thenReturn(List.of());
        when(state.getAllChecked()).thenReturn(Set.of());
        when(state.isGoalAchieved()).thenReturn(false);

        try (MockedStatic<CheckedLocationsState> checkedStateMock = mockStatic(CheckedLocationsState.class)) {
            checkedStateMock.when(() -> CheckedLocationsState.get(server)).thenReturn(state);

            APEvents.resendCheckedLocationsOnConnect(server);

            assertTrue(client.checkedLocations.isEmpty());
            assertTrue(client.gameStates.isEmpty());
        }
    }
}
