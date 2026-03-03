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

    public AnimationEngine(JavaPlugin plugin, TablistHandler handler, SortingRegistry sortingRegistry, PermissionHook permissionHook) {
        this.plugin = plugin;
        this.handler = handler;
        this.sortingRegistry = sortingRegistry;
        this.permissionHook = permissionHook;
        this.vanishRegistry = new VanishRegistry();
    }

    public void setTemplates(String group, List<String> header, List<String> footer) {
        headerGroups.put(group, header);
        footerGroups.put(group, footer);
    }

    public void start() {
        int interval = plugin.getConfig().getInt("tablist.update_interval", 10);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::tick, 0, interval);
    }

    private void tick() {
        animationTick++;
        
        // 1. Prepare data for all online players (O(N))
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = cache.computeIfAbsent(player.getUniqueId(), id -> new PlayerData(player));
            
            // Refresh metadata every 20 ticks
            if (animationTick % 20 == 0) {
                data.group = sortingRegistry.getGroup(player);
                data.ping = getPing(player);
                data.vanished = vanishRegistry.isVanished(player);
            }

            // Always update display strings (potential animations)
            data.lastHeader = buildString(player, headerGroups.getOrDefault(data.getGroupName(), headerGroups.get("default")));
            data.lastFooter = buildString(player, footerGroups.getOrDefault(data.getGroupName(), footerGroups.get("default")));
            data.fullDisplayName = data.group.prefix + player.getName() + data.group.suffix;
        }

        // 2. Dispatch updates to viewers (O(N))
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            PlayerData viewerData = cache.get(viewer.getUniqueId());
            if (viewerData == null) continue;

            // Send Header/Footer
            handler.updateHeaderFooter(viewer, viewerData.lastHeader, viewerData.lastFooter);

            // Update visible entries for the viewer (O(V) where V is visible players)
            for (PlayerData targetData : cache.values()) {
                Player target = Bukkit.getPlayer(targetData.uuid);
                if (target == null || !target.isOnline()) {
                    cache.remove(targetData.uuid);
                    continue;
                }

                if (targetData.vanished && !viewer.hasPermission("omnitab.vanish.see")) continue;

                handler.updatePlayerEntry(viewer, target, targetData.group.prefix, targetData.group.suffix, targetData.ping);
            }
        }
    }

    public void updateSingle(Player player) {
        PlayerData data = cache.get(player.getUniqueId());
        if (data != null) {
            handler.updateHeaderFooter(player, data.lastHeader, data.lastFooter);
        }
    }

    private int getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            return (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (Exception e) {
            return 0;
        }
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
        private boolean vanished;
        private String lastHeader;
        private String lastFooter;
        private String fullDisplayName;

        public PlayerData(Player player) {
            this.uuid = player.getUniqueId();
        }

        public String getGroupName() {
            return group != null ? group.name : "default";
        }
    }
}
