package com.omnitab.adapters.v1_8_R3;

import com.omnitab.api.TablistHandler;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class HandlerImpl implements TablistHandler {

    private Field footerField;

    public HandlerImpl() {
        try {
            footerField = PacketPlayOutPlayerListHeaderFooter.class.getDeclaredField("b");
            footerField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHeaderFooter(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        IChatBaseComponent headerComp = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + header + "\"}");
        IChatBaseComponent footerComp = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}");

        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(headerComp);
        
        try {
            if (footerField != null) {
                footerField.set(packet, footerComp);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        // In 1.8.8, custom prefixes/suffixes are best handled via Scoreboard Teams
        // This handler interacts with the player info packet for ping
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, 
                ((CraftPlayer) target).getHandle()
        );
        
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public void onPlayerJoin(@NotNull Player player) {
        // Initialization for 1.8.8
    }

    @Override
    public void onPlayerQuit(@NotNull Player player) {
        // Cleanup
    }
}
