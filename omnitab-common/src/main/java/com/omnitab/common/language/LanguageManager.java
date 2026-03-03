package com.omnitab.common.language;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LanguageManager {

    private final JavaPlugin plugin;
    private FileConfiguration langConfig;
    private String currentLanguage;

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadLanguage(String langCode) {
        this.currentLanguage = langCode;
        String fileName = "messages_" + langCode + ".yml";
        File langFile = new File(plugin.getDataFolder(), fileName);

        if (!langFile.exists()) {
            plugin.saveResource(fileName, false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Load default values from internal resource
        InputStream defLangStream = plugin.getResource(fileName);
        if (defLangStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defLangStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defConfig);
        }
    }

    public String getMessage(String key) {
        if (langConfig == null) return "Lang Error: " + key;
        String message = langConfig.getString(key, "Missing key: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
