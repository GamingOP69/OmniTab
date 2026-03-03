package com.omnitab.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface TablistHandler {
    
    /**
     * Updates the header and footer for a specific player.
     */
    void updateHeaderFooter(@NotNull Player player, @NotNull String header, @NotNull String footer);

    /**
     * Updates a player entry for a viewer.
     */
    void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping);

    /**
     * Injects the handler for a player (e.g. packet listeners).
     */
    void onPlayerJoin(@NotNull Player player);

    /**
     * Cleans up for a player.
     */
    void onPlayerQuit(@NotNull Player player);

    /**
     * Updates visibility of a target player for a viewer.
     */
    void updateVisibility(@NotNull Player viewer, @NotNull Player target, boolean visible);
}
