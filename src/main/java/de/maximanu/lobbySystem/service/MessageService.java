package de.maximanu.lobbySystem.service;

import de.maximanu.lobbySystem.LobbySystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageService {

    private final LobbySystem plugin;
    private FileConfiguration messages;
    private File messagesFile;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();
    private final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();
    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&#([0-9a-f]{6})");
    private static final Pattern MINI_TAG_PATTERN = Pattern.compile("<[^>]+>");

    public MessageService(LobbySystem plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String get(String key, String fallback) {
        String raw = messages.getString(key, fallback);
        return toLegacy(raw);
    }

    public List<String> getList(String key, List<String> fallback) {
        List<String> raw = messages.getStringList(key);
        if (raw == null || raw.isEmpty()) raw = fallback;
        return toLegacyList(raw);
    }

    public String format(String key, String fallback, Map<String, String> vars) {
        String raw = messages.getString(key, fallback);
        if (raw == null) raw = fallback;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            raw = raw.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return toLegacy(raw);
    }

    public List<String> formatList(String key, List<String> fallback, Map<String, String> vars) {
        List<String> raw = messages.getStringList(key);
        if (raw == null || raw.isEmpty()) raw = fallback;
        return raw.stream()
                .map(line -> formatInline(line, vars))
                .collect(Collectors.toList());
    }

    public String toLegacy(String raw) {
        return legacySection.serialize(parse(raw));
    }

    public List<String> toLegacyList(List<String> raw) {
        return raw.stream().map(this::toLegacy).collect(Collectors.toList());
    }

    private String formatInline(String line, Map<String, String> vars) {
        String replaced = line;
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            replaced = replaced.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return toLegacy(replaced);
    }

    private Component parse(String input) {
        if (input == null) return Component.empty();
        String normalized = HEX_PATTERN.matcher(input).replaceAll("<#$1>");
        if (MINI_TAG_PATTERN.matcher(normalized).find()) {
            return miniMessage.deserialize(normalized);
        }
        if (normalized.indexOf('§') >= 0) {
            return legacySection.deserialize(normalized);
        }
        if (normalized.indexOf('&') >= 0) {
            return legacyAmpersand.deserialize(normalized);
        }
        return Component.text(normalized);
    }
}
