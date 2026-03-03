package com.omnitab.core;

import com.omnitab.api.TablistHandler;
import com.omnitab.common.animation.AnimationEngine;
import com.omnitab.common.language.LanguageManager;
import com.omnitab.common.sort.SortingRegistry;
import com.omnitab.core.commands.OmniTabCommand;
import org.bukkit.configuration.ConfigurationSection;
import com.omnitab.core.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class OmniTab extends JavaPlugin implements Listener {

    private static OmniTab instance;
    private TablistHandler tablistHandler;
    private AnimationEngine animationEngine;
    private SortingRegistry sortingRegistry;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize Language Manager
        this.languageManager = new LanguageManager(this);
        languageManager.loadLanguage(getConfig().getString("settings.language", "en"));

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

        // Initialize Sorting Registry
        this.sortingRegistry = new SortingRegistry();
        ConfigurationSection groups = getConfig().getConfigurationSection("sorting.groups");
        if (groups != null) {
            for (String key : groups.getKeys(false)) {
                int priority = groups.getInt(key + ".priority");
                String permission = groups.getString(key + ".permission");
                sortingRegistry.registerGroup(priority, permission);
            }
        }

        // Initialize Animation Engine
        this.animationEngine = new AnimationEngine(this, tablistHandler);
        this.animationEngine.setTemplates(
            getConfig().getStringList("tablist.header"),
            getConfig().getStringList("tablist.footer")
        );
        this.animationEngine.start();

        // Register Commands & Events
        getCommand("omnitab").setExecutor(new OmniTabCommand());
        Bukkit.getPluginManager().registerEvents(this, this);

        // Update Checker
        new UpdateChecker(this, 123456).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                getLogger().warning("A new update is available: v" + version);
            }
        });

        getLogger().info("OmniTab enabled on " + Bukkit.getVersion());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sortingRegistry.applySorting(event.getPlayer());
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            animationEngine.updateSingle(event.getPlayer());
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Cleanup if needed
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
    public LanguageManager getLanguageManager() { return languageManager; }
}
