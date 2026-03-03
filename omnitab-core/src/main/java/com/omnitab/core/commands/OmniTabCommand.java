package com.omnitab.core.commands;

import com.omnitab.core.OmniTab;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OmniTabCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "OmniTab v" + OmniTab.getInstance().getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Developed with Antigravity AI.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("omnitab.admin")) {
                sender.sendMessage(ChatColor.RED + "No permission!");
                return true;
            }
            
            OmniTab.getInstance().reloadConfig();
            // In a full implementation, we'd re-initialize the AnimationEngine templates here
            sender.sendMessage(ChatColor.GREEN + "OmniTab configuration reloaded!");
            return true;
        }

        return false;
    }
}
