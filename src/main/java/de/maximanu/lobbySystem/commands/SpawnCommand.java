package de.maximanu.lobbySystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import de.maximanu.lobbySystem.LobbySystem;

public class SpawnCommand implements CommandExecutor {

    private final LobbySystem plugin;

    public SpawnCommand(LobbySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageService().get("errors.only-players", "&cOnly players can use this command."));
            return true;
        }
        Player player = (Player) sender;
        Location spawn = plugin.getSpawnService().getSpawnLocation();
        if (spawn == null) {
            player.sendMessage(plugin.getMessageService().get("errors.spawn-not-set", "&cSpawn is not set."));
            return true;
        }
        player.teleport(spawn);
        player.sendMessage(plugin.getMessageService().get("info.spawn-teleport", "&aYou have been teleported to the spawn point."));
        return true;
    }
}
