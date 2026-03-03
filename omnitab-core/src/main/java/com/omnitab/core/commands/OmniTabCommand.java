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
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("omnitab.admin")) {
                    sender.sendMessage(prefix + lm.getMessage("no_permission"));
                    return true;
                }
                OmniTab.getInstance().reloadPlugin();
                sender.sendMessage(prefix + lm.getMessage("config_reloaded"));
                break;

            case "debug":
                if (!sender.hasPermission("omnitab.admin")) {
                    sender.sendMessage(prefix + lm.getMessage("no_permission"));
                    return true;
                }
                boolean currentDebug = OmniTab.getInstance().getConfig().getBoolean("settings.debug_mode", false);
                OmniTab.getInstance().getConfig().set("settings.debug_mode", !currentDebug);
                OmniTab.getInstance().saveConfig();
                sender.sendMessage(prefix + "§7Debug mode is now " + (!currentDebug ? "§aEnabled" : "§cDisabled"));
                break;

            case "version":
                sender.sendMessage(prefix + "§7Running OmniTab §f" + OmniTab.getInstance().getDescription().getVersion());
                sender.sendMessage(prefix + "§7Developed and Maintained by §6GamingOP");
                break;

            default:
                sender.sendMessage(prefix + lm.getMessage("unknown_command"));
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage("§6§lOmniTab Universal §7v" + OmniTab.getInstance().getDescription().getVersion());
        sender.sendMessage("§7Developed by §fGamingOP");
        sender.sendMessage("");
        sender.sendMessage("§e/omnitab reload §7- Reload configuration");
        sender.sendMessage("§e/omnitab debug  §7- Toggle debug mode");
        sender.sendMessage("§e/omnitab version §7- Show version info");
        sender.sendMessage("§e/omnitab help    §7- Display this menu");
        sender.sendMessage("§8§m----------------------------------------");
    }
}
