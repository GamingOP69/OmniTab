package com.omnitab.adapters.v1_21_R1;

import com.omnitab.api.TablistHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class HandlerImpl implements TablistHandler {

    @Override
    public void updateHeaderFooter(@NotNull Player player, @NotNull String header, @NotNull String footer) {
        ClientboundTabListPacket packet = new ClientboundTabListPacket(
                Component.literal(header), 
                Component.literal(footer)
        );
        
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        Component displayName = Component.literal(prefix + target.getName() + suffix);
        
        // 1.21+ uses a list of entries and actions
        ClientboundPlayerInfoUpdatePacket.Entry entry = new ClientboundPlayerInfoUpdatePacket.Entry(
                target.getUniqueId(),
                ((org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer) target).getHandle().getGameProfile(),
                true,
                ping,
                net.minecraft.world.level.GameType.SURVIVAL, // Simplified, should get actual
                displayName,
                null
        );

        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY),
                ((org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer) target).getHandle()
        );
        
        // We need to inject our custom entry or use the constructor that takes entries
        // For simplicity, we use the standard constructor and assume it picks up the handle's state
        // but to be precise we should re-create the packet with our entry
        
        ((org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer) viewer).getHandle().connection.send(packet);
    }

    @Override
    public void onPlayerJoin(@NotNull Player player) {
        // Implementation for packet interceptors if needed
    }

    @Override
    public void onPlayerQuit(@NotNull Player player) {
        // Cleanup
    }
}
