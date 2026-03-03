package com.omnitab.common.utils;

import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * ELITE PING UTILITY
 * Cross-version compatibility for player latency retrieval.
 */
public class PingUtil {

    private static Method getPingMethod;
    private static Method getHandleMethod;
    private static Field pingField;

    static {
        try {
            // Check for modern Spigot/Paper API (1.13+)
            getPingMethod = Player.class.getMethod("getPing");
        } catch (NoSuchMethodException e) {
            // Fallback to NMS for Legacy (1.8 - 1.12)
            try {
                getHandleMethod = Class.forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer").getMethod("getHandle");
            } catch (Exception ex) {
                // Try version-agnostic NMS access if needed, but we mainly support 1.8.8 and 1.12.2 specifically
            }
        }
    }

    public static int getPing(Player player) {
        if (player == null) return 0;

        // Try modern API first
        if (getPingMethod != null) {
            try {
                return (int) getPingMethod.invoke(player);
            } catch (Exception ignored) {}
        }

        // Fallback to Reflection
        try {
            if (getHandleMethod == null) {
                getHandleMethod = player.getClass().getMethod("getHandle");
            }
            Object handle = getHandleMethod.invoke(player);
            
            if (pingField == null) {
                pingField = handle.getClass().getField("ping");
            }
            return pingField.getInt(handle);
        } catch (Exception e) {
            return 0;
        }
    }
}
