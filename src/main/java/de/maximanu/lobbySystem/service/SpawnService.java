package de.maximanu.lobbySystem.service;

import de.maximanu.lobbySystem.LobbySystem;
import org.bukkit.Location;

public class SpawnService {

    private final LobbySystem plugin;

    public SpawnService(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public void saveSpawnLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        plugin.getConfig().set("spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("spawn.x", loc.getX());
        plugin.getConfig().set("spawn.y", loc.getY());
        plugin.getConfig().set("spawn.z", loc.getZ());
        plugin.getConfig().set("spawn.yaw", loc.getYaw());
        plugin.getConfig().set("spawn.pitch", loc.getPitch());
        plugin.saveConfig();
        plugin.getConfigService().reload();
    }

    public Location getSpawnLocation() {
        String world = plugin.getConfig().getString("spawn.world", "").trim();
        if (world.isEmpty()) return null;
        if (plugin.getServer().getWorld(world) == null) return null;
        double x = plugin.getConfig().getDouble("spawn.x");
        double y = plugin.getConfig().getDouble("spawn.y");
        double z = plugin.getConfig().getDouble("spawn.z");
        float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");
        return new Location(plugin.getServer().getWorld(world), x, y, z, yaw, pitch);
    }
}
