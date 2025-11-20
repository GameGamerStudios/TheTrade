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
    private List<Trade> activeTrades = new ArrayList<>();
    public TradeManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new TradeListener(this), plugin);
    }

    public void newTrade(Player player1, Player player2) {
        activeTrades.add(new Trade(player1, player2));
    }
    public void cancelTrade(Player canceller, Player player2) {
        for (Trade trade : activeTrades) {
            List<UUID> tPlayers = new ArrayList<>();
            tPlayers.add(trade.getPlayer1().getUniqueId());
            tPlayers.add(trade.getPlayer2().getUniqueId());

            if (!tPlayers.contains(canceller.getUniqueId()) || !tPlayers.contains(player2.getUniqueId())) continue;

            if (canceller.isOnline()) { // trade may be canceled if they leave
                canceller.sendMessage(MessageManager.getMessage("trade.cancelPlayer"));
                canceller.closeInventory();
            }

            player2.sendMessage(MessageManager.getMessage("trade.canceledOther"));
            player2.closeInventory();
        }

        Iterator<Trade> iterator = activeTrades.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getPlayer1().getUniqueId().equals(canceller.getUniqueId())
                    && iterator.next().getPlayer2().getUniqueId().equals(player2.getUniqueId())) {
                iterator.remove();
            }
        }
    }

    public boolean isTrading(Player player) {
        for (Trade trade : activeTrades) {
            if (trade.getPlayer1() == player || trade.getPlayer2() == player) return true;
        }
        return false;
    }

    public List<Trade> getActiveTrades() { return activeTrades; }
}
