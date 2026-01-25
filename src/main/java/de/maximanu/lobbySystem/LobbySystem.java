package de.maximanu.lobbySystem;

import de.maximanu.lobbySystem.commands.LobbySystemCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import de.maximanu.lobbySystem.commands.SetSpawnCommand;
import de.maximanu.lobbySystem.commands.SpawnCommand;
import de.maximanu.lobbySystem.commands.BuildCommand;
import de.maximanu.lobbySystem.listener.PlayerListener;

import java.util.UUID;

public final class LobbySystem extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        saveDefaultConfig();
        if (getCommand("spawn") != null) getCommand("spawn").setExecutor(new SpawnCommand(this));
        if (getCommand("setspawn") != null) getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        if (getCommand("build") != null) getCommand("build").setExecutor(new BuildCommand(this));
        if (getCommand("lobbysystem") != null) getCommand("lobbysystem").setExecutor(new LobbySystemCommand(this));
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        getLogger().info("LobbySystem enabled");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("LobbySystem disabled");
    }

    public void saveSpawnLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        FileConfiguration cfg = getConfig();
        cfg.set("spawn.world", loc.getWorld().getName());
        cfg.set("spawn.x", loc.getX());
        cfg.set("spawn.y", loc.getY());
        cfg.set("spawn.z", loc.getZ());
        cfg.set("spawn.yaw", loc.getYaw());
        cfg.set("spawn.pitch", loc.getPitch());
        saveConfig();
    }

    public Location getSpawnLocation() {
        FileConfiguration cfg = getConfig();
        String world = cfg.getString("spawn.world", "").trim();
        if (world.isEmpty()) return null;
        if (getServer().getWorld(world) == null) return null;
        double x = cfg.getDouble("spawn.x");
        double y = cfg.getDouble("spawn.y");
        double z = cfg.getDouble("spawn.z");
        float yaw = (float) cfg.getDouble("spawn.yaw");
        float pitch = (float) cfg.getDouble("spawn.pitch");
        return new Location(getServer().getWorld(world), x, y, z, yaw, pitch);
    }

    public int getPlayerHiderState(UUID uuid) {
        return getConfig().getInt("playerhider." + uuid.toString(), 0);
    }

    public void setPlayerHiderState(UUID uuid, int state) {
        getConfig().set("playerhider." + uuid.toString(), state);
        saveConfig();
    }

    public boolean getBuildMode(UUID uuid) {
        return getConfig().getBoolean("build." + uuid.toString(), false);
    }

    public void setBuildMode(UUID uuid, boolean enabled) {
        getConfig().set("build." + uuid.toString(), enabled);
        saveConfig();
    }
}