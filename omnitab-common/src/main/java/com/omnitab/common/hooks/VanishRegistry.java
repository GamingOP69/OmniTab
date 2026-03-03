package com.omnitab.common.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class VanishRegistry {

    private final boolean essentialsVanish;
    private final boolean cmiVanish;
    private final boolean genericVanish;

    public VanishRegistry() {
        this.essentialsVanish = Bukkit.getPluginManager().isPluginEnabled("Essentials");
        this.cmiVanish = Bukkit.getPluginManager().isPluginEnabled("CMI");
        this.genericVanish = true; // Most vanish plugins use metadata 'vanished'
    }

    public boolean isVanished(Player player) {
        if (player == null) return false;

        // Check metadata first (SuperVanish, PremiumVanish, and many others)
        if (player.hasMetadata("vanished")) {
            for (MetadataValue value : player.getMetadata("vanished")) {
                if (value.asBoolean()) return true;
            }
        }

        // EssentialsX hook
        if (essentialsVanish) {
            try {
                // Using reflection or checking metadata if standard vanish
                if (player.hasMetadata("vanished-hidden")) return true;
            } catch (Exception ignored) {}
        }

        return false;
    }
}
