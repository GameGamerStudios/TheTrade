package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.TradeManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
        Player p = (Player) e.getWhoClicked();

        Trade trade = null;
        for (Trade t : tradeManager.getActiveTrades()) {
            if (t.getPlayer1() == p || t.getPlayer2() == p) {
                trade = t;
                break;
            }
        }
        if (trade == null) return;

        if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
            }
            return;
        }

        e.setCancelled(true);
        if ((e.getRawSlot() % 9) >= 4) {
            return;
        }

        if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
            trade.updateItem(p, e.getCursor().clone(), e.getRawSlot());
            e.setCursor(null);
            return;
        }

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR) {
            ItemStack clone = e.getCurrentItem().clone();
            trade.removeItem(p, e.getRawSlot());
            e.setCursor(clone);
            return;
        }
    }

    @EventHandler
    public void onTradeDrag(InventoryDragEvent e) {
        if (!e.getView().getTitle().contains("You")) return;

        for (int slot : e.getRawSlots()) {
            if (slot < e.getView().getTopInventory().getSize()) {
                e.setCancelled(true);
                return;
            }
        }
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
        if (tradeManager.isTrading(p)) tradeManager.cancelTrade(p, trade.getPlayer2());
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
        if (tradeManager.isTrading(p)) tradeManager.cancelTrade(p, trade.getPlayer2());
    }
}
