package de.maximanu.lobbySystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import de.maximanu.lobbySystem.LobbySystem;

import java.util.UUID;

public class BuildCommand implements CommandExecutor {

    private final LobbySystem plugin;

    public BuildCommand(LobbySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp() && !player.hasPermission("lobbysystem.build")) {
            player.sendMessage("§cYou don't have permission to toggle build mode.");
            return true;
        }
        UUID id = player.getUniqueId();
        boolean now = !plugin.getBuildMode(id);
        plugin.setBuildMode(id, now);
        player.sendMessage(now ? "§aBuild mode enabled." : "§cBuild mode disabled.");
        return true;
    }
}