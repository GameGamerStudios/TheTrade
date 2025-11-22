package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private static FileConfiguration config;
    public static void setupConfig(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults();
        config = plugin.getConfig();
        updateConfig(plugin);
    }

    public static void updateConfig(Plugin plugin) {
        if (config.getInt("fileVersion", 0) < FileUtil.configVersionLatest()) {
            Bukkit.getLogger().info(MessageManager.getMessage("file.config.updating")
                    .replace("%version%", FileUtil.configVersionLatest() + ""));
            FileUtil.updateFile("config.yml", config.getInt("fileVersion", 0), FileUtil.configVersionLatest(), plugin);
        }
    }

    public static String getString(String path) {
        if (!MessageManager.getConfig()) { return ChatColor.translateAlternateColorCodes('&', config.getString(path, "")); }
        try {
            return ChatColor.translateAlternateColorCodes('&', config.getString(path, ""));
        } catch (Exception e) {
            for (String msg : MessageManager.getMessageList("file.config.stringfail")) {
                Bukkit.getLogger().warning(msg.replace("%value%", path));
            }
            return ChatColor.RED + "ERROR";
        }
    }

    public static boolean getBoolean(String path) { return config.getBoolean(path); }
}
