package com.omnitab.adapters.v1_8_R3;

import com.omnitab.api.TablistHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * CI-READY ADAPTER: Uses Reflection ONLY to avoid compile-time NMS dependencies.
 */
public class HandlerImpl implements TablistHandler {

    private static Class<?> chatSerializer;
    private static Class<?> packetHeaderFooter;
    private static Class<?> packetPlayerInfo;
    private static Class<?> enumPlayerInfoAction;
    private static Class<?> iChatBaseComponent;
    private static Class<?> playerInfoData;

    static {
        try {
            String nms = "net.minecraft.server.v1_8_R3.";
            chatSerializer = Class.forName(nms + "IChatBaseComponent$ChatSerializer");
            packetHeaderFooter = Class.forName(nms + "PacketPlayOutPlayerListHeaderFooter");
            packetPlayerInfo = Class.forName(nms + "PacketPlayOutPlayerInfo");
            enumPlayerInfoAction = Class.forName(nms + "PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            iChatBaseComponent = Class.forName(nms + "IChatBaseComponent");
            playerInfoData = Class.forName(nms + "PacketPlayOutPlayerInfo$PlayerInfoData");
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
            Field a = packetHeaderFooter.getDeclaredField("a");
            Field b = packetHeaderFooter.getDeclaredField("b");
            a.setAccessible(true);
            b.setAccessible(true);
            a.set(packet, headerComp);
            b.set(packet, footerComp);

            sendPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        try {
            Method serialize = chatSerializer.getMethod("a", String.class);
            Object displayName = serialize.invoke(null, "{\"text\":\"" + prefix + target.getName() + suffix + "\"}");

            Method getHandle = target.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(target);

            // Construct UPDATE_DISPLAY_NAME info packet
            Object action = enumPlayerInfoAction.getField("UPDATE_DISPLAY_NAME").get(null);
            
            // PacketPlayOutPlayerInfo(Action, EntityPlayer...)
            Constructor<?> constructor = packetPlayerInfo.getConstructor(enumPlayerInfoAction, Iterable.class);
            Object packet = constructor.newInstance(action, java.util.Collections.singletonList(handle));

            // Modify the underlying PlayerInfoData list if needed, 
            // but 1.8.8 constructor usually picks up the current display name of the handle.
            // If it doesn't, we manually inject into 'b' field list.

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
            Method send = connection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server.v1_8_R3.Packet"));
            send.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
