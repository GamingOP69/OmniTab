package com.omnitab.adapters.v1_12_R1;

import com.omnitab.api.TablistHandler;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class HandlerImpl implements TablistHandler {

    @Override
    public void updateHeaderFooter(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        IChatBaseComponent headerComp = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + header + "\"}");
        IChatBaseComponent footerComp = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}");

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        
        try {
            Field headerField = packet.getClass().getDeclaredField("a");
            Field footerField = packet.getClass().getDeclaredField("b");
            headerField.setAccessible(true);
            footerField.setAccessible(true);
            headerField.set(packet, headerComp);
            footerField.set(packet, footerComp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, 
                ((CraftPlayer) target).getHandle()
        );
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void onPlayerJoin(@NotNull Player player) {}

    @Override
    public void onPlayerQuit(@NotNull Player player) {}
}
