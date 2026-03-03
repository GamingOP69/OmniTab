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
import org.bukkit.entity.Player;
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
        
        metrics.addCustomChart(new SimplePie("active_language", () -> 
            getConfig().getString("settings.language", "en")));
            
        metrics.addCustomChart(new SimplePie("using_vanish_hook", () -> 
            vanishRegistry != null && !vanishRegistry.getVanishPlugins().isEmpty() ? "Yes" : "No"));

        metrics.addCustomChart(new SimplePie("using_placeholderapi", () -> 
            Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "Yes" : "No"));
        
        // Version Detection & Adapter Setup
        if (!setupAdapter()) {
            getLogger().severe("Failed to detect server version! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize Sorting Registry
        this.sortingRegistry = new SortingRegistry();
        ConfigurationSection sortingGroups = getConfig().getConfigurationSection("tablist.sorting.groups");
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
        this.animationEngine = new AnimationEngine(this, tablistHandler, sortingRegistry, permissionHook);
        
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

        // Update & Auto-Update System
        if (getConfig().getBoolean("settings.check_for_updates", true)) {
            UpdateChecker checker = new UpdateChecker(this, "GamingOP69/OmniTab");
            checker.check().thenAccept(latest -> {
                if (checker.isNewer(this.getDescription().getVersion(), latest)) {
                    getLogger().warning("====================================================");
                    getLogger().warning("OmniTab UPDATE AVAILABLE! Version: " + latest);
                    
                    if (getConfig().getBoolean("settings.auto_update", false)) {
                        getLogger().info("Auto-update enabled. Downloading latest version...");
                        checker.download(Bukkit.getConsoleSender()).thenAccept(success -> {
                            if (success) {
                                getLogger().info("Update downloaded successfully to 'plugins/update/'. It will be applied on next restart.");
                            } else {
                                getLogger().warning("Failed to download the update automatically.");
                            }
                        });
                    } else {
                        getLogger().warning("Download: https://github.com/GamingOP69/OmniTab/releases");
                        getLogger().warning("Or run /omnitab update to download automatically.");
                    }
                    getLogger().warning("====================================================");
                }
            });
        }

        getLogger().info("========================================");
        getLogger().info("   OmniTab Universal v" + getDescription().getVersion());
        getLogger().info("   Developed and Maintained by GamingOP");
        getLogger().info("   Status: Operational - Universal Engine Active");
        getLogger().info("========================================");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sortingRegistry.applySorting(event.getPlayer());
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            animationEngine.updateSingle(event.getPlayer());
        }, 5L);
    }

    @Override
    public void onDisable() {
        if (animationEngine != null) {
            animationEngine.stop();
        }
        getLogger().info("OmniTab has been safely disabled.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Player data will be flagged as offline in the next tick and removed
    }

    public void reloadPlugin() {
        reloadConfig();
        
        // Reload Language
        languageManager.loadLanguage(getConfig().getString("settings.language", "en"));
        
        // Reload Sorting Groups
        sortingRegistry.clearGroups();
        ConfigurationSection sortingGroups = getConfig().getConfigurationSection("tablist.sorting.groups");
        if (sortingGroups != null) {
            for (String key : sortingGroups.getKeys(false)) {
                int priority = sortingGroups.getInt(key + ".priority");
                String permission = sortingGroups.getString(key + ".permission");
                String prefix = sortingGroups.getString(key + ".prefix", "");
                String suffix = sortingGroups.getString(key + ".suffix", "");
                sortingRegistry.registerGroup(key, priority, permission, prefix, suffix);
            }
        }
        
        // Reload Animation Templates
        animationEngine.clearTemplates();
        animationEngine.setTemplates("default", 
            getConfig().getStringList("tablist.header"),
            getConfig().getStringList("tablist.footer")
        );
        ConfigurationSection groupSection = getConfig().getConfigurationSection("tablist.groups");
        if (groupSection != null) {
            for (String group : groupSection.getKeys(false)) {
                animationEngine.setTemplates(group,
                    groupSection.getStringList(group + ".header"),
                    groupSection.getStringList(group + ".footer")
                );
            }
        }
        
        // Force refresh for all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            sortingRegistry.applySorting(player);
            animationEngine.updateSingle(player);
        }
    }

    private boolean setupAdapter() {
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String bukkitVersion = Bukkit.getBukkitVersion();
        
        getLogger().info("Initializing version adapter...");
        getLogger().info("Server Package: " + pkg);
        getLogger().info("Bukkit Version: " + bukkitVersion);

        String[] parts = pkg.split("\\.");
        String nmsVersion = (parts.length >= 4) ? parts[3] : "";

        try {
            // Priority 1: NMS Package Check (Legacy & 1.12)
            if (nmsVersion.equals("v1_7_R4") || bukkitVersion.contains("1.7.10")) {
                tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_7_R4.HandlerImpl").newInstance();
                getLogger().info("Detected Legacy 1.7.10 Stack.");
            }
            else if (nmsVersion.equals("v1_8_R3") || bukkitVersion.contains("1.8.8")) {
                tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_8_R3.HandlerImpl").newInstance();
                getLogger().info("Detected Legacy 1.8.8 Stack.");
            } 
            else if (nmsVersion.equals("v1_12_R1") || bukkitVersion.contains("1.12")) {
                tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_12_R1.HandlerImpl").newInstance();
                getLogger().info("Detected 1.12.2 Stack.");
            } 
            // Priority 2: Modern Bukkit Version Check (1.21+)
            else if (bukkitVersion.contains("1.21")) {
                tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_21_R1.HandlerImpl").newInstance();
                getLogger().info("Detected Modern 1.21.x Stack.");
            } 
            // Priority 3: General Fallback for 1.20-1.21 range
            else if (bukkitVersion.contains("1.20") || nmsVersion.startsWith("v1_20")) {
                tablistHandler = (TablistHandler) Class.forName("com.omnitab.adapters.v1_21_R1.HandlerImpl").newInstance();
                getLogger().info("Using 1.21 Adapter Fallback for " + bukkitVersion);
            } 
            else {
                getLogger().severe("Unsupported server version detected: " + bukkitVersion);
                return false;
            }
            
            return tablistHandler != null;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Fatal error during adapter initialization: " + bukkitVersion, e);
            return false;
        }
    }

    public static OmniTab getInstance() { return instance; }
    public TablistHandler getTablistHandler() { return tablistHandler; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public PermissionHook getPermissionHook() { return permissionHook; }
    public VanishRegistry getVanishRegistry() { return vanishRegistry; }
}
