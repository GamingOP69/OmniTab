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

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "OmniTab v" + OmniTab.getInstance().getDescription().getVersion());
            sender.sendMessage(ChatColor.YELLOW + "Developed with Antigravity AI.");
            sender.sendMessage(lm.getMessage("available_commands"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("omnitab.admin")) {
                sender.sendMessage(prefix + lm.getMessage("no_permission"));
                return true;
            }
            
            OmniTab.getInstance().reloadConfig();
            lm.loadLanguage(OmniTab.getInstance().getConfig().getString("settings.language", "en"));
            
            sender.sendMessage(prefix + lm.getMessage("config_reloaded"));
            return true;
        }

        sender.sendMessage(prefix + lm.getMessage("unknown_command"));
        return false;
    }
}
