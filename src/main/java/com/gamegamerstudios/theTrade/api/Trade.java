package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import com.gamegamerstudios.theTrade.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.security.MessageDigest;
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
            plugin.getEconomyManager().getEconomy().withdrawPlayer(player1, player1Despoit);
            plugin.getEconomyManager().getEconomy().withdrawPlayer(player2, player2Deposit);
            plugin.getEconomyManager().getEconomy().depositPlayer(player1, player2Deposit);
            plugin.getEconomyManager().getEconomy().depositPlayer(player2, player1Despoit);
        }

        player1.sendMessage(MessageManager.getMessage("trade.complete"));
        player2.sendMessage(MessageManager.getMessage("trade.complete"));
        for (ItemStack item : items.get(player2.getUniqueId()).values()) {
            Utils.giveItem(player1, item);
            String itemDisplay = (item.getItemMeta() != null && item.hasItemMeta() ? item.getItemMeta().getDisplayName()
                    : Utils.getVanillaName(item));
            player1.sendMessage(MessageManager.getMessage("trade.summaryPositive")
                    .replace("%item%", itemDisplay)
                    .replace("%amount%", item.getAmount() + ""));
            player2.sendMessage(MessageManager.getMessage("trade.summaryNegative")
                    .replace("%item%", itemDisplay)
                    .replace("%amount%", item.getAmount() + ""));
        }

        for (ItemStack item : items.get(player1.getUniqueId()).values()) {
            Utils.giveItem(player2, item);
            String itemDisplay = (item.getItemMeta() != null && item.hasItemMeta() ? item.getItemMeta().getDisplayName()
                    : Utils.getVanillaName(item));
            player2.sendMessage(MessageManager.getMessage("trade.summaryPositive")
                    .replace("%item%", itemDisplay)
                    .replace("%amount%", item.getAmount() + ""));
            player1.sendMessage(MessageManager.getMessage("trade.summaryNegative")
                    .replace("%item%", itemDisplay)
                    .replace("%amount%", item.getAmount() + ""));
        }

        if (player1Despoit > 0) {
            player1.sendMessage(MessageManager.getMessage("trade.summaryNegativeMoney")
                    .replace("%amount%", player1Despoit + ""));
            player2.sendMessage(MessageManager.getMessage("trade.summaryPositiveMoney")
                    .replace("%amount%", player1Despoit + ""));
        }
        if (player2Deposit > 0) {
            player2.sendMessage(MessageManager.getMessage("trade.summaryNegativeMoney")
                    .replace("%amount%", player2Deposit + ""));
            player1.sendMessage(MessageManager.getMessage("trade.summaryPositiveMoney")
                    .replace("%amount%", player2Deposit + ""));
        }


        cancelCountdown();
        player1.closeInventory();
        player2.closeInventory();

        plugin.getDataManager().logTrade(player1, player2, items, player1Despoit, player2Deposit);

        items.clear();
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
        if (this.tradeCompleteTimer == null) {
            this.tradeCompleteTimer = new TradeTimer(this, plugin, plugin.getConfig().getInt("completeTimer", 3));
        }
        this.tradeCompleteTimer.start();
    }

    public void cancelCountdown() {
        if (this.tradeCompleteTimer != null && this.tradeCompleteTimer.isRunning()) tradeCompleteTimer.end();
        this.tradeCompleteTimer = null;
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

        if (ready) {
            startCountdown();
        } else {
            cancelCountdown();
        }
    }

    public boolean isCompleting() { return completing; };
    public TradeTimer getTradeTimer() { return tradeCompleteTimer; }
    public List<UUID> getReadyToComplete() { return readyToComplete; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public double getDeposit(Player player) { return player == player1 ? player1Despoit : player2Deposit; }
    public String getDepositFormatted(Player player) { return player == player1 ? Utils.formatDate(player1Despoit) : Utils.formatDate(player2Deposit); }
    public HashMap<UUID, HashMap<Integer, ItemStack>> getItems() { return items; }
}
