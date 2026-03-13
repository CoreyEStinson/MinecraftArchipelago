package com.minecraftarchipelago.apstages.command;

import com.minecraftarchipelago.MinecraftArchipelago;
import com.minecraftarchipelago.apstages.StageRegistry;
import com.minecraftarchipelago.apstages.service.StageUnlockApplier;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class StageCommands {
    private static final String DEFAULT_NAMESPACE = MinecraftArchipelago.MOD_ID;

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
            .then(CommandManager.literal("debug")
                .requires(source -> source.hasPermissionLevel(2)) // ops only
                .then(CommandManager.literal("unlock")
                    .then(CommandManager.argument("stage", StringArgumentType.string())
                        .suggests(StageCommands::suggestStageIds)
                        .executes(ctx -> debugUnlock(ctx.getSource(),
                            StringArgumentType.getString(ctx, "stage")))))
                .then(CommandManager.literal("unlocked")
                    .executes(ctx -> debugUnlocked(ctx.getSource())))
                .then(CommandManager.literal("reset")
                    .executes(ctx -> debugReset(ctx.getSource())))
            )
        );
    }

    private static CompletableFuture<Suggestions> suggestStageIds(
        CommandContext<ServerCommandSource> ctx,
        SuggestionsBuilder builder
    ) {
        Set<String> suggestions = new TreeSet<>();
        for (Identifier id : StageRegistry.stageIds()) {
            suggestions.add(id.getPath());
        }

        return CommandSource.suggestMatching(suggestions, builder);
    }

    private static int debugUnlock(ServerCommandSource source, String stageString) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();

        Identifier stageId = resolveStageId(stageString);
        if (stageId == null) {
            source.sendError(Text.literal("Unknown stage id: " + stageString + " (try /stages list)"));
            return 0;
        }

        StageUnlockState state = StageUnlockState.get(source.getServer());
        boolean added = state.unlock(player.getUuid(), stageId);

        if (added) {
            source.sendFeedback(() -> Text.literal("Unlocked stage: " + stageId), false);
            StageUnlockApplier.apply(player, stageId);
        }
        else {
            source.sendFeedback(() -> Text.literal("Already unlocked: " + stageId), false);
        }

        return  1;
    }

    private static Identifier resolveStageId(String stageString) {
        if (stageString == null) return null;

        String raw = stageString.trim();
        if (raw.isEmpty()) return null;

        Identifier candidate = raw.contains(":")
            ? Identifier.tryParse(raw)
            : Identifier.tryParse(DEFAULT_NAMESPACE + ":" + raw);

        if (candidate == null) return null;
        return StageRegistry.getStage(candidate) != null ? candidate : null;
    }

    private static int debugUnlocked(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        StageUnlockState state = StageUnlockState.get(source.getServer());
        
        Set<Identifier> unlocked = state.getUnlocked(player.getUuid());
        if (unlocked.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No stages unlocked."), false);
            return  0;
        }
        
        String joined = unlocked.stream().map(Identifier::toString).sorted().collect(Collectors.joining(", "));
        source.sendFeedback(() -> Text.literal("Unlocked: " + joined), false);
        return 1;
    }
    
    private static int debugReset(ServerCommandSource source) throws  CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        StageUnlockState state = StageUnlockState.get(source.getServer());
        
        boolean removed = state.reset(player.getUuid());
        source.sendFeedback(() -> Text.literal(removed ? "Reset your unlocks." : "Nothing to reset"), false);
        return  1;
    }
    

    private StageCommands() {}
}
