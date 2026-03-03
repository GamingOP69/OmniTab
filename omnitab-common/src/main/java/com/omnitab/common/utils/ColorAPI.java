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

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<(#[A-Fa-f0-9]{6})>(.*?)</(#[A-Fa-f0-9]{6})>");

    /**
     * Translates color codes, including hex and gradients.
     * @param message The message to translate.
     * @return The translated message.
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return "";

        // 1. Handle Gradients <#RRGGBB>Text</#RRGGBB>
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(message);
        StringBuffer gradientBuffer = new StringBuffer();
        while (gradientMatcher.find()) {
            String startHex = gradientMatcher.group(1).substring(1);
            String content = gradientMatcher.group(2);
            String endHex = gradientMatcher.group(3).substring(1);
            gradientMatcher.appendReplacement(gradientBuffer, applyGradient(content, startHex, endHex));
        }
        gradientMatcher.appendTail(gradientBuffer);
        message = gradientBuffer.toString();

        // 2. Handle Hex Colors (&#RRGGBB)
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            try {
                matcher.appendReplacement(buffer, translateHex(hexCode));
            } catch (Throwable t) {
                matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x" + toLegacyHex(hexCode));
            }
        }
        matcher.appendTail(buffer);

        // 3. Handle Legacy Colors (&)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static String applyGradient(String text, String startHex, String endHex) {
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
            builder.append(translateHex(hex)).append(text.charAt(i));
        }
        return builder.toString();
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
