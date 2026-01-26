package de.maximanu.lobbySystem.config;

import org.bukkit.Material;

import java.util.List;

public final class ServerEntry {

    private final String displayName;
    private final String bungeeName;
    private final Material material;
    private final List<String> lore;
    private final int slot;

    public ServerEntry(String displayName, String bungeeName, Material material, List<String> lore, int slot) {
        this.displayName = displayName;
        this.bungeeName = bungeeName;
        this.material = material;
        this.lore = lore;
        this.slot = slot;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBungeeName() {
        return bungeeName;
    }

    public Material getMaterial() {
        return material;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getSlot() {
        return slot;
    }
}
