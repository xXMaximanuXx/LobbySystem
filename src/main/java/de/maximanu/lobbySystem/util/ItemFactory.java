package de.maximanu.lobbySystem.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class ItemFactory {

    private ItemFactory() {}

    public static ItemStack createNamedItem(Material mat, String display, List<String> lore) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        if (m != null) {
            m.setDisplayName(display);
            m.setLore(lore);
            i.setItemMeta(m);
        }
        return i;
    }

    public static String safeName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta m = item.getItemMeta();
        if (m == null) return null;
        return m.getDisplayName();
    }

    public static List<String> getLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return List.of();
        ItemMeta m = item.getItemMeta();
        if (m == null || m.getLore() == null) return List.of();
        return m.getLore();
    }
}