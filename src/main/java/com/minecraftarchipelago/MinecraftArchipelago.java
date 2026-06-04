package com.minecraftarchipelago;

import com.minecraftarchipelago.apitems.APItemsReloadListener;
import com.minecraftarchipelago.aplocations.APBossKillLocationsReloadListener;
import com.minecraftarchipelago.aplocations.APLocationsReloadListener;
import com.minecraftarchipelago.aplocations.BossKillListener;
import com.minecraftarchipelago.aplocations.VictoryCondition;
import com.minecraftarchipelago.apstages.APStagesReloadListener;
import com.minecraftarchipelago.apstages.item.ItemStageEnforcer;
import com.minecraftarchipelago.apstages.service.StageUnlockApplier;
import com.minecraftarchipelago.apstages.state.StageUnlockState;
import com.minecraftarchipelago.collections.ItemCollection;
import com.minecraftarchipelago.collections.ItemCollectionRegistry;
import com.minecraftarchipelago.item.ModItems;
import com.minecraftarchipelago.loot.APEntityDeathLootHandler;
import com.minecraftarchipelago.loot.APLootTableModifier;
import com.minecraftarchipelago.loot.APLootSourceRegistry;
import com.minecraftarchipelago.loot.APLootSourcesReloadListener;
import com.minecraftarchipelago.loot.APVillagerLootTrades;
import com.minecraftarchipelago.loot.AssignLootableCheckFunction;
import com.minecraftarchipelago.loot.ChestOpenHandler;
import com.minecraftarchipelago.loot.SetLootSourceFunction;
import com.minecraftarchipelago.victory.*;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecraftArchipelago implements ModInitializer {
	public static final String MOD_ID = "minecraftarchipelago";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which 14 mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.


		LOGGER.info("Hello Fabric world!");

		Registry.register(
				Registries.LOOT_FUNCTION_TYPE,
				Identifier.of("minecraftarchipelago", "assign_lootable_check"),
				AssignLootableCheckFunction.TYPE
		);
		Registry.register(
				Registries.LOOT_FUNCTION_TYPE,
				Identifier.of("minecraftarchipelago", "set_loot_source"),
				SetLootSourceFunction.TYPE
		);

		// --- Win condition checkers (order determines HUD display order) ---
		VictoryConditionRegistry.register(new AdvancementGoalChecker());
		VictoryConditionRegistry.register(new BossKillsChecker());
		VictoryConditionRegistry.register(new LootableChecksChecker());
		for (ItemCollection collection : ItemCollectionRegistry.getAll()) {
			VictoryConditionRegistry.register(new ItemCollectionChecker(collection));
		}

		ModItems.register();
		APLootSourceRegistry.loadBundledDefaults();
		APLootTableModifier.register();
		APEntityDeathLootHandler.register();
		APVillagerLootTrades.register();
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(new APLootSourcesReloadListener());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
			.registerReloadListener(new APStagesReloadListener());
		ItemStageEnforcer.register();
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(new APLocationsReloadListener());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(new APItemsReloadListener());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
				.registerReloadListener(new APBossKillLocationsReloadListener());
		BossKillListener.register();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			server.execute(() -> applyBaseRulesOnFirstJoin(server, player));
		});
	}

	static void applyBaseRulesOnFirstJoin(net.minecraft.server.MinecraftServer server,
										  ServerPlayerEntity player) {
		StageUnlockState state = StageUnlockState.get(server);
		Identifier baseRules = Identifier.of("minecraftarchipelago", "base_rules");

		if (state.unlock(player.getUuid(), baseRules)) {
			StageUnlockApplier.apply(player, baseRules);
		}
	}
}
