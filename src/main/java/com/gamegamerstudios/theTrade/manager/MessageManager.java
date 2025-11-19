package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {
    private static FileConfiguration config;
    private static Plugin plugin;
    private static String lang;
    public static void initMessageManager(Plugin p) {
        plugin = p;
        if (plugin.getConfig().getString("language") == null) {
            Bukkit.getLogger().severe("[TheTrade] No language option found! This must be set to run (config.yml -> 'language')");
            Bukkit.getLogger().severe("[TheTrade] Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        File file = null;
        switch (plugin.getConfig().getString("language").toLowerCase()) {
            case "lang_en":
                Bukkit.getLogger().info("[TheTrade] Loading language 'lang_en' (English)...");
                file = new File(plugin.getDataFolder() + "/lang", "lang_en.yml");
                if (!file.exists()) {
                    plugin.saveResource("lang/lang_en.yml", false);
                }
                break;
        }
        lang = plugin.getConfig().getString("language").toLowerCase();
        if (file == null) {
            Bukkit.getLogger().severe("[TheTrade] Unable to fetch a language file! Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        config = YamlConfiguration.loadConfiguration(file);

        if (config.getInt("fileVersion") < FileUtil.getLangFileVersionLatest()) {
            Bukkit.getLogger().info("[TheTrade] Updating lang file...");
            FileUtil.updateFile(file.getName() + ".yml", config.getInt("fileVersion"), FileUtil.getLangFileVersionLatest(), plugin);
        }
        try {
            config.save(file);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[TheTrade] Unable to save lang file! Some messages may not be sent.");
        }
    }

    public static void reload() {
        File file = new File(plugin.getDataFolder() + "/lang", lang + ".yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static String getMessage(String key) {
        if (config.getString(key) == null) {
            Bukkit.getLogger().warning("[TheTrade] Message key " + key + " not set!");
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', config.getString(key)
                .replace("%prefix%", MessageManager.getPrefix())
                .replace("%log_prefix%", MessageManager.getLogPrefix())
        );
    }

    public static String getMessage(String key, String playerName) {
        String message = config.getString(key);
        if (message == null) {
            Bukkit.getLogger().warning("[TheTrade] Message key " + key + " not set!");
        }

        message = message
                .replace("%prefix%", MessageManager.getPrefix())
                .replace("%player%", playerName)
                .replace("%log_prefix%", MessageManager.getLogPrefix());

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> getMessageList(String key) {
        if (config.getStringList(key).isEmpty()) {
            Bukkit.getLogger().warning("[TheTrade] Message key " + key + " not set!");
        }
        List<String> results = new ArrayList<>();
        for (String str : config.getStringList(key)) {
            results.add(ChatColor.translateAlternateColorCodes('&', str
                    .replace("%prefix%", MessageManager.getPrefix())
                    .replace("%log_prefix%", MessageManager.getLogPrefix())
            ));
        }
        return results;
    }

    public static String getPrefix() { return getMessage("prefix"); }
    public static String getLogPrefix() { return getMessage("logprefix"); }

    public static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static boolean getConfig() { return config != null; }
}
