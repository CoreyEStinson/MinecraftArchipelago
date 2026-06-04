package com.minecraftarchipelago.facades;

import com.minecraftarchipelago.APClient;
import io.github.archipelagomw.ClientStatus;

import java.util.ArrayList;
import java.util.List;

public final class DefaultArchipelagoClientFacade implements ArchipelagoClientFacade {
    private final APClient client;

    public DefaultArchipelagoClientFacade(APClient client) {
        this.client = client;
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public String getConnectedAddress() {
        return client.getConnectedAddress();
    }

    @Override
    public void checkLocation(long locationId) {
        client.checkLocation(locationId);
    }

    @Override
    public void scoutLocations(List<Long> locations) {
        client.scoutLocations(new ArrayList<>(locations));
    }

    @Override
    public void sendDeathLink(String slotName, String cause) {
        client.sendDeathlink(slotName, cause);
    }

    @Override
    public void setGameState(ClientStatus status) {
        client.setGameState(status);
    }

    @Override
    public void setDeathLinkEnabled(boolean enabled) {
        client.setDeathLinkEnabled(enabled);
    }
}
