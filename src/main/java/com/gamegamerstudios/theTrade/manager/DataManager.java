package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class DataManager {
    private final Plugin plugin;
    private File logFile;
    private File bansFile;
    private YamlConfiguration logs;
    private YamlConfiguration bans;
    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        setupLogs();
        setupBans();
    }

    private void setupLogs() {
        logFile = new File(plugin.getDataFolder(), "logs.yml");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe(MessageManager.getMessage("general.fileError")
                        .replace("%file%", "logs.yml"));
                e.printStackTrace();
            }
            if (!logFile.exists()) {
                Bukkit.getLogger().severe(MessageManager.getMessage("general.fileError")
                        .replace("%file%", "logs.yml"));
                return;
            }
        }

        logs = YamlConfiguration.loadConfiguration(logFile);
        if (logs.getConfigurationSection("trades") == null) {
            logs.createSection("trades");
        }
        if (plugin.getConfig().getBoolean("logBans")) {
            if (logs.getConfigurationSection("bans") == null) {
                logs.createSection("bans");
            }
        }
        saveLogs();
    }

    private void setupBans() {
        bansFile = new File(plugin.getDataFolder(), "bans.yml");
        if (!bansFile.exists()) {
            try {
                bansFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getLogger().severe(MessageManager.getMessage("general.fileError")
                        .replace("%file%", "bans.yml"));
                e.printStackTrace();
            }
            if (!bansFile.exists()) {
                Bukkit.getLogger().severe(MessageManager.getMessage("general.fileError")
                        .replace("%file%", "bans.yml"));
                return;
            }
        }

        bans = YamlConfiguration.loadConfiguration(bansFile);
        saveBans();
    }

    private void saveLogs() {
        try {
            logs.save(logFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe(MessageManager.getMessage("general.fileSaveError")
                    .replace("%file%", "logs.yml"));
            e.printStackTrace();
        }
    }

    private void saveBans() {
        try {
            bans.save(bansFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe(MessageManager.getMessage("general.fileSaveError")
                    .replace("%file%", "bans.yml"));
            e.printStackTrace();
        }
    }

    public boolean isBanned(UUID uuid) {
        HashMap<UUID, String> bannedUsers = new HashMap<>();
        int i = 0;
        for (String str : bans.getConfigurationSection("bannedUsers").getKeys(false)) {
            bannedUsers.put(UUID.fromString(bans.getString(str + i + ".uuid")), bans.getString(str + i + ".name"));
            i++;
        }
        return bannedUsers.containsKey(uuid);
    }

    public void ban(OfflinePlayer player, CommandSender banner) {
        HashMap<UUID, String> bannedUsers = new HashMap<>();
        int i = 0;
        for (String str : bans.getConfigurationSection("bannedUsers").getKeys(false)) {
            bannedUsers.put(UUID.fromString(bans.getString(str + i + ".uuid")), bans.getString(str + i + ".name"));
            i++;
        }
        bannedUsers.put(player.getUniqueId(), player.getName());

        Iterator<Map.Entry<UUID, String>> iterator = bannedUsers.entrySet().iterator();
        int d = 0;
        while (iterator.hasNext()) {
            Map.Entry<UUID, String> entry = iterator.next();
            bans.set("bannedUsers." + d + ".uuid", entry.getKey());
            bans.set("bannedUsers." + d + ".name", entry.getValue());
            d++;
        }

        saveBans();
        logBan(player, banner, true);
    }

    public void unban(OfflinePlayer player, CommandSender banner) {
        HashMap<UUID, String> bannedUsers = new HashMap<>();
        int i = 0;
        for (String str : bans.getConfigurationSection("bannedUsers").getKeys(false)) {
            bannedUsers.put(UUID.fromString(bans.getString(str + i + ".uuid")), bans.getString(str + i + ".name"));
            i++;
        }
        bannedUsers.remove(player.getUniqueId());

        Iterator<Map.Entry<UUID, String>> iterator = bannedUsers.entrySet().iterator();
        int d = 0;
        while (iterator.hasNext()) {
            Map.Entry<UUID, String> entry = iterator.next();
            bans.set("bannedUsers." + d + ".uuid", entry.getKey());
            bans.set("bannedUsers." + d + ".name", entry.getValue());
            d++;
        }

        saveBans();
        logBan(player, banner, false);
    }

    public void logTrade(Player player1, Player player2, HashMap<UUID, HashMap<Integer, ItemStack>> items, double player1Deposit, double player2Deposit) {
        int id = 0;
        for (String str : logs.getConfigurationSection("trades").getKeys(false)) {
            id = Integer.parseInt(str);
        }

        String key = "trades." + id;
        logs.set(key + ".date", Utils.formatDate(LocalDateTime.now()));
        logs.set(key + ".players.0.name", player1.getDisplayName());
        logs.set(key + ".players.0.uuid", player1.getUniqueId().toString());
        logs.set(key + ".players.0.location", Utils.formatLocation(player1.getLocation()));
        logs.set(key + ".players.1.name", player2.getDisplayName());
        logs.set(key + ".players.1.uuid", player2.getUniqueId().toString());
        logs.set(key + ".players.1.location", Utils.formatLocation(player2.getLocation()));

        int itemId = 0;
        String itemKey = key + ".items." + itemId;
        for (ItemStack i : items.get(player1.getUniqueId()).values()) {
            logs.set(itemKey + ".tradedFrom", player1.getDisplayName() + " : " + player1.getUniqueId().toString());
            logs.set(itemKey + ".material", i.getType().name());
            if (i.hasItemMeta() && i.getItemMeta() != null) {
                logs.set(itemKey + ".name", i.getItemMeta().getDisplayName());
                List<String> lore = new ArrayList<>(i.getItemMeta().getLore());
                logs.set(itemKey + ".lore", lore);
            }
            logs.set(itemKey + ".amount", i.getAmount());
            itemId++;
        }

        for (ItemStack i : items.get(player2.getUniqueId()).values()) {
            logs.set(itemKey + ".tradedFrom", player2.getDisplayName() + " : " + player2.getUniqueId().toString());
            logs.set(itemKey + ".material", i.getType().name());
            if (i.hasItemMeta() && i.getItemMeta() != null) {
                logs.set(itemKey + ".name", i.getItemMeta().getDisplayName());
                List<String> lore = new ArrayList<>(i.getItemMeta().getLore());
                logs.set(itemKey + ".lore", lore);
            }
            logs.set(itemKey + ".amount", i.getAmount());
            itemId++;
        }

        int moneyId = 0;
        String moneyKey = key + ".money." + moneyId;
        if (plugin.getEconomyManager().getIsEnabled() && plugin.getConfig().getBoolean("tradeInGameMoney")) {
            if (player1Deposit > 0) {
                logs.set(moneyKey + ".tradedFrom", player1.getDisplayName() + " : " + player1.getUniqueId().toString());
                logs.set(moneyKey + ".amount", player1Deposit);
                moneyId++;
            }
            if (player2Deposit > 0) {
                logs.set(moneyKey + ".tradedFrom", player2.getDisplayName() + " : " + player2.getUniqueId().toString());
                logs.set(moneyKey + ".amount", player2Deposit);
                moneyId++;
            }
        }
        saveLogs();
    }

    public void logBan(OfflinePlayer player, CommandSender banner, boolean banned) {
        int id = 0;
        for (String str : logs.getConfigurationSection("bans").getKeys(false)) {
            id = Integer.parseInt(str);
        }
        id++;

        String key = "bans." + id;
        logs.set(key + ".date", Utils.formatDate(LocalDateTime.now()));
        if (banned) {
            logs.set(key + ".action", "BANNED");
        } else {
            logs.set(key + ".action", "UNBANNED");
        }
        logs.set(key + ".affectedPlayer.name", (player.isOnline() ? Bukkit.getPlayer(player.getUniqueId()).getDisplayName() : player.getName()));
        logs.set(key + ".affectedPlayer.uuid", player.getUniqueId());
        logs.set(key + ".byUser.name", ((banner instanceof Player) ? ((Player) banner).getDisplayName() : "console"));
        logs.set(key + ".byUser.uuid", ((banner instanceof Player) ? ((Player) banner).getUniqueId() : "console"));
        saveLogs();
    }

    public List<String> getBannedNames() {
        List<String> bannedUsers = new ArrayList<>();
        int i = 0;
        for (String str : bans.getConfigurationSection("bannedUsers").getKeys(false)) {
            bannedUsers.add(bans.getString(str + i + ".name"));
            i++;
        }
        return bannedUsers;
    }
}
