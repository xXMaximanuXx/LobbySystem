package de.maximanu.lobbySystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import de.maximanu.lobbySystem.LobbySystem;

public class SetSpawnCommand implements CommandExecutor {

    private final LobbySystem plugin;

    public SetSpawnCommand(LobbySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageService().get("errors.only-players", "&cOnly players can use this command."));
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp() && !player.hasPermission("lobbysystem.set")) {
            player.sendMessage(plugin.getMessageService().get("errors.no-permission.setspawn", "&cYou don't have permission to set spawn."));
            return true;
        }
        plugin.getSpawnService().saveSpawnLocation(player.getLocation());
        player.sendMessage(plugin.getMessageService().get("info.spawn-set", "&aSpawn set."));
        return true;
    }
}
