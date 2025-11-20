package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class TradeGUI {
    private final Player player;
    private final Player otherPlayer;
    private final Trade trade;
    private final Inventory inv;
    public TradeGUI(Player player, Player otherPlayer, Trade trade) {
        this.player = player;
        this.otherPlayer = otherPlayer;
        this.trade = trade;
        this.inv = Bukkit.createInventory(null, 54,
                MessageManager.getMessage("gui.you") + " | " + MessageManager.getMessage("gui.otherPlayer")
                        .replace("%player%", otherPlayer.getDisplayName()));
        fillColum();
    }

    private void fillColum() {
        for (int i = 0; i < inv.getSize(); i++) {
            if ((i % 9) != 4) continue;
            ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1);
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName("");
            filler.setItemMeta(fillerMeta);
            inv.setItem(i, filler);
        }
    }

    public void updateItems() {
        inv.clear();
        fillColum();

        for (Map.Entry<Integer, ItemStack> e : trade.getItems().get(player.getUniqueId()).entrySet()) {
            inv.setItem(e.getKey(), e.getValue());
        }

        for (Map.Entry<Integer, ItemStack> e : trade.getItems().get(otherPlayer.getUniqueId()).entrySet()) {
            inv.setItem(e.getKey() + 5, e.getValue());
        }
    }

    public Inventory getInv() { return inv; }
}
