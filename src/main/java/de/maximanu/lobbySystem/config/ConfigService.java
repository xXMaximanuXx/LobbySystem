package de.maximanu.lobbySystem.config;

import de.maximanu.lobbySystem.LobbySystem;
import de.maximanu.lobbySystem.service.MessageService;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigService {

    private final LobbySystem plugin;
    private final MessageService messageService;
    private String spawnWorldName;
    private String lobbyWorldName;
    private boolean teleportOnJoin;
    private boolean teleportOnRespawn;
    private boolean teleportOnVoid;
    private boolean protectAllWorlds;
    private boolean protectDamage;
    private boolean protectHunger;
    private boolean protectBlockBreak;
    private boolean protectBlockPlace;
    private boolean protectInteract;
    private boolean protectEntityInteract;
    private boolean protectInventory;
    private boolean protectItemDrop;
    private boolean hotbarLockEnabled;
    private boolean hotbarEnabled;
    private boolean hotbarAllWorlds;
    private Map<String, String> links;
    private List<ServerEntry> serverEntries;
    private Map<String, Integer> hotbarSlots;
    private Map<String, Material> hotbarMaterials;
    private int selectorSize;
    private List<Integer> selectorLayoutSlots;
    private Material selectorFillerMaterial;
    private int selectorPrevSlot;
    private int selectorNextSlot;
    private boolean selectorFillEmpty;

    public ConfigService(LobbySystem plugin, MessageService messageService) {
        this.plugin = plugin;
        this.messageService = messageService;
        reload();
    }

    public String getSpawnWorldName() {
        return spawnWorldName;
    }

    public String getLobbyWorldName() {
        return lobbyWorldName;
    }

    public boolean isTeleportOnJoin() {
        return teleportOnJoin;
    }

    public boolean isTeleportOnRespawn() {
        return teleportOnRespawn;
    }

    public boolean isTeleportOnVoid() {
        return teleportOnVoid;
    }

    public boolean isProtectAllWorlds() {
        return protectAllWorlds;
    }

    public boolean isProtectDamage() {
        return protectDamage;
    }

    public boolean isProtectHunger() {
        return protectHunger;
    }

    public boolean isProtectBlockBreak() {
        return protectBlockBreak;
    }

    public boolean isProtectBlockPlace() {
        return protectBlockPlace;
    }

    public boolean isProtectInteract() {
        return protectInteract;
    }

    public boolean isProtectEntityInteract() {
        return protectEntityInteract;
    }

    public boolean isProtectInventory() {
        return protectInventory;
    }

    public boolean isProtectItemDrop() {
        return protectItemDrop;
    }

    public boolean isHotbarLockEnabled() {
        return hotbarLockEnabled;
    }

    public boolean isHotbarEnabled() {
        return hotbarEnabled;
    }

    public boolean isHotbarAllWorlds() {
        return hotbarAllWorlds;
    }

    public List<ServerEntry> getServerEntries() {
        return serverEntries;
    }

    public int getHotbarSlot(String key, int fallback) {
        return hotbarSlots.getOrDefault(key, fallback);
    }

    public Material getHotbarMaterial(String key, Material fallback) {
        return hotbarMaterials.getOrDefault(key, fallback);
    }

    public int getSelectorSize() {
        return selectorSize;
    }

    public List<Integer> getSelectorLayoutSlots() {
        return selectorLayoutSlots;
    }

    public Material getSelectorFillerMaterial() {
        return selectorFillerMaterial;
    }

    public int getSelectorPrevSlot() {
        return selectorPrevSlot;
    }

    public int getSelectorNextSlot() {
        return selectorNextSlot;
    }

    public boolean isSelectorFillEmpty() {
        return selectorFillEmpty;
    }

    public String getLink(String key, String fallback) {
        return links.getOrDefault(key, fallback);
    }

    public void reload() {
        spawnWorldName = plugin.getConfig().getString("spawn.world", "").trim();
        lobbyWorldName = plugin.getConfig().getString("lobby.world", "").trim();
        if (lobbyWorldName.isEmpty()) {
            lobbyWorldName = spawnWorldName;
        }

        teleportOnJoin = plugin.getConfig().getBoolean("lobby.teleport-on-join", true);
        teleportOnRespawn = plugin.getConfig().getBoolean("lobby.teleport-on-respawn", true);
        teleportOnVoid = plugin.getConfig().getBoolean("lobby.teleport-on-void", true);
        protectAllWorlds = plugin.getConfig().getBoolean("lobby.protect.apply-to-all-worlds", true);
        protectDamage = plugin.getConfig().getBoolean("lobby.protect.damage", true);
        protectHunger = plugin.getConfig().getBoolean("lobby.protect.hunger", true);
        protectBlockBreak = plugin.getConfig().getBoolean("lobby.protect.block-break", true);
        protectBlockPlace = plugin.getConfig().getBoolean("lobby.protect.block-place", true);
        protectInteract = plugin.getConfig().getBoolean("lobby.protect.interact", true);
        protectEntityInteract = plugin.getConfig().getBoolean("lobby.protect.entity-interact", true);
        protectInventory = plugin.getConfig().getBoolean("lobby.protect.inventory", true);
        protectItemDrop = plugin.getConfig().getBoolean("lobby.protect.item-drop", true);
        hotbarLockEnabled = plugin.getConfig().getBoolean("hotbar.lock", true);
        hotbarEnabled = plugin.getConfig().getBoolean("hotbar.enabled", true);
        hotbarAllWorlds = plugin.getConfig().getBoolean("hotbar.apply-to-all-worlds", false);

        Map<String, String> linkMap = new HashMap<>();
        linkMap.put("website", plugin.getConfig().getString("links.website", "https://example.com"));
        linkMap.put("discord", plugin.getConfig().getString("links.discord", "https://discord.gg/example"));
        linkMap.put("store", plugin.getConfig().getString("links.store", "https://store.example.com"));
        links = linkMap;

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("servers");
        List<ServerEntry> entries = new ArrayList<>();
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                ConfigurationSection server = sec.getConfigurationSection(key);
                if (server == null) continue;
                String bungee = server.getString("bungee", "").trim();
                if (bungee.isEmpty()) continue;
                String display = messageService.toLegacy(server.getString("name", key));
                String itemName = server.getString("item", "PAPER");
                Material mat = Material.matchMaterial(itemName);
                if (mat == null) mat = Material.PAPER;
                int slot = server.getInt("slot", -1);
                List<String> customLore = server.getStringList("lore");
                List<String> lore;
                if (customLore.isEmpty()) {
                    lore = messageService.getList(
                            "menu.selector.item.default-lore",
                            List.of("&7Click to connect")
                    );
                } else {
                    lore = messageService.toLegacyList(customLore);
                }
                List<String> finalLore = new ArrayList<>(lore);
                entries.add(new ServerEntry(display, bungee, mat, finalLore, slot));
            }
        }
        serverEntries = Collections.unmodifiableList(entries);

        hotbarSlots = new HashMap<>();
        hotbarSlots.put("info", normalizeHotbarSlot("hotbar.info.slot", 0));
        hotbarSlots.put("selector", normalizeHotbarSlot("hotbar.selector.slot", 4));
        hotbarSlots.put("hider", normalizeHotbarSlot("hotbar.hider.slot", 8));
        warnDuplicateHotbarSlots();

        hotbarMaterials = new HashMap<>();
        hotbarMaterials.put("info", materialOrDefault(plugin.getConfig().getString("hotbar.info.material"), Material.BOOK));
        hotbarMaterials.put("selector", materialOrDefault(plugin.getConfig().getString("hotbar.selector.material"), Material.NETHER_STAR));
        hotbarMaterials.put("hider", materialOrDefault(plugin.getConfig().getString("hotbar.hider.material"), Material.PLAYER_HEAD));

        selectorSize = normalizeMenuSize(plugin.getConfig().getInt("menu.selector.size", 27));
        selectorLayoutSlots = normalizeSlots(plugin.getConfig().getIntegerList("menu.selector.layout-slots"), selectorSize);
        selectorFillerMaterial = materialOrDefault(plugin.getConfig().getString("menu.selector.filler-material"), Material.GRAY_STAINED_GLASS_PANE);
        selectorFillEmpty = plugin.getConfig().getBoolean("menu.selector.fill-empty", true);
        int defaultPrev = Math.max(0, selectorSize - 9);
        int defaultNext = selectorSize - 1;
        selectorPrevSlot = normalizeMenuSlot("menu.selector.prev-slot", defaultPrev, selectorSize);
        selectorNextSlot = normalizeMenuSlot("menu.selector.next-slot", defaultNext, selectorSize);
        if (selectorPrevSlot == selectorNextSlot && selectorPrevSlot != -1) {
            selectorNextSlot = defaultNext != selectorPrevSlot ? defaultNext : -1;
        }
    }

    private int normalizeMenuSize(int size) {
        if (size < 9) return 9;
        if (size > 54) return 54;
        return size - (size % 9);
    }

    private List<Integer> normalizeSlots(List<Integer> slots, int size) {
        if (slots == null || slots.isEmpty()) return List.of();
        List<Integer> valid = new ArrayList<>();
        for (Integer slot : slots) {
            if (slot == null) continue;
            if (slot < 0 || slot >= size) continue;
            if (!valid.contains(slot)) valid.add(slot);
        }
        return Collections.unmodifiableList(valid);
    }

    private Material materialOrDefault(String name, Material fallback) {
        if (name == null || name.isBlank()) return fallback;
        Material mat = Material.matchMaterial(name.trim());
        return mat != null ? mat : fallback;
    }

    private int normalizeHotbarSlot(String path, int fallback) {
        int slot = plugin.getConfig().getInt(path, fallback);
        if (slot < 0 || slot > 8) {
            plugin.getLogger().warning("Invalid hotbar slot '" + slot + "' at " + path + ". Using " + fallback + ".");
            return fallback;
        }
        return slot;
    }

    private int normalizeMenuSlot(String path, int fallback, int size) {
        int slot = plugin.getConfig().getInt(path, fallback);
        if (slot < 0) return -1;
        if (slot >= size) {
            plugin.getLogger().warning("Invalid menu slot '" + slot + "' at " + path + ". Using " + fallback + ".");
            return fallback;
        }
        return slot;
    }

    private void warnDuplicateHotbarSlots() {
        int info = hotbarSlots.getOrDefault("info", 0);
        int selector = hotbarSlots.getOrDefault("selector", 4);
        int hider = hotbarSlots.getOrDefault("hider", 8);
        if (info == selector || info == hider || selector == hider) {
            plugin.getLogger().warning("Hotbar slots overlap (info=" + info + ", selector=" + selector + ", hider=" + hider + ").");
        }
    }
}
