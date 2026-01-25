package de.maximanu.lobbySystem.listener;

import de.maximanu.lobbySystem.LobbySystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.maximanu.lobbySystem.util.ItemFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final LobbySystem plugin;

    private static final String INFO_NAME = "§aꜱᴇʀᴠᴇʀ ɪɴꜰᴏʀᴍᴀᴛɪᴏɴᴇɴ";
    private static final String SELECTOR_NAME = "§eꜱᴇʀᴠᴇʀ ꜱᴇʟᴇᴄᴛᴏʀ";
    private static final String HIDER_PREFIX = "§cᴘʟᴀʏᴇʀ ʜɪᴅᴇʀ";
    private static final String SERVER_SELECTOR_TITLE = SELECTOR_NAME;

    public PlayerListener(LobbySystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        teleportToSpawnIfSet(p);
        giveHotbarItems(p);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Location spawn = plugin.getSpawnLocation();
        if (spawn != null) e.setRespawnLocation(spawn);
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> giveHotbarItems(p), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            teleportToSpawnIfSet(p);
            e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!plugin.getBuildMode(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!plugin.getBuildMode(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Action action = e.getAction();

        if (!plugin.getBuildMode(p.getUniqueId())) {
            e.setCancelled(true);
        }

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack hand = p.getInventory().getItemInMainHand();
        String itemName = ItemFactory.safeName(hand);
        if (itemName == null) return;

        if (itemName.equals(INFO_NAME)) {
            sendLinks(p);
        } else if (itemName.equals(SELECTOR_NAME)) {
            openServerSelector(p);
        } else if (itemName.startsWith(HIDER_PREFIX)) {
            togglePlayerHider(p);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        if (!plugin.getBuildMode(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            if (!plugin.getBuildMode(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }

        if (e.getView() != null && SERVER_SELECTOR_TITLE.equals(e.getView().getTitle())) {
            e.setCancelled(true);
            if (!(e.getWhoClicked() instanceof Player)) return;
            Player p = (Player) e.getWhoClicked();
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null) { p.closeInventory(); return; }
            List<String> lore = ItemFactory.getLore(clicked);
            if (lore.isEmpty()) { p.closeInventory(); return; }
            String first = lore.get(0);
            if (first.startsWith("server:")) {
                String key = first.substring("server:".length()).trim();
                p.closeInventory();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(key);

                p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            } else {
                p.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack dropped = e.getItemDrop().getItemStack();
        String name = ItemFactory.safeName(dropped);
        if (name != null && isHotbarItemName(name)) e.setCancelled(true);
    }

    private void giveHotbarItems(Player p) {
        p.getInventory().setItem(0, ItemFactory.createNamedItem(Material.BOOK, INFO_NAME, List.of("§7Right click")));
        p.getInventory().setItem(4, ItemFactory.createNamedItem(Material.NETHER_STAR, SELECTOR_NAME, List.of("§7Right click to choose a server")));
        updatePlayerHiderItem(p);
    }

    private void updatePlayerHiderItem(Player p) {
        int state = plugin.getPlayerHiderState(p.getUniqueId());
        Material mat = Material.PLAYER_HEAD;
        String name;
        List<String> lore;
        switch (state) {
            case 1 -> { name = HIDER_PREFIX + " §7(Only Ops)"; lore = List.of("§7Right click to show all players"); }
            case 2 -> { name = HIDER_PREFIX + " §7(Hidden)"; lore = List.of("§7Right click to show players"); }
            default -> { name = HIDER_PREFIX + " §7(Shown)"; lore = List.of("§7Right click to hide players"); }
        }
        p.getInventory().setItem(8, ItemFactory.createNamedItem(mat, name, lore));
    }

    private void togglePlayerHider(Player p) {
        UUID id = p.getUniqueId();
        int state = (plugin.getPlayerHiderState(id) + 1) % 3;
        plugin.setPlayerHiderState(id, state);
        applyVisibility(p, state);
        updatePlayerHiderItem(p);
        String msg = switch (state) {
            case 0 -> "§bᴘʟᴀʏᴇʀ ᴠɪꜱɪʙɪʟɪᴛʏ: §7All players shown";
            case 1 -> "§bᴘʟᴀʏᴇʀ ᴠɪꜱɪʙɪʟɪᴛʏ: §7Only ops shown";
            default -> "§bᴘʟᴀʏᴇʀ ᴠɪꜱɪʙɪʟɪᴛʏ: §7All players hidden";
        };
        p.sendMessage("§7" + msg);
    }

    private void applyVisibility(Player p, int state) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(p)) continue;
            if (state == 0) p.showPlayer(plugin, other);
            else if (state == 1) {
                if (other.isOp()) p.showPlayer(plugin, other);
                else p.hidePlayer(plugin, other);
            } else p.hidePlayer(plugin, other);
        }
    }

    private void openServerSelector(Player p) {
        Inventory sel = Bukkit.createInventory(null, 9, SERVER_SELECTOR_TITLE);
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("servers");
        if (sec == null) {
            sel.setItem(4, ItemFactory.createNamedItem(Material.BARRIER, "§cNo servers configured", List.of("§7Please add them in the config.yml")));
            p.openInventory(sel);
            return;
        }
        List<String> keys = new ArrayList<>(sec.getKeys(false));
        int[] slots = new int[] {3, 5, 2, 6, 1, 7};
        for (int i = 0; i < keys.size() && i < slots.length; i++) {
            String key = keys.get(i);
            String display = "§6" + key.toUpperCase();
            List<String> lore = List.of("§7Click to connect");
            sel.setItem(slots[i], ItemFactory.createNamedItem(materialForServerKey(key), display, lore));
        }
        p.openInventory(sel);
    }

    private Material materialForServerKey(String key) {
        return switch (key.toLowerCase()) {
            case "ffa" -> Material.BOW;
            case "duels" -> Material.DIAMOND_SWORD;
            default -> Material.PAPER;
        };
    }

    private boolean isHotbarItemName(String name) {
        return name.equals(INFO_NAME) || name.equals(SELECTOR_NAME) || name.startsWith(HIDER_PREFIX);
    }

    private void sendLinks(Player p) {
        p.sendMessage("§aᴡᴇʙꜱɪᴛᴇ: §7" + plugin.getConfig().getString("links.website", "https://example.com"));
        p.sendMessage("§9ᴅɪꜱᴄᴏʀᴅ: §7" + plugin.getConfig().getString("links.discord", "https://discord.gg/example"));
        p.sendMessage("§6ꜱᴛᴏʀᴇ: §7" + plugin.getConfig().getString("links.store", "https://store.example.com"));
    }

    private void teleportToSpawnIfSet(Player p) {
        Location spawn = plugin.getSpawnLocation();
        if (spawn != null) p.teleport(spawn);
    }
}