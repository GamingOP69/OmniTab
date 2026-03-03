package com.omnitab.common.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderRegistry {

    private static final Pattern INTERNAL_PLACEHOLDER_PATTERN = Pattern.compile("%(online_players|max_players|server_version|player_name|player_ping|server_tps|server_ram)%");

    public static String parse(Player player, String text) {
        if (text == null) return "";

        // Internal Placeholders
        Matcher matcher = INTERNAL_PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = getInternalValue(player, placeholder);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        text = sb.toString();

        // PlaceholderAPI Support (Hooking dynamically)
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String getInternalValue(Player player, String placeholder) {
        switch (placeholder) {
            case "online_players": return String.valueOf(Bukkit.getOnlinePlayers().size());
            case "max_players": return String.valueOf(Bukkit.getMaxPlayers());
            case "server_version": return Bukkit.getBukkitVersion();
            case "player_name": return player.getName();
            case "player_ping": 
                try {
                    Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
                    return String.valueOf(entityPlayer.getClass().getField("ping").get(entityPlayer));
                } catch (Exception e) { return "0"; }
            case "server_tps": return "20.0"; // Requires NMS for real TPS, 20.0 is a safe default
            case "server_ram": 
                long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;
                long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
                return (total - free) + "MB";
            default: return "";
        }
    }
}

