package com.omnitab.core.commands;

import com.omnitab.core.OmniTab;
import com.omnitab.common.language.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OmniTabCommand implements CommandExecutor, TabCompleter {
    
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
                sender.sendMessage(prefix + ChatColor.GRAY + "Debug mode is now " + (!currentDebug ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
                break;

            case "version":
                sender.sendMessage(prefix + ChatColor.GRAY + "Running OmniTab " + ChatColor.WHITE + OmniTab.getInstance().getDescription().getVersion());
                sender.sendMessage(prefix + ChatColor.GRAY + "Developed and Maintained by " + ChatColor.GOLD + "GamingOP");
                break;

            case "update":
                if (!sender.hasPermission("omnitab.admin")) {
                    sender.sendMessage(prefix + lm.getMessage("no_permission"));
                    return true;
                }
                sender.sendMessage(prefix + ChatColor.GRAY + "Checking for updates...");
                com.omnitab.core.utils.UpdateChecker checker = new com.omnitab.core.utils.UpdateChecker(OmniTab.getInstance(), "GamingOP69/OmniTab");
                checker.check().thenAccept(latest -> {
                    if (checker.isNewer(OmniTab.getInstance().getDescription().getVersion(), latest)) {
                        sender.sendMessage(prefix + ChatColor.GREEN + "A new update is available: " + ChatColor.WHITE + latest);
                        sender.sendMessage(prefix + ChatColor.YELLOW + "Downloading update...");
                        checker.download(sender).thenAccept(success -> {
                            if (success) {
                                sender.sendMessage(prefix + ChatColor.GREEN + "Update downloaded! Finish by restarting the server.");
                            } else {
                                sender.sendMessage(prefix + ChatColor.RED + "Download failed. Check console for details.");
                            }
                        });
                    } else {
                        sender.sendMessage(prefix + ChatColor.GREEN + "You are running the latest version.");
                    }
                }).exceptionally(ex -> {
                    sender.sendMessage(prefix + ChatColor.RED + "Failed to check for updates.");
                    return null;
                });
                break;

            default:
                sender.sendMessage(prefix + lm.getMessage("unknown_command"));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("omnitab.admin")) return Collections.emptyList();
        
        if (args.length == 1) {
            return Arrays.asList("reload", "debug", "version", "update", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
        sender.sendMessage(ChatColor.GOLD + "OmniTab Universal " + ChatColor.GRAY + "v" + OmniTab.getInstance().getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "Developed by " + ChatColor.WHITE + "GamingOP");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/omnitab reload " + ChatColor.GRAY + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/omnitab debug  " + ChatColor.GRAY + "- Toggle debug mode");
        sender.sendMessage(ChatColor.YELLOW + "/omnitab update " + ChatColor.GRAY + "- Check for updates");
        sender.sendMessage(ChatColor.YELLOW + "/omnitab version " + ChatColor.GRAY + "- Show version info");
        sender.sendMessage(ChatColor.YELLOW + "/omnitab help    " + ChatColor.GRAY + "- Display this menu");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m----------------------------------------"));
    }
}
