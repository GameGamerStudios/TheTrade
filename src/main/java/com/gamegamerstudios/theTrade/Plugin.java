package com.gamegamerstudios.theTrade;

import com.gamegamerstudios.theTrade.commands.TradeAdminCommand;
import com.gamegamerstudios.theTrade.commands.TradeCommand;
import com.gamegamerstudios.theTrade.manager.*;
import com.gamegamerstudios.theTrade.util.Metrics;
import com.gamegamerstudios.theTrade.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public final class Plugin extends JavaPlugin {
    private final int resourceId = 130311;
    private RequestManager requestManager;
    private TradeManager tradeManager;
    private EconomyManager economyManager;
    private DataManager dataManager;
    private UpdateChecker updateChecker;
    @Override
    public void onEnable() {
        sendStartupMessages();
        ConfigManager.setupConfig(this);
        MessageManager.initMessageManager(this);
        this.requestManager = new RequestManager(this);
        this.tradeManager = new TradeManager(this);
        this.economyManager = new EconomyManager(this);
        this.dataManager = new DataManager(this);
        if (getConfig().getBoolean("updateChecker")) {
            this.updateChecker = new UpdateChecker(this, resourceId, Duration.ofHours(1));
            updateChecker.checkForUpdates();
        }
        registerCommands();
        setupMetrics();
    }

    private void registerCommands() {
        getServer().getPluginCommand("trade").setExecutor(new TradeCommand(this));
        TradeAdminCommand adminCommand = new TradeAdminCommand(this);
        getServer().getPluginCommand("tradeadmin").setExecutor(adminCommand);
        getServer().getPluginCommand("tradeadmin").setTabCompleter(adminCommand);
    }

    private void setupMetrics() {
        int pluginId = 28075;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        requestManager.shutdown();
        tradeManager.shutdown();
    }

    public int getResourceId() { return resourceId; }

    public RequestManager getRequestManager() { return requestManager; }
    public TradeManager getTradeManager() { return tradeManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public DataManager getDataManager() { return dataManager; }
    public UpdateChecker getUpdateChecker() { return updateChecker; }

    private void sendStartupMessages() {
        Bukkit.getLogger().info(" ████████╗██╗  ██╗███████╗████████╗██████╗  █████╗ ██████╗ ███████╗");
        Bukkit.getLogger().info(" ╚══██╔══╝██║  ██║██╔════╝╚══██╔══╝██╔══██╗██╔══██╗██╔══██╗██╔════╝");
        Bukkit.getLogger().info("    ██║   ██╔══██║██╔══╝     ██║   ██╔══██╗██╔══██║██╔══██╗██╔══╝");
        Bukkit.getLogger().info("    ██║   ██╔══██║██╔══╝     ██║   ██╔══██╗██╔══██║██╔══██╗██╔══╝");
        Bukkit.getLogger().info("    ██║   ██║  ██║███████╗   ██║   ██║  ██║██║  ██║██║  ██║███████╗");
        Bukkit.getLogger().info("    ╚═╝   ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝");
        Bukkit.getLogger().info(" ");
        Bukkit.getLogger().info("                      By GameGamerStudios");
        Bukkit.getLogger().info(" ");
        Bukkit.getLogger().info("                       Loading Plugin...");
    }
}
