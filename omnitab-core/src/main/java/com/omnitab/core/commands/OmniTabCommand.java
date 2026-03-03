package com.omnitab.core.commands;

import com.omnitab.core.OmniTab;
import com.omnitab.common.language.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OmniTabCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        LanguageManager lm = OmniTab.getInstance().getLanguageManager();
        String prefix = lm.getMessage("prefix");

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.DARK_GRAY + "§m----------------------------------------");
            sender.sendMessage(ChatColor.GOLD + "  OmniTab Universal v" + OmniTab.getInstance().getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "  Developed by " + ChatColor.WHITE + "GamingOP");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.YELLOW + "  /omnitab reload " + ChatColor.GRAY + "- Reloads configuration & languages");
            sender.sendMessage(ChatColor.YELLOW + "  /omnitab help   " + ChatColor.GRAY + "- Displays this help menu");
            sender.sendMessage(ChatColor.DARK_GRAY + "§m----------------------------------------");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("omnitab.admin")) {
                sender.sendMessage(prefix + lm.getMessage("no_permission"));
                return true;
            }
            
            OmniTab.getInstance().reloadPlugin();
            sender.sendMessage(prefix + lm.getMessage("config_reloaded"));
            return true;
        }

        sender.sendMessage(prefix + lm.getMessage("unknown_command"));
        return false;
    }
}
