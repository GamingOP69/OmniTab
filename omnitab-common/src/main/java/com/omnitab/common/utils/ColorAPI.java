package com.omnitab.common.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Universal Color API for OmniTab (2026)
 * Supports Legacy Colors, Hex (&#RRGGBB) for 1.16+, and Legacy Fallback.
 */
public class ColorAPI {

    /**
     * Translates color codes, including hex and gradients.
     * @param message The message to translate.
     * @return The translated message.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";

        boolean supportsHex = supportsHex();

        // 1. Handle Gradients <#RRGGBB>Text</#RRGGBB>
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer gradientBuffer = new StringBuffer();
        while (gradientMatcher.find()) {
            String startHex = gradientMatcher.group(1).substring(1);
            String content = gradientMatcher.group(2);
            String endHex = gradientMatcher.group(3).substring(1);
            gradientMatcher.appendReplacement(gradientBuffer, applyGradient(content, startHex, endHex, supportsHex));
        }
        gradientMatcher.appendTail(gradientBuffer);
        message = gradientBuffer.toString();

        // 2. Handle Hex Colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            matcher.appendReplacement(buffer, supportsHex ? translateHex(hexCode) : toLegacyColor(hexCode));
        }
        matcher.appendTail(buffer);

        // 3. Handle Legacy Colors (&)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static boolean supportsHex() {
        try {
            org.bukkit.Bukkit.class.getMethod("getBukkitVersion");
            String version = org.bukkit.Bukkit.getBukkitVersion().split("-")[0];
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return major > 1 || minor >= 16;
        } catch (Exception e) {
            return false;
        }
    }

    private static String applyGradient(String text, String startHex, String endHex, boolean supportsHex) {
        StringBuilder builder = new StringBuilder();
        int length = text.length();
        
        int r1 = Integer.parseInt(startHex.substring(0, 2), 16);
        int g1 = Integer.parseInt(startHex.substring(2, 4), 16);
        int b1 = Integer.parseInt(startHex.substring(4, 6), 16);
        
        int r2 = Integer.parseInt(endHex.substring(0, 2), 16);
        int g2 = Integer.parseInt(endHex.substring(2, 4), 16);
        int b2 = Integer.parseInt(endHex.substring(4, 6), 16);

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (float) (length > 1 ? length - 1 : 1);
            int r = (int) (r1 + ratio * (r2 - r1));
            int g = (int) (g1 + ratio * (g2 - g1));
            int b = (int) (b1 + ratio * (b2 - b1));
            
            String hex = String.format("%02x%02x%02x", r, g, b);
            builder.append(supportsHex ? translateHex(hex) : toLegacyColor(hex)).append(text.charAt(i));
        }
        return builder.toString();
    }

    private static String translateHex(String hexCode) {
        StringBuilder builder = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        for (char c : hexCode.toCharArray()) {
            builder.append(ChatColor.COLOR_CHAR).append(c);
        }
        return builder.toString();
    }

    private static String toLegacyColor(String hexCode) {
        int r = Integer.parseInt(hexCode.substring(0, 2), 16);
        int g = Integer.parseInt(hexCode.substring(2, 4), 16);
        int b = Integer.parseInt(hexCode.substring(4, 6), 16);

        // Nearest Legacy Color mapping
        return getNearestLegacyColor(r, g, b);
    }

    private static String getNearestLegacyColor(int r, int g, int b) {
        // Basic legacy color values for nearest-neighbor match
        int[][] colors = {
            {0, 0, 0}, {0, 0, 170}, {0, 170, 0}, {0, 170, 170},
            {170, 0, 0}, {170, 0, 170}, {255, 170, 0}, {170, 170, 170},
            {85, 85, 85}, {85, 85, 255}, {85, 255, 85}, {85, 255, 255},
            {255, 85, 85}, {255, 85, 255}, {255, 255, 85}, {255, 255, 255}
        };
        char[] codes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        int bestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < colors.length; i++) {
            double distance = Math.pow(r - colors[i][0], 2) + Math.pow(g - colors[i][1], 2) + Math.pow(b - colors[i][2], 2);
            if (distance < minDistance) {
                minDistance = distance;
                bestIndex = i;
            }
        }
        return "&" + codes[bestIndex];
    }
}
