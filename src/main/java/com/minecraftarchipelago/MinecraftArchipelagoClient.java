package com.minecraftarchipelago;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

public class MinecraftArchipelagoClient implements ClientModInitializer
{
    public static final String MOD_ID = "minecraftarchipelago";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient()
    {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("archipelago")
                    .then(ClientCommandManager.literal("join")
                        .then(ClientCommandManager.argument("address", StringArgumentType.string())
                            // /archipelago join <address>
                            .executes(ctx -> {
                                var source = ctx.getSource();
                                String address = StringArgumentType.getString(ctx, "address");

                                // Default slot = Minecraft username
                                String slot = source.getPlayer().getName().getString();
                                joinAp(source, address, "38281", slot, null);
                                return 0;
                            })
                            .then(ClientCommandManager.argument("port", StringArgumentType.string())
                                // /archipelago join <address>
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    String address = StringArgumentType.getString(ctx, "address");
                                    String port = StringArgumentType.getString(ctx, "port");
                                    
                                    // Default slot = Minecraft username
                                    String slot = source.getPlayer().getName().getString();
                                    joinAp(source, address, port, slot, null);
                                    return 0;
                            })
                            .then(ClientCommandManager.argument("slot", StringArgumentType.string())
                                // /archipelago join <address> <slot>
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    String address = StringArgumentType.getString(ctx, "address");
                                    String slot = StringArgumentType.getString(ctx, "slot");
                                    String port = StringArgumentType.getString(ctx, "port");
                                    joinAp(source, address, port, slot, null);
                                    return 0;
                                })
                            .then(ClientCommandManager.argument("password", StringArgumentType.string())
                                // /archipelago join <address> <slot> <password>
                                .executes(ctx -> {
                                    var source = ctx.getSource();
                                    String address = StringArgumentType.getString(ctx, "address");
                                    String slot = StringArgumentType.getString(ctx, "slot");
                                    String password = StringArgumentType.getString(ctx, "password");
                                    String port = StringArgumentType.getString(ctx, "port");
                                    joinAp(source, address, port, slot, password);
                                    return 0;
                                })
                                )
                            )
                        )

                    )
                    .then(ClientCommandManager.literal("leave").executes(ctx -> {
                        APSession.CLIENT.close();
                        ctx.getSource().sendFeedback(Text.literal("Disconnected from Archipelago."));
                        return 0;
                    }))
                    .then(ClientCommandManager.literal("status").executes(ctx -> {
                        boolean c = APSession.CLIENT.isConnected();
                        String addr = APSession.CLIENT.getConnectedAddress();
                        ctx.getSource().sendFeedback(Text.literal(
                                c ? ("Connected to " + addr) : "Not connected."
                        ));
                        return 0;
                    }))
            ));
        });
        ClientSendMessageEvents.CHAT.register(message -> {
            // Only forward messages if there is an AP session connected
            if (APSession.CLIENT == null) return;

            var result = APSession.CLIENT.sendChat(message); // Sends to AP chat


        });
    }

    private static void joinAp(FabricClientCommandSource source, String address, String port, String slot, String password) {
        source.sendFeedback(Text.literal("Connecting to Archipelago at " + address + " as " + slot + "..."));

        APSession.ensureListeners();

        // Configure the AP Client
        APSession.CLIENT.setGame("Minecraft");
        APSession.CLIENT.setName(slot);

        if (password != null && !password.isBlank()){
            APSession.CLIENT.setPassword(password);
        }

        // Disconnect the old connection if needed
        if (APSession.CLIENT.isConnected()){
            APSession.CLIENT.close(); // closes websocket if connected
        }

        // Do not block the client thread
        new Thread(() -> {
            try {
                APSession.CLIENT.connect(address + ":" + port); // Defaults to port 38281 if missing
            } catch (URISyntaxException e){
                source.getClient().execute(() ->
                    source.sendError(Text.literal("Bad address. Try host:port (example: localhost:38281"))
                );
                return;
            }  catch (Exception e){
                source.getClient().execute(() ->
                        source.sendError(Text.literal("Connection failed: " + e.getMessage()))
                );
                return;
            }

            // Wait briefly and then report "socket open" status
            for (int i = 0; i < 50; i++){
                if (APSession.CLIENT.isConnected()){
                    String where = APSession.CLIENT.getConnectedAddress();
                    source.getClient().execute(() ->
                        source.sendFeedback(Text.literal("Connected! Socket open to " +
                                where + " (auth may still be in progress)"))
                    );
                    return;
                }
                try {Thread.sleep(100);} catch (InterruptedException ignored) {}
            }
            source.getClient().execute(() ->
                source.sendError((Text.literal(("Timed out waiting for socket to open. Check the address/port"))))
            );
        }, "Archipelago-Join").start();
    }
}