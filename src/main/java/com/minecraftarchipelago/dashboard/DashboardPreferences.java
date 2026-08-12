package com.minecraftarchipelago.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minecraftarchipelago.MinecraftArchipelago;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class DashboardPreferences {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("minecraftarchipelago.dashboard.json");

    private static DashboardPreferences instance = new DashboardPreferences();

    public boolean hideCompletedAdvancements = false;
    public boolean showAdvancementDescriptions = true;
    public boolean showDeathLinkStatusStrip = true;
    public int recentActivityCount = 5;
    public String selectedDashboardPage = "OVERVIEW";

    public static DashboardPreferences get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            instance = new DashboardPreferences();
            return;
        }

        try {
            DashboardPreferences loaded = GSON.fromJson(
                    Files.readString(CONFIG_PATH),
                    DashboardPreferences.class
            );

            instance = loaded != null ? loaded : new DashboardPreferences();
            instance.recentActivityCount =
                    instance.recentActivityCount == 3 ? 3 : 5;
        } catch (Exception e) {
            MinecraftArchipelago.LOGGER.warn(
                    "[AP Dashboard] Could not load preferences {}",
                    e.getMessage()
            );
            instance = new DashboardPreferences();
        }
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (Exception e) {
            MinecraftArchipelago.LOGGER.warn(
                    "[AP Dashboard] Could not save preferences {}",
                    e.getMessage()
            );
        }

    }
}
