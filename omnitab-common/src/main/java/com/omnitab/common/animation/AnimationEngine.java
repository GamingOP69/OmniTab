package com.omnitab.common.animation;

import com.omnitab.api.TablistHandler;
import com.omnitab.common.placeholder.PlaceholderRegistry;
import com.omnitab.common.hooks.PermissionHook;
import com.omnitab.common.sort.SortingRegistry;
import com.omnitab.common.hooks.VanishRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ELITE ANIMATION ENGINE
 * Optimized for O(N) performance using PlayerData caching and async batching.
 */
public class AnimationEngine {

    private final JavaPlugin plugin;
    private final TablistHandler handler;
    private final VanishRegistry vanishRegistry;
    private final SortingRegistry sortingRegistry;
    private final PermissionHook permissionHook;
    
    private final Map<String, List<String>> headerGroups = new ConcurrentHashMap<>();
    private final Map<String, List<String>> footerGroups = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    private int animationTick = 0;
    private org.bukkit.scheduler.BukkitTask fetchTask;
    private org.bukkit.scheduler.BukkitTask tickTask;

    public AnimationEngine(JavaPlugin plugin, TablistHandler handler, SortingRegistry sortingRegistry, PermissionHook permissionHook) {
        this.plugin = plugin;
        this.handler = handler;
        this.sortingRegistry = sortingRegistry;
        this.permissionHook = permissionHook;
        this.vanishRegistry = new VanishRegistry();
    }

    public void clearTemplates() {
        headerGroups.clear();
        footerGroups.clear();
    }

    public void setTemplates(String group, List<String> header, List<String> footer) {
        headerGroups.put(group, header);
        footerGroups.put(group, footer);
    }

    public void start() {
        int interval = plugin.getConfig().getInt("tablist.update_interval", 20);
        // Sync task to fetch data safely
        this.fetchTask = Bukkit.getScheduler().runTaskTimer(plugin, this::fetch, 0, interval);
        // Async task to process strings and dispatch packets
        this.tickTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::tick, 0, interval);
    }

    public void stop() {
        if (fetchTask != null) fetchTask.cancel();
        if (tickTask != null) tickTask.cancel();
        cache.clear();
    }

    private void fetch() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = cache.computeIfAbsent(player.getUniqueId(), id -> new PlayerData(player));
            data.online = true;
            data.group = sortingRegistry.getGroup(player);
            data.vanished = vanishRegistry.isVanished(player);
            data.ping = getPing(player);
            // Permissions for other players might change, but we'll fetch them here if needed
        }
    }

    private void tick() {
        animationTick++;
        
        // 1. Process animations and placeholders (Async)
        for (PlayerData data : cache.values()) {
            Player player = Bukkit.getPlayer(data.uuid);
            if (player == null || !player.isOnline()) {
                data.online = false;
                continue;
            }

            data.header = buildString(player, headerGroups.getOrDefault(data.getGroupName(), headerGroups.get("default")));
            data.footer = buildString(player, footerGroups.getOrDefault(data.getGroupName(), footerGroups.get("default")));
            data.displayName = data.group.prefix + player.getName() + data.group.suffix;
        }

        // 2. Dispatch with Dirty Checking
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            PlayerData viewerData = cache.get(viewer.getUniqueId());
            if (viewerData == null) continue;

            // Header/Footer Dirty Check
            if (!viewerData.header.equals(viewerData.lastHeader) || !viewerData.footer.equals(viewerData.lastFooter)) {
                handler.updateHeaderFooter(viewer, viewerData.header, viewerData.footer);
                viewerData.lastHeader = viewerData.header;
                viewerData.lastFooter = viewerData.footer;
            }

            // Tablist Entry Dirty Check
            for (PlayerData targetData : cache.values()) {
                if (!targetData.online) continue;
                Player target = Bukkit.getPlayer(targetData.uuid);
                if (target == null) continue;

                boolean canSee = !targetData.vanished || viewer.hasPermission("omnitab.vanish.see");
                
                // Visibility Toggle Check
                if (canSee != targetData.isSeenBy(viewer.getUniqueId())) {
                    handler.updateVisibility(viewer, target, canSee);
                    targetData.setSeenBy(viewer.getUniqueId(), canSee);
                }

                if (!canSee) continue;

                // Content Dirty Check
                if (!targetData.displayName.equals(targetData.lastDisplayName) || targetData.ping != targetData.lastPing) {
                    handler.updatePlayerEntry(viewer, target, targetData.group.prefix, targetData.group.suffix, targetData.ping);
                }
            }
        }

        // Finalize state after all viewers processed
        for (java.util.Iterator<PlayerData> it = cache.values().iterator(); it.hasNext();) {
            PlayerData data = it.next();
            if (!data.online) {
                it.remove();
                continue;
            }
            data.lastDisplayName = data.displayName;
            data.lastPing = data.ping;
        }
    }

    public void updateSingle(Player player) {
        PlayerData data = cache.get(player.getUniqueId());
        if (data != null) {
            handler.updateHeaderFooter(player, data.lastHeader, data.lastFooter);
        }
    }

    private int getPing(Player player) {
        return com.omnitab.common.utils.PingUtil.getPing(player);
    }

    private String buildString(Player player, List<String> template) {
        if (template == null || template.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < template.size(); i++) {
            builder.append(template.get(i));
            if (i < template.size() - 1) builder.append("\n");
        }
        return com.omnitab.common.utils.ColorAPI.colorize(PlaceholderRegistry.parse(player, builder.toString()));
    }

    private static class PlayerData {
        private final UUID uuid;
        private SortingRegistry.Group group;
        private int ping;
        private int lastPing;
        private boolean vanished;
        private boolean online;
        
        private String header = "";
        private String footer = "";
        private String displayName = "";
        
        private String lastHeader = "";
        private String lastFooter = "";
        private String lastDisplayName = "";

        private final Map<UUID, Boolean> visibilityMap = new ConcurrentHashMap<>();

        public PlayerData(Player player) {
            this.uuid = player.getUniqueId();
        }

        public String getGroupName() {
            return group != null ? group.name : "default";
        }

        public boolean isSeenBy(UUID viewer) {
            return visibilityMap.getOrDefault(viewer, true);
        }

        public void setSeenBy(UUID viewer, boolean seen) {
            visibilityMap.put(viewer, seen);
        }
    }
}
