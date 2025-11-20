package com.gamegamerstudios.theTrade.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class Trade {
    private final Player player1;
    private final Player player2;
    private final TradeGUI player1Inv;
    private final TradeGUI player2Inv;
    private HashMap<UUID, HashMap<Integer, ItemStack>> items = new HashMap<>();
    public Trade(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1Inv = new TradeGUI(player1, player2, this);
        this.player2Inv = new TradeGUI(player2, player1, this);

        items.put(player1.getUniqueId(), new HashMap<>());
        items.put(player2.getUniqueId(), new HashMap<>());

        player1.openInventory(player1Inv.getInv());
        player2.openInventory(player2Inv.getInv());
    }

    public void updateItem(Player owner, ItemStack item, int slot) {
        items.get(owner.getUniqueId()).put(slot, item);
        player1Inv.updateItems();
        player2Inv.updateItems();
    }

    public void removeItem(Player owner, int slot) {
        items.get(owner.getUniqueId()).remove(slot);
        player1Inv.updateItems();
        player2Inv.updateItems();
    }

    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public HashMap<UUID, HashMap<Integer, ItemStack>> getItems() { return items; }
}
