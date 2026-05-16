package com.minecraftarchipelago;

import com.minecraftarchipelago.hud.APHudRenderer;
import com.minecraftarchipelago.hud.APHudState;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
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

        APHudRenderer.register();

        // Register the toggle keybind (default: H, changeable in Controls)
        KeyBinding hudToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Toggle Hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "Archipelago"
        ));

        // Check the key every tick and toggle visibility
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (hudToggleKey.wasPressed()) {
                APHudState.visible = !APHudState.visible;
            }
        });

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
                        APSession.clearSlotData();
                        ctx.getSource().sendFeedback(Text.literal("Disconnected from Archipelago."));

                        MinecraftServer server = ctx.getSource().getClient().getServer();
                        if (server != null){
                            server.execute(() -> APConnectionState.get(server).clear());
                        }
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
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            // Runs on the client thread when a world is loaded
            MinecraftServer server = client.getServer();
            if (server == null) return; // Not a singleplayer world

            server.execute(() -> {
                APConnectionState connState = APConnectionState.get(server);
                if (!connState.hasSavedConnection()) return;
                if (APSession.CLIENT.isConnected()) return; // Already connected

                String host = connState.getHost();
                String port = connState.getPort();
                String slot = connState.getSlot();
                String password = connState.getPassword();

                client.execute(() -> {
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal(
                                "[AP] Auto-connecting to " + host + " as " + slot + "..."
                        ));
                    }
                    autoReconnect(host, port, slot, password, client);
                });
            });
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (APSession.CLIENT.isConnected()){
                APSession.CLIENT.close();
            }
            APSession.clearSlotData();
        });
    }

    private static void joinAp(FabricClientCommandSource source, String address, String port, String slot, String password) {
        // Store credentials so onConected can persist them
        APSession.setPendingCredentials(address, port, slot, password);

        source.sendFeedback(Text.literal("Connecting to Archipelago at " + address + " as " + slot + "..."));

        APSession.ensureListeners();

        // Configure the AP Client
        APSession.CLIENT.setGame("Minecraft Archipelago");
        APSession.CLIENT.setName(slot);
        APSession.slotName = slot;
        APSession.CLIENT.setItemsHandlingFlags(7);

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
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
            source.getClient().execute(() ->
                source.sendError((Text.literal(("Timed out waiting for socket to open. Check the address/port"))))
            );
        }, "Archipelago-Join").start();
    }

    private static void autoReconnect(
            String host, String port, String slot, String password, MinecraftClient mc
    ) {
        APSession.setPendingCredentials(host, port, slot, password);
        APSession.ensureListeners();
        APSession.CLIENT.setGame("Minecraft Archipelago");
        APSession.CLIENT.setName(slot);
        APSession.slotName = slot;
        APSession.CLIENT.setItemsHandlingFlags(7);

        if (password != null && !password.isBlank()) {
            APSession.CLIENT.setPassword(password);
        }

        if (APSession.CLIENT.isConnected()) {
            APSession.CLIENT.close();
        }

        new Thread(() -> {
            try {
                APSession.CLIENT.connect(host + ":" + port);
            } catch (Exception e){
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.sendMessage(Text.literal(
                                "[AP] Auto-connect failed: " + e.getMessage()
                        ));
                    }
                });
                return;
            }

            for (int i = 0; i < 50; i++) {
                if (APSession.CLIENT.isConnected()) return;
                try {
                    Thread.sleep(100);
                    notifyReconnectAttempt(i, 50, 5);
                } catch (InterruptedException ignored) {}
            }

            notifyReconnectFailed();
        }, "Archipelago-AutoConnect").start();
    }

    // At the start of each attempt
    public static void notifyReconnectAttempt(int attempt, int maxAttempts, int delaySeconds) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            var player = mc.player;
            if (player == null) return;
            if (attempt == 0) return;
            player.sendMessage(
                    Text.literal(String.format(
                            "[AP] Reconnecting... (attempt %d/%d, waiting %ds)",
                            attempt, maxAttempts, delaySeconds
                    )).formatted(Formatting.YELLOW),
                    true
            );
        });
    }

    // When all attempts are exhausted
    public static void notifyReconnectFailed() {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            var player = mc.player;
            if (player == null) return;
            player.sendMessage(
                    Text.literal("[AP] Could not reconnect automatically.")
                            .formatted(Formatting.RED)
            );
            player.sendMessage(
                    Text.literal("[AP] Use /archipelago join <address> to reconnect manually.")
                            .formatted(Formatting.GRAY)
            );
        });
    }
}