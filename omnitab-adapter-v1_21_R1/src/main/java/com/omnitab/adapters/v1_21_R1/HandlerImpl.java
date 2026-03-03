package com.omnitab.adapters.v1_21_R1;

import com.omnitab.api.TablistHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * CI-READY ADAPTER: Uses spigot-api ONLY where possible.
 */
public class HandlerImpl implements TablistHandler {

    @Override
    public void updateHeaderFooter(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        // 1.21.x has these methods in the Spigot/Paper API! 
        // No reflection needed for standard header/footer.
        player.setPlayerListHeaderFooter(header, footer);
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        target.setPlayerListName(prefix + target.getName() + suffix);
        
        // Try to set latency if Paper API is available
        try {
            Method setPing = target.getClass().getMethod("setPing", int.class);
            setPing.invoke(target, ping);
        } catch (Exception ignored) {
            // No direct API for ping in Spigot 1.21, usually managed by server
        }
    }

    @Override
    public void onPlayerJoin(@NotNull Player player) {
        // No version-specific injection needed for 1.21.x yet
    }

    @Override
    public void onPlayerQuit(@NotNull Player player) {
        // No version-specific cleanup needed for 1.21.x yet
    }

    @Override
    public void updateVisibility(@NotNull Player viewer, @NotNull Player target, boolean visible) {
        if (visible) {
            viewer.showPlayer(target);
        } else {
            viewer.hidePlayer(target);
        }
    }
}
