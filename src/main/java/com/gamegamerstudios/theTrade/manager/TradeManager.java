package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.api.Trade;
import com.gamegamerstudios.theTrade.api.TradeGUI;
import com.gamegamerstudios.theTrade.api.TradeListener;
import com.gamegamerstudios.theTrade.api.TradeRequest;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class TradeManager {
    private final Plugin plugin;
    private final List<Trade> activeTrades = new ArrayList<>();
    public TradeManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new TradeListener(this), plugin);
    }

    public void newTrade(Player player1, Player player2) {
        activeTrades.add(new Trade(player1, player2));
    }
    public void cancelTrade(Player canceller, Player player2) {
        Iterator<Trade> iterator = activeTrades.iterator();
        Trade trade = null;

        while (iterator.hasNext()) {
            trade = iterator.next();
            boolean match =
                    trade.getPlayer1().getUniqueId().equals(canceller.getUniqueId()) &&
                            trade.getPlayer2().getUniqueId().equals(player2.getUniqueId()) ||
                            trade.getPlayer2().getUniqueId().equals(canceller.getUniqueId()) &&
                                    trade.getPlayer1().getUniqueId().equals(player2.getUniqueId());
            if (match) {
                iterator.remove();
                break;
            }
        }
        if (trade == null) return;

        if (canceller.isOnline()) { // trade may be canceled if they leave
            canceller.sendMessage(MessageManager.getMessage("trade.cancelPlayer"));
        }

        if (player2.isOnline()) {
            player2.sendMessage(MessageManager.getMessage("trade.canceledOther")
                    .replace("%player%", canceller.getDisplayName()));
        }

        if (canceller.isOnline()) canceller.closeInventory();
        if (player2.isOnline()) player2.closeInventory();
    }

    public boolean isTrading(Player player) {
        for (Trade trade : activeTrades) {
            if (trade.getPlayer1() == player || trade.getPlayer2() == player) return true;
        }
        return false;
    }

    public List<Trade> getActiveTrades() { return activeTrades; }
}
