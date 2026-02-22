package com.minecraftarchipelago.apstages.command;

import com.minecraftarchipelago.apstages.StageRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;

public final class StageCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerStagesCommand(dispatcher);
        }); // register callback for server command registration :contentReference[oaicite:1]{index=1}
    }

    private static void registerStagesCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stages")
            .then(CommandManager.literal("list")
                .executes(ctx -> {
                    ServerCommandSource source = ctx.getSource();

                    List<Identifier> stages = StageRegistry.stageIds();
                    List<Identifier> packages = StageRegistry.packageIds();

                    stages.sort(Comparator.comparing(Identifier::toString));
                    packages.sort(Comparator.comparing(Identifier::toString));

                    source.sendFeedback(() -> Text.literal(
                            "Loaded " + stages.size() + " stages and " + packages.size() + " packages."
                    ), false);

                    source.sendFeedback(() -> Text.literal("Stages:"), false);
                    for (Identifier id : stages) {
                        source.sendFeedback(() -> Text.literal(" - " + id), false);
                    }

                    source.sendFeedback(() -> Text.literal("Packages:"), false);
                    for (Identifier id : packages) {
                        source.sendFeedback(() -> Text.literal(" - " + id), false);
                    }

                    return 1;
                })
            )
        );
    }

    private StageCommands() {}
}
