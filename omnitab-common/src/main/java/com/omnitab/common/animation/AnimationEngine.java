package com.omnitab.common.animation;

import com.omnitab.api.TablistHandler;
import com.omnitab.common.placeholder.PlaceholderRegistry;
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

    private List<String> headerTemplate;
    private List<String> footerTemplate;

    public AnimationEngine(JavaPlugin plugin, TablistHandler handler) {
        this.plugin = plugin;
        this.handler = handler;
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
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            updateAll();
        }, 0, 1); // Run every tick, internal logic handles timing if needed
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String header = buildString(player, headerTemplate);
            String footer = buildString(player, footerTemplate);
            handler.updateHeaderFooter(player, header, footer);
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
