package de.maximanu.lobbySystem;

import de.maximanu.lobbySystem.commands.BuildCommand;
import de.maximanu.lobbySystem.commands.LobbySystemCommand;
import de.maximanu.lobbySystem.commands.SetSpawnCommand;
import de.maximanu.lobbySystem.commands.SpawnCommand;
import de.maximanu.lobbySystem.config.ConfigService;
import de.maximanu.lobbySystem.listener.PlayerListener;
import de.maximanu.lobbySystem.menu.ServerSelectorMenu;
import de.maximanu.lobbySystem.service.HotbarService;
import de.maximanu.lobbySystem.service.MessageService;
import de.maximanu.lobbySystem.service.PlayerStateService;
import de.maximanu.lobbySystem.service.SpawnService;
import de.maximanu.lobbySystem.service.VisibilityService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbySystem extends JavaPlugin {

    private PlayerListener playerListener;
    private MessageService messageService;
    private ConfigService configService;
    private HotbarService hotbarService;
    private ServerSelectorMenu serverSelectorMenu;
    private PlayerStateService playerStateService;
    private SpawnService spawnService;
    private VisibilityService visibilityService;

    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        saveDefaultConfig();
        messageService = new MessageService(this);
        configService = new ConfigService(this, messageService);
        playerStateService = new PlayerStateService(this);
        spawnService = new SpawnService(this);
        visibilityService = new VisibilityService(this);
        hotbarService = new HotbarService(this);
        serverSelectorMenu = new ServerSelectorMenu(this, configService);
        if (getCommand("spawn") != null) getCommand("spawn").setExecutor(new SpawnCommand(this));
        if (getCommand("setspawn") != null) getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        if (getCommand("build") != null) getCommand("build").setExecutor(new BuildCommand(this));
        if (getCommand("lobbysystem") != null) getCommand("lobbysystem").setExecutor(new LobbySystemCommand(this));
        playerListener = new PlayerListener(this);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        getLogger().info("LobbySystem enabled");
    }

    @Override
    public void onDisable() {
        saveConfig();
        getLogger().info("LobbySystem disabled");
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        saveDefaultConfig();
        messageService.reload();
        configService.reload();
        hotbarService.reload();
        serverSelectorMenu.reloadMessages();
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public HotbarService getHotbarService() {
        return hotbarService;
    }

    public ServerSelectorMenu getServerSelectorMenu() {
        return serverSelectorMenu;
    }

    public PlayerStateService getPlayerStateService() {
        return playerStateService;
    }

    public SpawnService getSpawnService() {
        return spawnService;
    }

    public VisibilityService getVisibilityService() {
        return visibilityService;
    }
}
