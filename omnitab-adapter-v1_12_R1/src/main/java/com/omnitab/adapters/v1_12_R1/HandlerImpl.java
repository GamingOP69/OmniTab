package com.omnitab.adapters.v1_12_R1;

import com.omnitab.api.TablistHandler;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class HandlerImpl implements TablistHandler {

    private Field headerField;
    private Field footerField;

    public HandlerImpl() {
        try {
            headerField = PacketPlayOutPlayerListHeaderFooter.class.getDeclaredField("a");
            footerField = PacketPlayOutPlayerListHeaderFooter.class.getDeclaredField("b");
            headerField.setAccessible(true);
            footerField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHeaderFooter(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        IChatBaseComponent headerComp = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + header + "\"}");
        IChatBaseComponent footerComp = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}");

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        
        try {
            if (headerField != null && footerField != null) {
                headerField.set(packet, headerComp);
                footerField.set(packet, footerComp);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        IChatBaseComponent displayName = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + prefix + target.getName() + suffix + "\"}");
        
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, 
                ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) target).getHandle()
        );

        try {
            Field bField = packet.getClass().getDeclaredField("b");
            bField.setAccessible(true);
            java.util.List<PacketPlayOutPlayerInfo.PlayerInfoData> list = (java.util.List<PacketPlayOutPlayerInfo.PlayerInfoData>) bField.get(packet);
            
            // Re-create the data with the display name for the first entry
            if (!list.isEmpty()) {
                PacketPlayOutPlayerInfo.PlayerInfoData data = list.get(0);
                PacketPlayOutPlayerInfo.PlayerInfoData newData = new PacketPlayOutPlayerInfo.PlayerInfoData(
                        data.a(), data.b(), data.c(), displayName);
                list.set(0, newData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
        
        // Also update latency in a separate packet for simplicity or combine actions
        PacketPlayOutPlayerInfo latencyPacket = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY,
                ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) target).getHandle()
        );
        ((org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer) viewer).getHandle().playerConnection.sendPacket(latencyPacket);
    }

    @Override
    public void onPlayerJoin(@NotNull Player player) {}

    @Override
    public void onPlayerQuit(@NotNull Player player) {}
}
