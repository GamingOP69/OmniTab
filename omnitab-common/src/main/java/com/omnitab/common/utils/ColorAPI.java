package com.omnitab.common.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Universal Color API for OmniTab (2026)
 * Supports Legacy Colors, Hex (&#RRGGBB) for 1.16+, and Legacy Fallback.
 */
public class ColorAPI {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Translates color codes, including hex.
     * @param message The message to translate.
     * @return The translated message.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";

        // 1. Handle Hex Colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            // If server version is 1.16+ (or supports hex), translate to net.md_5.bungee.api.ChatColor
            // Since we are universal, we attempt to use the hex format if available, 
            // otherwise we fall back to the nearest legacy color or just strip the hex prefix.
            try {
                // We use the Bungee ChatColor for hex if available on classpath
                matcher.appendReplacement(buffer, translateHex(hexCode));
            } catch (Throwable t) {
                // Fallback: Strip the &# and just keep the hex or use &x style legacy hex
                matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x" + toLegacyHex(hexCode));
            }
        }
        matcher.appendTail(buffer);

        // 2. Handle Legacy Colors (&)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String translateHex(String hexCode) {
        // Modern Spigot/Paper approach for Hex
        StringBuilder builder = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        for (char c : hexCode.toCharArray()) {
            builder.append(ChatColor.COLOR_CHAR).append(c);
        }
        return builder.toString();
    }

    private static String toLegacyHex(String hexCode) {
        // Simple fallback for older versions
        StringBuilder builder = new StringBuilder();
        for (char c : hexCode.toCharArray()) {
            builder.append(ChatColor.COLOR_CHAR).append(c);
        }
        return builder.toString();
    }
}
