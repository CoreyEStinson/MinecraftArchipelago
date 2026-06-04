package com.minecraftarchipelago;

import com.minecraftarchipelago.facades.ArchipelagoClientFacade;
import com.minecraftarchipelago.facades.MinecraftRuntimeFacade;
import io.github.archipelagomw.ClientStatus;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class IntegrationTestSupport {
    static final Path MAIN_RESOURCES = Path.of("src", "main", "resources");
    static final Path MAIN_DATA = MAIN_RESOURCES.resolve(Path.of("data", "minecraftarchipelago"));

    static void loadCoreData() throws IOException {
        ResourceManager manager = resourceManagerFromMainData();
        new com.minecraftarchipelago.apstages.APStagesReloadListener().reload(manager);
        new com.minecraftarchipelago.apitems.APItemsReloadListener().reload(manager);
        new com.minecraftarchipelago.aplocations.APLocationsReloadListener().reload(manager);
        new com.minecraftarchipelago.aplocations.APBossKillLocationsReloadListener().reload(manager);
    }

    static Path mainResourcePath(String relative) {
        return MAIN_RESOURCES.resolve(relative);
    }

    static ResourceManager resourceManagerFromMainData() throws IOException {
        Map<Identifier, Path> resources = new LinkedHashMap<>();
        try (Stream<Path> stream = Files.walk(MAIN_DATA)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String relative = MAIN_DATA.relativize(path).toString().replace('\\', '/');
                resources.put(Identifier.of("minecraftarchipelago", relative), path);
            });
        }

        ResourceManager manager = mock(ResourceManager.class);
        when(manager.findResources(anyString(), any())).thenAnswer(invocation -> {
            String start = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            Predicate<Identifier> filter = invocation.getArgument(1);
            Map<Identifier, Resource> found = new LinkedHashMap<>();

            for (Map.Entry<Identifier, Path> entry : resources.entrySet()) {
                Identifier id = entry.getKey();
                if (!id.getPath().startsWith(start) || !filter.test(id)) {
                    continue;
                }

                Resource resource = mock(Resource.class);
                when(resource.getInputStream()).thenAnswer(ignored -> Files.newInputStream(entry.getValue()));
                found.put(id, resource);
            }

            return found;
        });

        return manager;
    }

    static final class RecordingClientFacade implements ArchipelagoClientFacade {
        boolean connected = true;
        String address = "integration";
        boolean deathLinkEnabled = false;
        final List<Long> checkedLocations = new ArrayList<>();
        final List<List<Long>> scoutRequests = new ArrayList<>();
        final List<String> sentDeathLinks = new ArrayList<>();
        final List<ClientStatus> gameStates = new ArrayList<>();

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public String getConnectedAddress() {
            return address;
        }

        @Override
        public void checkLocation(long locationId) {
            checkedLocations.add(locationId);
        }

        @Override
        public void scoutLocations(List<Long> locations) {
            scoutRequests.add(new ArrayList<>(locations));
        }

        @Override
        public void sendDeathLink(String slotName, String cause) {
            sentDeathLinks.add(slotName + "|" + cause);
        }

        @Override
        public void setGameState(ClientStatus status) {
            gameStates.add(status);
        }

        @Override
        public void setDeathLinkEnabled(boolean enabled) {
            deathLinkEnabled = enabled;
        }
    }

    static final class ImmediateRuntimeFacade implements MinecraftRuntimeFacade {
        private final MinecraftServer server;
        private final PlayerEntity player;

        ImmediateRuntimeFacade(MinecraftServer server, PlayerEntity player) {
            this.server = server;
            this.player = player;
        }

        @Override
        public void executeOnClient(Runnable action) {
            action.run();
        }

        @Override
        public MinecraftServer getCurrentServer() {
            return server;
        }

        @Override
        public PlayerEntity getCurrentPlayer() {
            return player;
        }
    }

    private IntegrationTestSupport() {}
}
