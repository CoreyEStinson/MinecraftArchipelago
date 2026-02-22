package com.minecraftarchipelago;


import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.LocationInfoEvent;
import io.github.archipelagomw.events.PrintJSONEvent;
import io.github.archipelagomw.events.RetrievedEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class APEvents {
    @ArchipelagoEventListener
    public void onLocationInfo(LocationInfoEvent e) {
        System.out.println("Got location info: " + e);
    }

    @ArchipelagoEventListener
    public void onRetrieved(RetrievedEvent e) {
        System.out.println("Datastorage retrieved: " + e);
    }

    @ArchipelagoEventListener
    public void onPrint(PrintJSONEvent e){
        String msg = e.apPrint.getPlainText();
        MinecraftClient.getInstance().execute(() ->{
            var player = MinecraftClient.getInstance().player;
            if (player != null) player.sendMessage(Text.literal("[AP] " + msg));
        });
    }


}