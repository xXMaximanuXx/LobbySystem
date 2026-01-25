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
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp() && !player.hasPermission("lobbysystem.set")) {
            player.sendMessage("§cYou don't have permission to set spawn.");
            return true;
        }
        plugin.saveSpawnLocation(player.getLocation());
        player.sendMessage("§aSpawn set.");
        return true;
    }
}