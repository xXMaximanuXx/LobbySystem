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
            sender.sendMessage(plugin.getMessageService().get("errors.only-players", "&cOnly players can use this command."));
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp() && !player.hasPermission("lobbysystem.build")) {
            player.sendMessage(plugin.getMessageService().get("errors.no-permission.build", "&cYou don't have permission to toggle build mode."));
            return true;
        }
        UUID id = player.getUniqueId();
        boolean now = !plugin.getPlayerStateService().getBuildMode(id);
        plugin.getPlayerStateService().setBuildMode(id, now);
        String msg = now
                ? plugin.getMessageService().get("info.build-enabled", "&aBuild mode enabled.")
                : plugin.getMessageService().get("info.build-disabled", "&cBuild mode disabled.");
        player.sendMessage(msg);
        return true;
    }
}
