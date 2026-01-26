package de.maximanu.lobbySystem.listener;

import de.maximanu.lobbySystem.LobbySystem;
import de.maximanu.lobbySystem.config.ConfigService;
import de.maximanu.lobbySystem.menu.ServerSelectorMenu;
import de.maximanu.lobbySystem.service.HotbarService;
import de.maximanu.lobbySystem.service.MessageService;
import de.maximanu.lobbySystem.service.PlayerStateService;
import de.maximanu.lobbySystem.service.SpawnService;
import de.maximanu.lobbySystem.service.VisibilityService;
import de.maximanu.lobbySystem.util.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final LobbySystem plugin;
    private final ConfigService configService;
    private final HotbarService hotbarService;
    private final ServerSelectorMenu serverSelectorMenu;
    private final MessageService messageService;
    private final PlayerStateService playerStateService;
    private final SpawnService spawnService;
    private final VisibilityService visibilityService;

    public PlayerListener(LobbySystem plugin) {
        this.plugin = plugin;
        this.configService = plugin.getConfigService();
        this.hotbarService = plugin.getHotbarService();
        this.serverSelectorMenu = plugin.getServerSelectorMenu();
        this.messageService = plugin.getMessageService();
        this.playerStateService = plugin.getPlayerStateService();
        this.spawnService = plugin.getSpawnService();
        this.visibilityService = plugin.getVisibilityService();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (configService.isTeleportOnJoin()) {
            teleportToSpawnIfSet(p);
        }
        if (shouldGiveHotbar(p)) {
            hotbarService.giveHotbarItems(p);
        }
        visibilityService.applyVisibility(p, playerStateService.getPlayerHiderState(p.getUniqueId()));
        updateVisibilityForOthers(p);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Location spawn = spawnService.getSpawnLocation();
        if (configService.isTeleportOnRespawn() && spawn != null) {
            e.setRespawnLocation(spawn);
        }
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (shouldGiveHotbar(p)) {
                hotbarService.giveHotbarItems(p);
            }
            visibilityService.applyVisibility(p, playerStateService.getPlayerHiderState(p.getUniqueId()));
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!shouldProtect(p)) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            if (configService.isTeleportOnVoid()) {
                teleportToSpawnIfSet(p);
                e.setCancelled(true);
                return;
            }
            if (configService.isProtectDamage()) {
                e.setCancelled(true);
            }
            return;
        }
        if (configService.isProtectDamage()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!shouldProtect(player)) return;
        if (configService.isProtectHunger()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!shouldProtect(e.getPlayer())) return;
        if (configService.isProtectBlockBreak() && !playerStateService.getBuildMode(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!shouldProtect(e.getPlayer())) return;
        if (configService.isProtectBlockPlace() && !playerStateService.getBuildMode(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action action = e.getAction();

        boolean inLobbyWorld = isLobbyWorld(p);
        boolean protect = configService.isProtectAllWorlds() || inLobbyWorld;
        boolean hotbarActive = configService.isHotbarEnabled()
                && (configService.isHotbarAllWorlds() || inLobbyWorld);

        if (protect && configService.isProtectInteract() && !playerStateService.getBuildMode(p.getUniqueId())) {
            e.setCancelled(true);
        }

        if (!hotbarActive) return;
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack hand = p.getInventory().getItemInMainHand();
        String itemName = ItemFactory.safeName(hand);
        if (itemName == null) return;

        if (hotbarService.isInfoItemName(itemName)) {
            sendLinks(p);
        } else if (hotbarService.isSelectorItemName(itemName)) {
            serverSelectorMenu.open(p);
        } else if (hotbarService.isHiderItemName(itemName)) {
            togglePlayerHider(p);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        if (!shouldProtect(e.getPlayer())) return;
        if (configService.isProtectEntityInteract() && !playerStateService.getBuildMode(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (serverSelectorMenu.isSelectorView(e.getView())) {
            e.setCancelled(true);
            serverSelectorMenu.handleClick(p, e.getCurrentItem(), e.getView());
            return;
        }

        if (!shouldProtect(p)) return;
        if (configService.isProtectInventory() && !playerStateService.getBuildMode(p.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (shouldHotbarLock(p) && isLockedHotbarInteraction(p, e)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }
        if (serverSelectorMenu.isSelectorView(e.getView())) {
            e.setCancelled(true);
            return;
        }
        if (!shouldProtect(p)) return;
        if (configService.isProtectInventory() && !playerStateService.getBuildMode(p.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if (shouldHotbarLock(p)) {
            for (int rawSlot : e.getRawSlots()) {
                if (isHotbarRawSlot(rawSlot, e.getView())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!shouldProtect(e.getPlayer())) return;
        if (configService.isProtectItemDrop() && !playerStateService.getBuildMode(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        ItemStack dropped = e.getItemDrop().getItemStack();
        String name = ItemFactory.safeName(dropped);
        if (shouldHotbarLock(e.getPlayer()) && name != null && hotbarService.isHotbarItemName(name)) {
            e.setCancelled(true);
        }
    }

    private void togglePlayerHider(Player p) {
        UUID id = p.getUniqueId();
        int state = (playerStateService.getPlayerHiderState(id) + 1) % 3;
        playerStateService.setPlayerHiderState(id, state);
        visibilityService.applyVisibility(p, state);
        hotbarService.updatePlayerHiderItem(p);
        String msg = switch (state) {
            case 0 -> messageService.get("info.visibility.all", "&bPlayer visibility: &7All players shown");
            case 1 -> messageService.get("info.visibility.ops", "&bPlayer visibility: &7Only ops shown");
            default -> messageService.get("info.visibility.hidden", "&bPlayer visibility: &7All players hidden");
        };
        p.sendMessage(msg);
    }

    private void sendLinks(Player p) {
        p.sendMessage(messageService.format(
                "links.website",
                "&aWebsite: &7{link}",
                java.util.Map.of("link", configService.getLink("website", "https://example.com"))
        ));
        p.sendMessage(messageService.format(
                "links.discord",
                "&9Discord: &7{link}",
                java.util.Map.of("link", configService.getLink("discord", "https://discord.gg/example"))
        ));
        p.sendMessage(messageService.format(
                "links.store",
                "&6Store: &7{link}",
                java.util.Map.of("link", configService.getLink("store", "https://store.example.com"))
        ));
    }

    private void teleportToSpawnIfSet(Player p) {
        Location spawn = spawnService.getSpawnLocation();
        if (spawn != null) {
            p.teleport(spawn);
            return;
        }
        if (p.isOp()) {
            p.sendMessage(messageService.get("errors.spawn-not-set-op", "&cSpawn is not set. Use /setspawn."));
        }
    }

    private void updateVisibilityForOthers(Player joined) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(joined)) continue;
            int state = playerStateService.getPlayerHiderState(other.getUniqueId());
            visibilityService.applyVisibilityToTarget(other, joined, state);
        }
    }

    public void refreshAllPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            refreshPlayer(p);
        }
    }

    public void refreshPlayer(Player p) {
        if (shouldGiveHotbar(p)) {
            hotbarService.giveHotbarItems(p);
        }
        visibilityService.applyVisibility(p, playerStateService.getPlayerHiderState(p.getUniqueId()));
        updateVisibilityForOthers(p);
    }

    private boolean isLobbyWorld(Player p) {
        String worldName = configService.getLobbyWorldName();
        if (worldName.isEmpty()) return true;
        return p.getWorld().getName().equalsIgnoreCase(worldName);
    }

    private boolean shouldProtect(Player p) {
        return configService.isProtectAllWorlds() || isLobbyWorld(p);
    }

    private boolean shouldGiveHotbar(Player p) {
        return configService.isHotbarEnabled()
                && (configService.isHotbarAllWorlds() || isLobbyWorld(p));
    }

    private boolean shouldHotbarLock(Player p) {
        return configService.isHotbarEnabled()
                && configService.isHotbarLockEnabled()
                && (configService.isHotbarAllWorlds() || isLobbyWorld(p));
    }

    private boolean isLockedHotbarInteraction(Player p, InventoryClickEvent e) {
        if (e.getClickedInventory() != null && e.getClickedInventory().equals(p.getInventory())) {
            int slot = e.getSlot();
            if (!hotbarService.isHotbarSlot(slot)) return false;
        }

        ItemStack current = e.getCurrentItem();
        String currentName = ItemFactory.safeName(current);
        if (currentName != null && hotbarService.isHotbarItemName(currentName)) return true;

        ItemStack cursor = e.getCursor();
        String cursorName = ItemFactory.safeName(cursor);
        if (cursorName != null && hotbarService.isHotbarItemName(cursorName)) return true;

        int hotbarButton = e.getHotbarButton();
        if (hotbarButton >= 0) {
            if (!hotbarService.isHotbarSlot(hotbarButton)) return false;
            ItemStack hotbarItem = p.getInventory().getItem(hotbarButton);
            String hotbarName = ItemFactory.safeName(hotbarItem);
            return hotbarName != null && hotbarService.isHotbarItemName(hotbarName);
        }
        return false;
    }

    private boolean isHotbarRawSlot(int rawSlot, org.bukkit.inventory.InventoryView view) {
        int topSize = view.getTopInventory().getSize();
        int hotbarBase = topSize + 27;
        for (int slot : hotbarService.getHotbarSlots()) {
            if (rawSlot == hotbarBase + slot) return true;
        }
        return false;
    }
}
