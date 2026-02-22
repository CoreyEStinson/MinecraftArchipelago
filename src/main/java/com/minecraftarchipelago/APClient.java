package com.minecraftarchipelago;

import io.github.archipelagomw.Client;

public class APClient extends Client
{
    @Override
    public void onError(Exception ex) {
        // called when the websocket/client hits an error
        ex.printStackTrace();
    }

    @Override
    public void onClose(String reason, int attemptingReconnect) {
        System.out.println("AP connection closed: " + reason);
    }
}
