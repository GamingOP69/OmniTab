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
        Component headerComp = Component.Serializer.fromJson(header, player.getServer().getScoreboardManager().getMainScoreboard().getObjective("dummy").getScoreboard().getHandle().registryAccess()); // Simplified for example, normally use Adventure
        Component footerComp = Component.Serializer.fromJson(footer, null); 

        ClientboundTabListPacket packet = new ClientboundTabListPacket(
                Component.literal(header), 
                Component.literal(footer)
        );
        
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void updatePlayerEntry(@NotNull Player viewer, @NotNull Player target, String prefix, String suffix, int ping) {
        // 1.19.3+ uses ClientboundPlayerInfoUpdatePacket
        // We'd need to create a dummy server player or update existing one
        // For simplicity in this demo, we use the literal update
        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY
        );
        
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
                actions, 
                ((CraftPlayer) target).getHandle()
        );
        
        ((CraftPlayer) viewer).getHandle().connection.send(packet);
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
