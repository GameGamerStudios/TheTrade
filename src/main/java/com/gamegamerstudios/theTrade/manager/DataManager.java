package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DataManager {
    private final Plugin plugin;
    private final File logFile;
    private YamlConfiguration tradeLogs;
    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        logFile = new File(plugin.getDataFolder(), "tradeLogs.yml");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe(MessageManager.getMessage("general.fileError")
                        .replace("%file%", "tradeLogs.yml"));
                e.printStackTrace();
            }
            if (!logFile.exists()) {
                Bukkit.getLogger().severe(MessageManager.getMessage("general.fileError")
                        .replace("%file%", "tradeLogs.yml"));
                return;
            }
        }

        tradeLogs = YamlConfiguration.loadConfiguration(logFile);
        if (tradeLogs.getConfigurationSection("logs") == null) {
            tradeLogs.createSection("logs");
        }

        saveLogs();
    }

    private void saveLogs() {
        try {
            tradeLogs.save(logFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe(MessageManager.getMessage("general.fileSaveError")
                    .replace("%file%", "tradeLogs.yml"));
            e.printStackTrace();
        }
    }

    public void logTrade(Player player1, Player player2, HashMap<UUID, HashMap<Integer, ItemStack>> items, double player1Deposit, double player2Deposit) {
        int id = 0;
        for (String str : tradeLogs.getConfigurationSection("logs").getKeys(false)) {
            id = Integer.parseInt(str);
        }

        String key = "logs." + id;
        tradeLogs.set(key + ".date", Utils.formatDate(LocalDateTime.now()));
        tradeLogs.set(key + ".players.0.name", player1.getDisplayName());
        tradeLogs.set(key + ".players.0.uuid", player1.getUniqueId().toString());
        tradeLogs.set(key + ".players.0.location", Utils.formatLocation(player1.getLocation()));
        tradeLogs.set(key + ".players.1.name", player2.getDisplayName());
        tradeLogs.set(key + ".players.1.uuid", player2.getUniqueId().toString());
        tradeLogs.set(key + ".players.1.location", Utils.formatLocation(player2.getLocation()));

        int itemId = 0;
        String itemKey = key + ".items." + itemId;
        for (ItemStack i : items.get(player1.getUniqueId()).values()) {
            tradeLogs.set(itemKey + ".tradedFrom", player1.getDisplayName() + " : " + player1.getUniqueId().toString());
            tradeLogs.set(itemKey + ".material", i.getType().name());
            if (i.hasItemMeta() && i.getItemMeta() != null) {
                tradeLogs.set(itemKey + ".name", i.getItemMeta().getDisplayName());
                List<String> lore = new ArrayList<>(i.getItemMeta().getLore());
                tradeLogs.set(itemKey + ".lore", lore);
            }
            tradeLogs.set(itemKey + ".amount", i.getAmount());
            itemId++;
        }

        for (ItemStack i : items.get(player2.getUniqueId()).values()) {
            tradeLogs.set(itemKey + ".tradedFrom", player2.getDisplayName() + " : " + player2.getUniqueId().toString());
            tradeLogs.set(itemKey + ".material", i.getType().name());
            if (i.hasItemMeta() && i.getItemMeta() != null) {
                tradeLogs.set(itemKey + ".name", i.getItemMeta().getDisplayName());
                List<String> lore = new ArrayList<>(i.getItemMeta().getLore());
                tradeLogs.set(itemKey + ".lore", lore);
            }
            tradeLogs.set(itemKey + ".amount", i.getAmount());
            itemId++;
        }

        int moneyId = 0;
        String moneyKey = key + ".money." + moneyId;
        if (plugin.getEconomyManager().getIsEnabled() && plugin.getConfig().getBoolean("tradeInGameMoney")) {
            if (player1Deposit > 0) {
                tradeLogs.set(moneyKey + ".tradedFrom", player1.getDisplayName() + " : " + player1.getUniqueId().toString());
                tradeLogs.set(moneyKey + ".amount", player1Deposit);
                moneyId++;
            }
            if (player2Deposit > 0) {
                tradeLogs.set(moneyKey + ".tradedFrom", player2.getDisplayName() + " : " + player2.getUniqueId().toString());
                tradeLogs.set(moneyKey + ".amount", player2Deposit);
                moneyId++;
            }
        }

        saveLogs();
    }
}
