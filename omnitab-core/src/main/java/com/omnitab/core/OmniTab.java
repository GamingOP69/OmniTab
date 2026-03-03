package com.omnitab.core;

import com.omnitab.api.TablistHandler;
import com.omnitab.common.animation.AnimationEngine;
import com.omnitab.core.commands.OmniTabCommand;
import com.omnitab.core.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class OmniTab extends JavaPlugin {

    private static OmniTab instance;
    private TablistHandler tablistHandler;
    private AnimationEngine animationEngine;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // bStats Integration
        int pluginId = 29876;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SimplePie("server_version", () -> Bukkit.getBukkitVersion()));
        
        // Version Detection & Adapter Setup
        if (!setupAdapter()) {
            getLogger().severe("Failed to detect server version! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize Animation Engine
        this.animationEngine = new AnimationEngine(this, tablistHandler);
        this.animationEngine.setTemplates(
            getConfig().getStringList("tablist.header"),
            getConfig().getStringList("tablist.footer")
        );
        this.animationEngine.start();

        // Register Commands
        getCommand("omnitab").setExecutor(new OmniTabCommand());

        // Update Checker
        new UpdateChecker(this, 123456).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                getLogger().warning("A new update is available: v" + version);
            }
        });

        getLogger().info("OmniTab enabled on " + Bukkit.getVersion());
    }

    private boolean setupAdapter() {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            switch (version) {
                case "v1_8_R3":
                    tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_8_R3.HandlerImpl").newInstance();
                    break;
                case "v1_12_R1":
                    tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_12_R1.HandlerImpl").newInstance();
                    break;
                case "v1_21_R1":
                    tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_21_R1.HandlerImpl").newInstance();
                    break;
                default:
                    // Soft version check fallback
                    if (Bukkit.getBukkitVersion().contains("1.21")) {
                        tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_21_R1.HandlerImpl").newInstance();
                    } else {
                        return false;
                    }
            }
            return tablistHandler != null;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Adapter failure: " + version, e);
            return false;
        }
    }

    public static OmniTab getInstance() { return instance; }
    public TablistHandler getTablistHandler() { return tablistHandler; }
}
