package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final Plugin plugin;
    private boolean isEnabled = false;
    private Economy economy;
    public EconomyManager(Plugin plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        if (!setupEconomy()) {
            isEnabled = false;
            Bukkit.getLogger().severe(MessageManager.getMessage("general.noVault"));
        } else {
            isEnabled = true;
        }
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() { return economy; }
    public boolean getIsEnabled() { return isEnabled; }
}
