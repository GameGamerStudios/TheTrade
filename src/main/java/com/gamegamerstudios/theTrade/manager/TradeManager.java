package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.api.Trade;
import com.gamegamerstudios.theTrade.api.TradeGUI;
import com.gamegamerstudios.theTrade.api.TradeListener;
import com.gamegamerstudios.theTrade.api.TradeRequest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class TradeManager {
    private final Plugin plugin;
    private final List<Trade> activeTrades = new ArrayList<>();
    public TradeManager(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new TradeListener(plugin, this), plugin);
    }

    public void newTrade(Player player1, Player player2) {
        activeTrades.add(new Trade(plugin, player1, player2));
    }
    public void cancelTrade(Player canceller) {
        Trade trade = getTrade(canceller);
        if (trade == null) return;

        Player other = trade.getPlayer1().equals(canceller)
                ? trade.getPlayer2()
                : trade.getPlayer1();
        trade.cancelCountdown();

        for (ItemStack item : trade.getItems().get(canceller.getUniqueId()).values()) {
            canceller.getInventory().addItem(item);
        }

        for (ItemStack item : trade.getItems().get(other.getUniqueId()).values()) {
            other.getInventory().addItem(item);
        }



        activeTrades.remove(trade);

        if (canceller.isOnline()) {
            canceller.sendMessage(MessageManager.getMessage("trade.cancelPlayer"));
        }

        if (other.isOnline()) {
            other.sendMessage(
                    MessageManager.getMessage("trade.canceledOther")
                            .replace("%player%", canceller.getDisplayName())
            );
        }

        if (canceller.isOnline()) canceller.closeInventory();
        if (other.isOnline()) other.closeInventory();
    }

    public void complete(Trade trade) {
        Iterator<Trade> iterator = activeTrades.iterator();

        while (iterator.hasNext()) {
            Trade listTrade = iterator.next();
            if (listTrade.equals(trade)) {
                listTrade.cancelCountdown();
                iterator.remove();
            }
        }
    }

    public Trade getTrade(Player player) {
        for (Trade trade : activeTrades) {
            if (trade.getPlayer1().equals(player) || trade.getPlayer2().equals(player)) {
                return trade;
            }
        }
        return null;
    }

    public boolean isTrading(Player player) {
        for (Trade trade : activeTrades) {
            if (trade.getPlayer1() == player || trade.getPlayer2() == player) {
                return !trade.isCompleting();
            }
        }
        return false;
    }

    public void shutdown() {
        for (Trade trade : activeTrades) {
            trade.cancelCountdown();
        }
    }

    public List<Trade> getActiveTrades() { return activeTrades; }
}
