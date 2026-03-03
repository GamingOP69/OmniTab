package com.omnitab.core;

import com.omnitab.api.TablistHandler;
import com.omnitab.common.animation.AnimationEngine;
import com.omnitab.common.hooks.PermissionHook;
import com.omnitab.common.hooks.VanishRegistry;
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
    private PermissionHook permissionHook;
    private VanishRegistry vanishRegistry;

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
        ConfigurationSection sortingGroups = getConfig().getConfigurationSection("sorting.groups");
        if (sortingGroups != null) {
            for (String key : sortingGroups.getKeys(false)) {
                int priority = sortingGroups.getInt(key + ".priority");
                String permission = sortingGroups.getString(key + ".permission");
                String prefix = sortingGroups.getString(key + ".prefix", "");
                String suffix = sortingGroups.getString(key + ".suffix", "");
                sortingRegistry.registerGroup(key, priority, permission, prefix, suffix);
            }
        }

        // Initialize Hooks
        this.permissionHook = new PermissionHook();
        this.vanishRegistry = new VanishRegistry();

        // Initialize Animation Engine
        this.animationEngine = new AnimationEngine(this, tablistHandler);
        
        // Load Default Templates
        this.animationEngine.setTemplates("default", 
            getConfig().getStringList("tablist.header"),
            getConfig().getStringList("tablist.footer")
        );

        // Load Group Templates
        ConfigurationSection groupSection = getConfig().getConfigurationSection("tablist.groups");
        if (groupSection != null) {
            for (String group : groupSection.getKeys(false)) {
                animationEngine.setTemplates(group,
                    groupSection.getStringList(group + ".header"),
                    groupSection.getStringList(group + ".footer")
                );
            }
        }
        
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
    public PermissionHook getPermissionHook() { return permissionHook; }
    public VanishRegistry getVanishRegistry() { return vanishRegistry; }
}
