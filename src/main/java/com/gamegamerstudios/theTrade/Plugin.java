package com.gamegamerstudios.theTrade;

import com.gamegamerstudios.theTrade.commands.TradeCommand;
import com.gamegamerstudios.theTrade.manager.ConfigManager;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import com.gamegamerstudios.theTrade.manager.RequestManager;
import com.gamegamerstudios.theTrade.manager.TradeManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Plugin extends JavaPlugin {
    private RequestManager requestManager;
    private TradeManager tradeManager;
    @Override
    public void onEnable() {
        ConfigManager.setupConfig(this);
        MessageManager.initMessageManager(this);

        this.requestManager = new RequestManager(this);
        this.tradeManager = new TradeManager(this);

        registerCommands();
    }

    private void registerCommands() {
        getServer().getPluginCommand("trade").setExecutor(new TradeCommand(this));
    }

    @Override
    public void onDisable() {
        requestManager.shutdown();
    }

    public RequestManager getRequestManager() { return requestManager; }
    public TradeManager getTradeManager() { return tradeManager; }
}
