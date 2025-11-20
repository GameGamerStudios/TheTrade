package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.TradeManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TradeListener implements Listener {
    private final TradeManager tradeManager;
    public TradeListener(TradeManager tradeManager) {
        this.tradeManager = tradeManager;
    }
    @EventHandler
    public void onTradeClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("You")) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) return;
        Player p = (Player) e.getWhoClicked();

        Trade trade = null;
        for (Trade t : tradeManager.getActiveTrades()) {
            if (t.getPlayer1() == p || t.getPlayer2() == p) {
                trade = t;
                break;
            }
        }
        if (trade == null) return;

        if ((e.getRawSlot() % 9) >= 4) {
            e.setCancelled(true);
            return;
        }
        if (e.getRawSlot() > e.getView().getTopInventory().getSize()) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        trade.updateItem(p, e.getCurrentItem(), e.getRawSlot());
    }

    @EventHandler
    public void onTradeClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().contains("You")) return;
        Player p = (Player) e.getPlayer();

        Trade trade = null;
        for (Trade t : tradeManager.getActiveTrades()) {
            if (t.getPlayer1() == p || t.getPlayer2() == p) {
                trade = t;
                break;
            }
        }
        if (trade == null) return;
        tradeManager.cancelTrade(p, trade.getPlayer2());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Trade trade = null;
        for (Trade t : tradeManager.getActiveTrades()) {
            if (t.getPlayer1() == p || t.getPlayer2() == p) {
                trade = t;
                break;
            }
        }
        if (trade == null) return;
        tradeManager.cancelTrade(p, trade.getPlayer2());
    }
}
