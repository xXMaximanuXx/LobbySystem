package de.maximanu.lobbySystem.service;

import de.maximanu.lobbySystem.LobbySystem;
import de.maximanu.lobbySystem.util.ItemFactory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HotbarService {

    private final LobbySystem plugin;
    private final MessageService messageService;
    private String infoName;
    private List<String> infoLore;
    private String selectorName;
    private List<String> selectorLore;
    private String hiderPrefix;
    private String hiderNameShown;
    private String hiderNameOps;
    private String hiderNameHidden;
    private List<String> hiderLoreShown;
    private List<String> hiderLoreOps;
    private List<String> hiderLoreHidden;
    private int infoSlot;
    private int selectorSlot;
    private int hiderSlot;
    private Material infoMaterial;
    private Material selectorMaterial;
    private Material hiderMaterial;
    private List<Integer> hotbarSlots;

    public HotbarService(LobbySystem plugin) {
        this.plugin = plugin;
        this.messageService = plugin.getMessageService();
        reload();
    }

    public void reload() {
        infoName = messageService.get("hotbar.info.name", "&aServer Information");
        infoLore = messageService.getList("hotbar.info.lore", List.of("&7Right click"));
        selectorName = messageService.get("hotbar.selector.name", "&eServer Selector");
        selectorLore = messageService.getList("hotbar.selector.lore", List.of("&7Right click to choose a server"));
        hiderPrefix = messageService.get("hotbar.hider.prefix", "&cPlayer Hider");
        hiderNameShown = messageService.get("hotbar.hider.name.shown", "&cPlayer Hider &7(Shown)");
        hiderNameOps = messageService.get("hotbar.hider.name.ops", "&cPlayer Hider &7(Only Ops)");
        hiderNameHidden = messageService.get("hotbar.hider.name.hidden", "&cPlayer Hider &7(Hidden)");
        hiderLoreShown = messageService.getList("hotbar.hider.lore.shown", List.of("&7Right click to hide players"));
        hiderLoreOps = messageService.getList("hotbar.hider.lore.ops", List.of("&7Right click to show all players"));
        hiderLoreHidden = messageService.getList("hotbar.hider.lore.hidden", List.of("&7Right click to show players"));

        infoSlot = plugin.getConfigService().getHotbarSlot("info", 0);
        selectorSlot = plugin.getConfigService().getHotbarSlot("selector", 4);
        hiderSlot = plugin.getConfigService().getHotbarSlot("hider", 8);
        hotbarSlots = List.of(infoSlot, selectorSlot, hiderSlot);

        infoMaterial = plugin.getConfigService().getHotbarMaterial("info", Material.BOOK);
        selectorMaterial = plugin.getConfigService().getHotbarMaterial("selector", Material.NETHER_STAR);
        hiderMaterial = plugin.getConfigService().getHotbarMaterial("hider", Material.PLAYER_HEAD);
    }

    public void giveHotbarItems(Player player) {
        clearHotbarItems(player);
        setHotbarItem(player, infoSlot, ItemFactory.createNamedItem(infoMaterial, infoName, infoLore));
        setHotbarItem(player, selectorSlot, ItemFactory.createNamedItem(selectorMaterial, selectorName, selectorLore));
        updatePlayerHiderItem(player);
    }

    public void updatePlayerHiderItem(Player player) {
        int state = plugin.getPlayerStateService().getPlayerHiderState(player.getUniqueId());
        String name;
        List<String> lore;
        switch (state) {
            case 1 -> {
                name = hiderNameOps;
                lore = hiderLoreOps;
            }
            case 2 -> {
                name = hiderNameHidden;
                lore = hiderLoreHidden;
            }
            default -> {
                name = hiderNameShown;
                lore = hiderLoreShown;
            }
        }
        setHotbarItem(player, hiderSlot, ItemFactory.createNamedItem(hiderMaterial, name, lore));
    }

    public boolean isInfoItemName(String name) {
        return infoName.equals(name);
    }

    public boolean isSelectorItemName(String name) {
        return selectorName.equals(name);
    }

    public boolean isHiderItemName(String name) {
        return name != null && name.startsWith(hiderPrefix);
    }

    public boolean isHotbarItemName(String name) {
        return isInfoItemName(name) || isSelectorItemName(name) || isHiderItemName(name);
    }

    public boolean isHotbarSlot(int slot) {
        return slot == infoSlot || slot == selectorSlot || slot == hiderSlot;
    }

    public List<Integer> getHotbarSlots() {
        return hotbarSlots;
    }

    private void setHotbarItem(Player player, int slot, ItemStack item) {
        if (slot < 0 || slot > 8) return;
        player.getInventory().setItem(slot, item);
    }

    private void clearHotbarItems(Player player) {
        if (infoSlot >= 0 && infoSlot <= 8) player.getInventory().setItem(infoSlot, null);
        if (selectorSlot >= 0 && selectorSlot <= 8) player.getInventory().setItem(selectorSlot, null);
        if (hiderSlot >= 0 && hiderSlot <= 8) player.getInventory().setItem(hiderSlot, null);
    }
}
