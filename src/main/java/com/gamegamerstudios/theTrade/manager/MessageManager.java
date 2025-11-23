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
            case "lang_nl":
                Bukkit.getLogger().info("[Handel] Taal laden 'lang_en' (Nederlands)...");
                file = new File(plugin.getDataFolder() + "/lang", "lang_nl.yml");
                if (!file.exists()) {
                    plugin.saveResource("lang/lang_nl.yml", false);
                }
                break;
            case "lang_ko":
                Bukkit.getLogger().info("[더 트레이드] 언어 로딩 중 'lang_ko' (한국어)...");
                file = new File(plugin.getDataFolder() + "/lang", "lang_ko.yml");
                if (!file.exists()) {
                    plugin.saveResource("lang/lang_ko.yml", false);
                }
                break;
            case "lang_zh_cn":
                Bukkit.getLogger().info("【交易】正在加载语言 'lang_zh_cn'（简体中文）...");
                file = new File(plugin.getDataFolder() + "/lang", "lang_zh_cn.yml");
                if (!file.exists()) {
                    plugin.saveResource("lang/lang_zh_cn.yml", false);
                }
                break;
            case "lang_zh":
                Bukkit.getLogger().info("【交易】正在加载语言 'lang_zh_cn'（简体中文）...");
                file = new File(plugin.getDataFolder() + "/lang", "lang_zh_cn.yml");
                if (!file.exists()) {
                    plugin.saveResource("lang/lang_zh_cn.yml", false);
                }
                break;
            case "lang_zh_tw":
                Bukkit.getLogger().info("【交易】正在載入語言「lang_zh_tw」（簡體中文）…");
                file = new File(plugin.getDataFolder() + "/lang", "lang_zh_tw.yml");
                if (!file.exists()) {
                    plugin.saveResource("lang/lang_zh_tw.yml", false);
                }
                break;
            case "lang_es":
                Bukkit.getLogger().info("[El Comercio] Cargando idioma 'lang_es' (Español)...");
                file = new File(plugin.getDataFolder() + "/lang", "lang_es.yml");
                if (!file.exists()) {
                    plugin.saveResource("lang/lang_es.yml", false);
                }
                break;
        }
        lang = plugin.getConfig().getString("language").toLowerCase();
        if (file == null) {
            Bukkit.getLogger().severe("[TheTrade] Unable to fetch a language file! Are you sure you set 'language' in config.yml correctly? Disabling plugin...");
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

    public static String getPrefix() { return config.getString(ChatColor.translateAlternateColorCodes('&', "prefix")); }
    public static String getLogPrefix() { return config.getString(ChatColor.translateAlternateColorCodes('&', "logprefix")); }

    public static String translate(String string) {
        return ChatColor.translateAlternateColorCodes('&', string
                .replace("%prefix%", getPrefix())
                .replace("%log_prefix%", getLogPrefix()));
    }

    public static boolean getConfig() { return config != null; }
}
