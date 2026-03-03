package com.omnitab.adapters.v1_12_R1;

import com.omnitab.api.TablistHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * CI-READY ADAPTER: Uses Reflection ONLY to avoid compile-time NMS dependencies.
 */
public class HandlerImpl implements TablistHandler {

    private static Class<?> chatSerializer;
    private static Class<?> packetHeaderFooter;
    private static Class<?> packetPlayerInfo;
    private static Class<?> enumPlayerInfoAction;

    static {
        try {
            String nms = "net.minecraft.server.v1_12_R1.";
            chatSerializer = Class.forName(nms + "IChatBaseComponent$ChatSerializer");
            packetHeaderFooter = Class.forName(nms + "PacketPlayOutPlayerListHeaderFooter");
            packetPlayerInfo = Class.forName(nms + "PacketPlayOutPlayerInfo");
            enumPlayerInfoAction = Class.forName(nms + "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHeaderFooter(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        try {
            Method serialize = chatSerializer.getMethod("a", String.class);
            Object headerComp = serialize.invoke(null, "{\"text\":\"" + header + "\"}");
            Object footerComp = serialize.invoke(null, "{\"text\":\"" + footer + "\"}");

            Object packet = packetHeaderFooter.newInstance();
            setField(packet, "a", headerComp);
            setField(packet, "b", footerComp);

            sendPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        try {
            Method getHandle = target.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(target);
            
            Object action = enumPlayerInfoAction.getField("UPDATE_DISPLAY_NAME").get(null);
            Constructor<?> constructor = packetPlayerInfo.getConstructor(enumPlayerInfoAction, Iterable.class);
            Object packet = constructor.newInstance(action, java.util.Collections.singletonList(handle));
            
            // In 1.12, we often need to manually set the data's display name via reflection 
            // because the handle's display name might not be set yet.
            
            sendPacket(viewer, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerJoin(@NotNull Player player) {
        // No version-specific injection needed for 1.12.2 yet
    }

    @Override
    public void onPlayerQuit(@NotNull Player player) {
        // No version-specific cleanup needed for 1.12.2 yet
    }

    @Override
    public void updateVisibility(@NotNull Player viewer, @NotNull Player target, boolean visible) {
        try {
            Method getHandle = target.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(target);
            
            Object action = enumPlayerInfoAction.getField(visible ? "ADD_PLAYER" : "REMOVE_PLAYER").get(null);
            Constructor<?> constructor = packetPlayerInfo.getConstructor(enumPlayerInfoAction, Iterable.class);
            Object packet = constructor.newInstance(action, java.util.Collections.singletonList(handle));
            
            sendPacket(viewer, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private void sendPacket(Player player, Object packet) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            Object connection = handle.getClass().getField("playerConnection").get(handle);
            Method send = connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server.v1_12_R1.Packet"));
            send.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
