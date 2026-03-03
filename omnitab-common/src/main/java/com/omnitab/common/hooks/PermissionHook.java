package com.omnitab.common.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class PermissionHook {

    private final boolean luckPerms;

    public PermissionHook() {
        this.luckPerms = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
    }

    public String getPrimaryGroup(Player player) {
        if (luckPerms) {
            return getLuckPermsGroup(player);
        }
        // Fallback or logic for other plugins
        return "default";
    }

    private String getLuckPermsGroup(Player player) {
        try {
            // Lazy loading LuckPerms API via reflection to avoid hard dependency issues
            Class<?> lpClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object lp = lpClass.getMethod("get").invoke(null);
            Object userManager = lp.getClass().getMethod("getUserManager").invoke(lp);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());
            if (user != null) {
                return (String) user.getClass().getMethod("getPrimaryGroup").invoke(user);
            }
        } catch (Exception ignored) {}
        return "default";
    }
}
