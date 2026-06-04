package com.minecraftarchipelago.facades;

import io.github.archipelagomw.ClientStatus;

import java.util.List;

public interface ArchipelagoClientFacade {
    boolean isConnected();

    String getConnectedAddress();

    void checkLocation(long locationId);

    void scoutLocations(List<Long> locations);

    void sendDeathLink(String slotName, String cause);

    void setGameState(ClientStatus status);

    void setDeathLinkEnabled(boolean enabled);
}
