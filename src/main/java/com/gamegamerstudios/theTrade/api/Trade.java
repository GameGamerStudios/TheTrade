package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Trade {
    private final Plugin plugin;
    private final Player player1;
    private final Player player2;
    private double player1Despoit;
    private double player2Deposit;
    private final TradeGUI player1Inv;
    private final TradeGUI player2Inv;
    private final List<UUID> readyToComplete = new ArrayList<>();
    private TradeTimer tradeCompleteTimer;
    private boolean completing = false;
    private HashMap<UUID, HashMap<Integer, ItemStack>> items = new HashMap<>();
    public Trade(Plugin plugin, Player player1, Player player2) {
        this.plugin = plugin;
        this.player1 = player1;
        this.player2 = player2;
        this.tradeCompleteTimer = new TradeTimer(this, plugin, plugin.getConfig().getInt("completeTimer", 3));
        this.player1Despoit = 0;
        this.player2Deposit = 0;
        this.player1Inv = new TradeGUI(plugin, player1, player2, this);
        this.player2Inv = new TradeGUI(plugin, player2, player1, this);

        items.put(player1.getUniqueId(), new HashMap<>());
        items.put(player2.getUniqueId(), new HashMap<>());

        player1.openInventory(player1Inv.getInv());
        player2.openInventory(player2Inv.getInv());
    }

    public void complete() {
        completing = true;

        player1.closeInventory();
        player2.closeInventory();

        if (plugin.getEconomyManager().getIsEnabled()) {
            plugin.getEconomyManager().getEconomy().depositPlayer(player1, player2Deposit);
            plugin.getEconomyManager().getEconomy().depositPlayer(player2, player1Despoit);
            plugin.getEconomyManager().getEconomy().withdrawPlayer(player1, player1Despoit);
            plugin.getEconomyManager().getEconomy().withdrawPlayer(player2, player2Deposit);
        }

        for (ItemStack item : items.get(player2.getUniqueId()).values()) {
            if (Utils.getOpenSlots(player1.getInventory()) <= 0) {
                player1.getWorld().dropItemNaturally(player1.getLocation(), item);
                continue;
            }
            player1.getInventory().addItem(item);
        }

        for (ItemStack item : items.get(player1.getUniqueId()).values()) {
            if (Utils.getOpenSlots(player2.getInventory()) <= 0) {
                player2.getWorld().dropItemNaturally(player2.getLocation(), item);
                continue;
            }
            player2.getInventory().addItem(item);
        }

        cancelCountdown();
        player1.closeInventory();
        player2.closeInventory();

        plugin.getTradeManager().complete(this);
    }

    public void updateItem(Player owner, ItemStack item, int slot) {
        items.get(owner.getUniqueId()).put(slot, item);
        updateGUI();
    }

    public void removeItem(Player owner, int slot) {
        items.get(owner.getUniqueId()).remove(slot);
        updateGUI();
    }

    public void setDeposit(Player player, double deposit) {
        if (player == player1) {
            player1Despoit = deposit;
        } else {
            player2Deposit = deposit;
        }
        updateGUI();
    }

    public Inventory getInventory(Player player) {
        if (player == player1) {
            return player1Inv.getInv();
        } else {
            return player2Inv.getInv();
        }
    }

    public void startCountdown() {
        if (!readyToComplete.contains(player1.getUniqueId()) || !readyToComplete.contains(player2.getUniqueId())) return;
        this.tradeCompleteTimer.start();
    }

    public void cancelCountdown() {
        if (this.tradeCompleteTimer != null) tradeCompleteTimer.cancel();
        tradeCompleteTimer = null;
    }

    public void updateGUI() { player1Inv.updateItems(); player2Inv.updateItems(); }

    public void setReadyToComplete(Player player, boolean ready) {
        if (player == player1 && ready) {
            if (!readyToComplete.contains(player.getUniqueId())) readyToComplete.add(player.getUniqueId());
        }
        if (player == player2 && ready) {
            if (!readyToComplete.contains(player.getUniqueId())) readyToComplete.add(player.getUniqueId());
        }
        if (player == player1 && !ready) {
            readyToComplete.remove(player.getUniqueId());
        }
        if (player == player2 && !ready) {
            readyToComplete.remove(player.getUniqueId());
        }

        startCountdown();
    }

    public boolean isCompleting() { return completing; };
    public TradeTimer getTradeTimer() { return tradeCompleteTimer; }
    public List<UUID> getReadyToComplete() { return readyToComplete; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public double getDeposit(Player player) { return player == player1 ? player1Despoit : player2Deposit; }
    public String getDepositFormatted(Player player) { return player == player1 ? Utils.format(player1Despoit) : Utils.format(player2Deposit); }
    public HashMap<UUID, HashMap<Integer, ItemStack>> getItems() { return items; }
}
