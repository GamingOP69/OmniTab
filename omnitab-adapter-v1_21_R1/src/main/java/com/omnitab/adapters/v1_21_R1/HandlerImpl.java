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
        // Modern approach: Paper/Spigot API for player list names
        // If they want packet level, we'd use reflection, but API is safer and works across 1.19+.
        target.setPlayerListName(prefix + target.getName() + suffix);
    }
}
