package com.omnitab.common.sort;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.TreeMap;

public class SortingRegistry {

    private final Map<Integer, String> groups = new TreeMap<>();

    public void registerGroup(int priority, String permission) {
        groups.put(priority, permission);
    }

    public void applySorting(Player player) {
        Scoreboard sb = player.getScoreboard();
        if (sb == null) sb = Bukkit.getScoreboardManager().getMainScoreboard();

        int priority = getPriority(player);
        String teamName = "OT_" + String.format("%03d", priority) + "_" + player.getName();
        
        // Truncate team name to 16 chars for legacy versions if needed
        if (teamName.length() > 16 && Bukkit.getBukkitVersion().contains("1.8")) {
            teamName = teamName.substring(0, 16);
        }

        Team team = sb.getTeam(teamName);
        if (team == null) {
            team = sb.registerNewTeam(teamName);
        }
        
        if (!team.hasEntry(player.getName())) {
            // Remove from old teams first
            for (Team oldTeam : sb.getTeams()) {
                if (oldTeam.getName().startsWith("OT_") && oldTeam.hasEntry(player.getName())) {
                    oldTeam.removeEntry(player.getName());
                }
            }
            team.addEntry(player.getName());
        }
    }

    private int getPriority(Player player) {
        for (Map.Entry<Integer, String> entry : groups.entrySet()) {
            if (player.hasPermission(entry.getValue())) {
                return entry.getKey();
            }
        }
        return 999;
    }
}
