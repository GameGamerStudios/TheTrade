package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.api.TradeGUI;
import com.gamegamerstudios.theTrade.api.TradeListener;
import com.gamegamerstudios.theTrade.api.TradeRequest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TradeManager {
    private final Plugin plugin;
    private List<TradeGUI> activeTrades = new ArrayList<>();
    public TradeManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new TradeListener(), plugin);
    }

    public void newTrade(Player player1, Player player2) {

    }
}
