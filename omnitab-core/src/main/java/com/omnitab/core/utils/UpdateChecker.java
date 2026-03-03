package com.omnitab.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * ELITE UPDATE CHECKER
 * Features: Async, Semantic Versioning comparison, caching.
 */
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;
    private String latestVersion;

    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public CompletableFuture<String> check() {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        if (latestVersion != null) {
            future.complete(latestVersion);
            return future;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); 
                 Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    this.latestVersion = scanner.next();
                    future.complete(this.latestVersion);
                }
            } catch (IOException exception) {
                future.completeExceptionally(exception);
            }
        });
        
        return future;
    }

    public boolean isNewer(String current, String latest) {
        if (current == null || latest == null) return false;
        
        String[] currParts = current.split("\\.");
        String[] lateParts = latest.split("\\.");
        
        int length = Math.max(currParts.length, lateParts.length);
        for (int i = 0; i < length; i++) {
            int curr = i < currParts.length ? Integer.parseInt(currParts[i].replaceAll("[^\\d]", "")) : 0;
            int late = i < lateParts.length ? Integer.parseInt(lateParts[i].replaceAll("[^\\d]", "")) : 0;
            
            if (late > curr) return true;
            if (curr > late) return false;
        }
        return false;
    }
}
