package com.minecraftarchipelago.facades;

import io.github.archipelagomw.ClientStatus;

import java.util.List;

public final class NoOpArchipelagoClientFacade implements ArchipelagoClientFacade {
    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public String getConnectedAddress() {
        return null;
    }

    @Override
    public void checkLocation(long locationId) {
    }

    @Override
    public void scoutLocations(List<Long> locations) {
    }

    @Override
    public void sendDeathLink(String slotName, String cause) {
    }

    @Override
    public void setGameState(ClientStatus status) {
    }

    @Override
    public void setDeathLinkEnabled(boolean enabled) {
    }
}
