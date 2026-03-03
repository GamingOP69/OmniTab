package com.omnitab.common.sort;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;
import java.util.TreeMap;

public class SortingRegistry {

    private final Map<Integer, Group> groups = new TreeMap<>();

    public static class Group {
        public final String name;
        public final int priority;
        public final String permission;
        public final String prefix;
        public final String suffix;

        public Group(String name, int priority, String permission, String prefix, String suffix) {
            this.name = name;
            this.priority = priority;
            this.permission = permission;
            this.prefix = com.omnitab.common.utils.ColorAPI.colorize(prefix);
            this.suffix = com.omnitab.common.utils.ColorAPI.colorize(suffix);
        }
    }

    public void registerGroup(String name, int priority, String permission, String prefix, String suffix) {
        groups.put(priority, new Group(name, priority, permission, prefix, suffix));
    }

    public void clearGroups() {
        groups.clear();
    }

    public void applySorting(Player player) {
        Scoreboard sb = player.getScoreboard();
        if (sb == null || sb == Bukkit.getScoreboardManager().getMainScoreboard()) {
            sb = Bukkit.getScoreboardManager().getMainScoreboard();
        }

        Group group = getGroup(player);
        int priority = group.priority;
        
        // Z-prefix ensures it sorts properly in alphabetical order (OT_001_...)
        String teamName = "OT_" + String.format("%03d", priority) + "_" + player.getName();
        
        if (teamName.length() > 16 && Bukkit.getBukkitVersion().contains("1.8")) {
            teamName = teamName.substring(0, 16);
        }

        Team team = sb.getTeam(teamName);
        if (team == null) {
            team = sb.registerNewTeam(teamName);
        }

        // We ONLY use teams for sorting (alphabetical order). 
        // We explicitly clear prefix/suffix to prevent interference with chat/join/death messages.
        // The Tablist prefix/suffix is handled via PlayerInfo packets in the AnimationEngine.
        team.setPrefix("");
        team.setSuffix("");
        
        if (!team.hasEntry(player.getName())) {
            clearOldTeams(sb, player);
            team.addEntry(player.getName());
        }
    }

    private void clearOldTeams(Scoreboard sb, Player player) {
        for (Team oldTeam : sb.getTeams()) {
            if (oldTeam.getName().startsWith("OT_") && oldTeam.hasEntry(player.getName())) {
                oldTeam.removeEntry(player.getName());
            }
        }
    }

    public Group getGroup(Player player) {
        for (Group group : groups.values()) {
            if (player.hasPermission(group.permission)) {
                return group;
            }
        }
        return new Group("default", 999, "omnitab.default", "", "");
    }
}
