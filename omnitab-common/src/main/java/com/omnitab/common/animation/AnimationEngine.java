package com.omnitab.common.animation;

import com.omnitab.api.TablistHandler;
import com.omnitab.common.placeholder.PlaceholderRegistry;
import com.omnitab.common.sort.SortingRegistry;
import com.omnitab.common.hooks.VanishRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationEngine {

    private final JavaPlugin plugin;
    private final Map<String, List<String>> animations = new ConcurrentHashMap<>();
    private final Map<String, Integer> currentFrames = new ConcurrentHashMap<>();
    private final TablistHandler handler;

    private final Map<String, List<String>> headerGroups = new HashMap<>();
    private final Map<String, List<String>> footerGroups = new HashMap<>();
    private final VanishRegistry vanishRegistry;

    public AnimationEngine(JavaPlugin plugin, TablistHandler handler) {
        this.plugin = plugin;
        this.handler = handler;
        this.vanishRegistry = new VanishRegistry();
    }

    public void setTemplates(List<String> header, List<String> footer) {
        this.headerTemplate = header;
        this.footerTemplate = footer;
    }

    public void registerAnimation(String id, List<String> frames) {
        animations.put(id, frames);
        currentFrames.put(id, 0);
    }

    public void start() {
        int interval = plugin.getConfig().getInt("tablist.update_interval", 10);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateAll, 0, interval);
    }

    public void updateSingle(Player player) {
        if (player == null || !player.isOnline()) return;
        
        // Skip update for vanished (will be handled by Join/Quit logic)
        if (vanishRegistry.isVanished(player)) return;

        String group = getPlayerGroup(player);
        List<String> headers = headerGroups.getOrDefault(group, headerGroups.get("default"));
        List<String> footers = footerGroups.getOrDefault(group, footerGroups.get("default"));

        String header = buildString(player, headers);
        String footer = buildString(player, footers);
        handler.updateHeaderFooter(player, header, footer);
    }

    private String getPlayerGroup(Player player) {
        return com.omnitab.core.OmniTab.getInstance().getPermissionHook().getPrimaryGroup(player);
    }

    private void updateAll() {
        SortingRegistry sortingRegistry = com.omnitab.core.OmniTab.getInstance().getSortingRegistry();
        
        // Safe access to online players in async task
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer == null || !viewer.isOnline()) continue;
            
            // 1. Update Header/Footer for the viewer
            updateSingle(viewer);
            
            // 2. Update player list entries (Prefixes/Ping) for the viewer
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target == null || !target.isOnline()) continue;
                if (vanishRegistry.isVanished(target) && !viewer.hasPermission("omnitab.vanish.see")) continue;

                SortingRegistry.Group group = sortingRegistry.getGroup(target);
                handler.updatePlayerEntry(viewer, target, group.prefix, group.suffix, getPing(target));
            }
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
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < template.size(); i++) {
            builder.append(template.get(i));
            if (i < template.size() - 1) builder.append("\n");
        }
        return PlaceholderRegistry.parse(player, builder.toString());
    }
}
