package com.minecraftarchipelago;

import com.minecraftarchipelago.apstages.APStagesReloadListener;
import com.minecraftarchipelago.apstages.command.StageCommands;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
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
		ResourceManagerHelper.get(ResourceType.SERVER_DATA)
			.registerReloadListener(new APStagesReloadListener());
		StageCommands.register();
	}
}