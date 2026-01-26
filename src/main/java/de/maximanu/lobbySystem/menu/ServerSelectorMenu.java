package de.maximanu.lobbySystem.menu;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.maximanu.lobbySystem.LobbySystem;
import de.maximanu.lobbySystem.config.ConfigService;
import de.maximanu.lobbySystem.config.ServerEntry;
import de.maximanu.lobbySystem.service.MessageService;
import de.maximanu.lobbySystem.util.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerSelectorMenu {

    private static final String DEFAULT_TITLE = "&eServer Selector";
    private static final String DEFAULT_PREV_PAGE = "&7Previous Page";
    private static final String DEFAULT_NEXT_PAGE = "&7Next Page";
    private final LobbySystem plugin;
    private final ConfigService configService;
    private final MessageService messageService;
    private final NamespacedKey bungeeKey;
    private ItemStack fillerItem;
    private String fillerName;
    private String titleBase;
    private String prevPageName;
    private String nextPageName;

    public ServerSelectorMenu(LobbySystem plugin, ConfigService configService) {
        this.plugin = plugin;
        this.configService = configService;
        this.messageService = plugin.getMessageService();
        this.bungeeKey = new NamespacedKey(plugin, "bungee");
        reloadMessages();
    }

    public void reloadMessages() {
        this.titleBase = messageService.get("menu.selector.title", DEFAULT_TITLE);
        this.prevPageName = messageService.get("menu.selector.prev-page", DEFAULT_PREV_PAGE);
        this.nextPageName = messageService.get("menu.selector.next-page", DEFAULT_NEXT_PAGE);
        this.fillerName = messageService.get("menu.selector.filler-name", " ");
        updateFillerItem();
    }

    public boolean isSelectorView(InventoryView view) {
        return view != null && view.getTitle().startsWith(titleBase);
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int requestedPage) {
        List<ServerEntry> entries = configService.getServerEntries();
        int size = configService.getSelectorSize();
        int prevPageSlot = configService.getSelectorPrevSlot();
        int nextPageSlot = configService.getSelectorNextSlot();
        List<Integer> freeSlots = buildFreeSlots(size, prevPageSlot, nextPageSlot);
        int perPage = freeSlots.size();
        int totalPages = Math.max(1, (entries.size() + perPage - 1) / perPage);
        int page = Math.min(Math.max(0, requestedPage), totalPages - 1);
        Inventory inv = Bukkit.createInventory(null, size, title(page, totalPages));

        if (entries.isEmpty()) {
            String name = messageService.get("menu.selector.no-servers.name", "&cNo servers configured");
            List<String> lore = messageService.getList(
                    "menu.selector.no-servers.lore",
                    List.of("&7Please add them in the config.yml")
            );
            int center = Math.min(inv.getSize() - 1, inv.getSize() / 2);
            inv.setItem(center, ItemFactory.createNamedItem(Material.BARRIER, name, lore));
            player.openInventory(inv);
            return;
        }

        updateFillerItem();
        int start = page * perPage;
        List<ServerEntry> pageEntries = entries.subList(start, Math.min(start + perPage, entries.size()));
        Set<Integer> usedSlots = new HashSet<>();

        for (ServerEntry entry : pageEntries) {
            int slot = entry.getSlot();
            if (slot >= 0 && slot < size && slot != prevPageSlot && slot != nextPageSlot && !usedSlots.contains(slot)) {
                setServerItem(inv, slot, entry);
                usedSlots.add(slot);
                freeSlots.remove((Integer) slot);
            }
        }

        for (ServerEntry entry : pageEntries) {
            if (usedSlots.contains(entry.getSlot())) continue;
            if (freeSlots.isEmpty()) break;
            int slot = freeSlots.remove(0);
            setServerItem(inv, slot, entry);
        }

        if (prevPageSlot >= 0 && totalPages > 1 && page > 0) {
            List<String> lore = messageService.formatList(
                    "menu.selector.page-lore",
                    List.of("&7Page {page} of {pages}"),
                    Map.of("page", String.valueOf(page), "pages", String.valueOf(totalPages))
            );
            inv.setItem(prevPageSlot, ItemFactory.createNamedItem(Material.ARROW, prevPageName, lore));
        }
        if (nextPageSlot >= 0 && totalPages > 1 && page < totalPages - 1) {
            List<String> lore = messageService.formatList(
                    "menu.selector.page-lore",
                    List.of("&7Page {page} of {pages}"),
                    Map.of("page", String.valueOf(page + 2), "pages", String.valueOf(totalPages))
            );
            inv.setItem(nextPageSlot, ItemFactory.createNamedItem(Material.ARROW, nextPageName, lore));
        }

        if (configService.isSelectorFillEmpty()) {
            fillEmpty(inv);
        }
        player.openInventory(inv);
    }

    public boolean handleClick(Player player, ItemStack clicked, InventoryView view) {
        if (clicked == null) {
            player.closeInventory();
            return true;
        }
        String clickedName = ItemFactory.safeName(clicked);
        if (fillerName.equals(clickedName)) {
            return true;
        }
        int page = getPageFromTitle(view.getTitle());
        if (prevPageName.equals(clickedName)) {
            open(player, Math.max(0, page - 1));
            return true;
        }
        if (nextPageName.equals(clickedName)) {
            open(player, page + 1);
            return true;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) {
            player.closeInventory();
            return true;
        }
        String server = meta.getPersistentDataContainer().get(bungeeKey, PersistentDataType.STRING);
        if (server == null || server.isEmpty()) {
            player.closeInventory();
            return true;
        }
        player.closeInventory();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        return true;
    }

    private void setServerItem(Inventory inv, int slot, ServerEntry entry) {
        ItemStack item = ItemFactory.createNamedItem(entry.getMaterial(), entry.getDisplayName(), entry.getLore());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(bungeeKey, PersistentDataType.STRING, entry.getBungeeName());
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }

    private List<Integer> buildFreeSlots(int size, int prevPageSlot, int nextPageSlot) {
        List<Integer> layout = configService.getSelectorLayoutSlots();
        List<Integer> slots = new ArrayList<>();
        if (layout.isEmpty()) {
            for (int i = 0; i < size; i++) {
                if (i == prevPageSlot || i == nextPageSlot) continue;
                slots.add(i);
            }
        } else {
            slots.addAll(layout);
        }
        if (prevPageSlot >= 0) slots.remove((Integer) prevPageSlot);
        if (nextPageSlot >= 0) slots.remove((Integer) nextPageSlot);
        if (slots.isEmpty()) {
            for (int i = 0; i < size; i++) {
                if (i == prevPageSlot || i == nextPageSlot) continue;
                slots.add(i);
            }
        }
        return slots;
    }

    private void fillEmpty(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, fillerItem);
            }
        }
    }

    private void updateFillerItem() {
        Material mat = configService.getSelectorFillerMaterial();
        fillerItem = ItemFactory.createNamedItem(mat, fillerName, List.of());
    }

    private String title(int page, int totalPages) {
        return titleBase + " §7(" + (page + 1) + "/" + totalPages + ")";
    }

    private int getPageFromTitle(String title) {
        int start = title.lastIndexOf('(');
        int slash = title.lastIndexOf('/');
        if (start == -1 || slash == -1 || slash < start) return 0;
        String num = title.substring(start + 1, slash).trim();
        try {
            int page = Integer.parseInt(num);
            return Math.max(0, page - 1);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
