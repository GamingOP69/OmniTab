package com.omnitab.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * ELITE UPDATE CHECKER & AUTO-UPDATER
 * Features: GitHub API integration, Async Downloading, safe 'update' folder placement.
 */
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String repo; // Format: "Owner/Repo"
    private String latestVersion;
    private String downloadUrl;

    public UpdateChecker(JavaPlugin plugin, String repo) {
        this.plugin = plugin;
        this.repo = repo;
    }

    public CompletableFuture<String> check() {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        if (latestVersion != null) {
            future.complete(latestVersion);
            return future;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/repos/" + repo + "/releases/latest").openConnection();
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                
                try (InputStream inputStream = connection.getInputStream(); 
                     Scanner scanner = new Scanner(inputStream)) {
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) response.append(scanner.nextLine());
                    
                    String body = response.toString();
                    // Basic JSON parsing without external dependencies
                    this.latestVersion = body.split("\"tag_name\":\"")[1].split("\"")[0];
                    
                    // Find the first .jar asset
                    if (body.contains("browser_download_url")) {
                        String[] assets = body.split("\"browser_download_url\":\"");
                        for (int i = 1; i < assets.length; i++) {
                            String url = assets[i].split("\"")[0];
                            if (url.endsWith(".jar")) {
                                this.downloadUrl = url;
                                break;
                            }
                        }
                    }
                    
                    future.complete(this.latestVersion);
                }
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        
        return future;
    }

    public CompletableFuture<Boolean> download(CommandSender sender) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (downloadUrl == null) {
            future.complete(false);
            return future;
        }

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            File updateFolder = new File(plugin.getDataFolder().getParentFile(), "update");
            if (!updateFolder.exists()) updateFolder.mkdirs();
            
            File targetFile = new File(updateFolder, "OmniTab.jar");
            
            try (BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                byte[] dataBuffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                future.complete(true);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to download update", e);
                future.complete(false);
            }
        });
        
        return future;
    }

    public boolean isNewer(String current, String latest) {
        if (current == null || latest == null) return false;
        
        String[] currParts = current.replace("v", "").split("\\.");
        String[] lateParts = latest.replace("v", "").split("\\.");
        
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
