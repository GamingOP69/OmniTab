package com.omnitab.adapters.v1_7_R4;

import com.omnitab.api.TablistHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Legacy 1.7.10 Adapter.
 * Note: 1.7.10 does not natively support Header/Footer or sophisticated PlayerInfo packets
 * unless using transitionary Spigot builds (v1_7_R4).
 */
public class HandlerImpl implements TablistHandler {

    private static Class<?> chatSerializer;
    private static Class<?> packetHeaderFooter;
    private static Class<?> packetPlayerInfo;
    private static Class<?> iChatBaseComponent;

    static {
        try {
            String nms = "net.minecraft.server.v1_7_R4.";
            chatSerializer = Class.forName(nms + "IChatBaseComponent$ChatSerializer");
            // Header/Footer was backported to some 1.7.10 builds. We check for existence.
            try {
                packetHeaderFooter = Class.forName(nms + "PacketPlayOutPlayerListHeaderFooter");
            } catch (ClassNotFoundException ignored) {}
            
            packetPlayerInfo = Class.forName(nms + "PacketPlayOutPlayerInfo");
            iChatBaseComponent = Class.forName(nms + "IChatBaseComponent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHeaderFooter(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        if (packetHeaderFooter == null) return; // Not supported on this specific 1.7.10 build

        try {
            Method serialize = chatSerializer.getMethod("a", String.class);
            Object headerComp = serialize.invoke(null, "{\"text\":\"" + header + "\"}");
            Object footerComp = serialize.invoke(null, "{\"text\":\"" + footer + "\"}");

            Object packet = packetHeaderFooter.newInstance();
            Field a = packetHeaderFooter.getDeclaredField("a");
            Field b = packetHeaderFooter.getDeclaredField("b");
            a.setAccessible(true);
            b.setAccessible(true);
            a.set(packet, headerComp);
            b.set(packet, footerComp);

            sendPacket(player, packet);
        } catch (Exception e) {
            // Silently fail if header/footer isn't supported
        }
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        try {
            // In 1.7.10, display names were often handled by teams. 
            // PacketPlayOutPlayerInfo in 1.7.10 was limited to (username, online, ping).
            // This adapter updates the ping via the legacy packet.
            
            // Method 1: Ping Update via Packet
            Object packet = packetPlayerInfo.getConstructor(String.class, boolean.class, int.class)
                    .newInstance(target.getName(), true, ping);
            
            sendPacket(viewer, packet);
            
            // Note: Custom prefixes/suffixes in 1.7.10 are best handled via Scoreboard Teams,
            // which OmniTab already does in SortingRegistry (now with prefixes/suffixes if we restore them for legacy).
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerJoin(@NotNull Player player) {
    }

    @Override
    public void onPlayerQuit(@NotNull Player player) {
    }

    @Override
    public void updateVisibility(@NotNull Player viewer, @NotNull Player target, boolean visible) {
        try {
            Object packet = packetPlayerInfo.getConstructor(String.class, boolean.class, int.class)
                    .newInstance(target.getName(), visible, 0);
            sendPacket(viewer, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(Player player, Object packet) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            Object connection = handle.getClass().getField("playerConnection").get(handle);
            Method send = connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server.v1_7_R4.Packet"));
            send.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
