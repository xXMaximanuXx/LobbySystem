package de.maximanu.lobbySystem.service;

import de.maximanu.lobbySystem.LobbySystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStateService {

    private final LobbySystem plugin;
    private final Map<UUID, Integer> hiderStates = new HashMap<>();
    private final Map<UUID, Boolean> buildModes = new HashMap<>();

    public PlayerStateService(LobbySystem plugin) {
        this.plugin = plugin;
    }

    public int getPlayerHiderState(UUID uuid) {
        return hiderStates.getOrDefault(uuid, 0);
    }

    public void setPlayerHiderState(UUID uuid, int state) {
        hiderStates.put(uuid, state);
    }

    public boolean getBuildMode(UUID uuid) {
        return buildModes.getOrDefault(uuid, false);
    }

    public void setBuildMode(UUID uuid, boolean enabled) {
        buildModes.put(uuid, enabled);
    }
}
