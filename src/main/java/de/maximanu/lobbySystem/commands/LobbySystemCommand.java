package de.maximanu.lobbySystem.commands;

import de.maximanu.lobbySystem.LobbySystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class LobbySystemCommand implements CommandExecutor {

    private final LobbySystem plugin;

    public LobbySystemCommand(LobbySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessageService().get("usage.lobbysystem", "Usage: /lobbysystem reload"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!p.isOp() && !p.hasPermission("lobbysystem.reload")) {
                    p.sendMessage(plugin.getMessageService().get("errors.no-permission.reload", "&cYou don't have permission to reload the config."));
                    return true;
                }
            } else if (sender instanceof ConsoleCommandSender) {
                // console allowed
            }

            plugin.reloadPluginConfig();
            plugin.getPlayerListener().refreshAllPlayers();
            sender.sendMessage(plugin.getMessageService().get("info.config-reloaded", "&aConfig reloaded."));
            return true;
        }

        sender.sendMessage(plugin.getMessageService().get("usage.lobbysystem", "Usage: /lobbysystem reload"));
        return true;
    }
}
