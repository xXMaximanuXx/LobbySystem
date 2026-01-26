package de.maximanu.lobbySystem.service;

import de.maximanu.lobbySystem.LobbySystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VisibilityService {

    private final LobbySystem plugin;

    public VisibilityService(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public void applyVisibility(Player player, int state) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(player)) continue;
            if (state == 0) {
                player.showPlayer(plugin, other);
            } else if (state == 1) {
                if (other.isOp()) player.showPlayer(plugin, other);
                else player.hidePlayer(plugin, other);
            } else {
                player.hidePlayer(plugin, other);
            }
        }
    }

    public void applyVisibilityToTarget(Player viewer, Player target, int state) {
        if (viewer.equals(target)) return;
        if (state == 0) {
            viewer.showPlayer(plugin, target);
        } else if (state == 1) {
            if (target.isOp()) viewer.showPlayer(plugin, target);
            else viewer.hidePlayer(plugin, target);
        } else {
            viewer.hidePlayer(plugin, target);
        }
    }
}
