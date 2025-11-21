package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeGUI {
    private final Plugin plugin;
    private final Player player;
    private final Player otherPlayer;
    private final Trade trade;
    private final Inventory inv;
    public TradeGUI(Plugin plugin, Player player, Player otherPlayer, Trade trade) {
        this.plugin = plugin;
        this.player = player;
        this.otherPlayer = otherPlayer;
        this.trade = trade;
        this.inv = Bukkit.createInventory(null, 54,
                MessageManager.getMessage("gui.you") + " | " + MessageManager.getMessage("gui.otherPlayer")
                        .replace("%player%", otherPlayer.getDisplayName()));
        setupGUI();
    }

    private void setupGUI() {
        for (int i = 0; i < inv.getSize(); i++) {
            if ((i % 9) != 4) continue;
            ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1);
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName("");
            filler.setItemMeta(fillerMeta);
            inv.setItem(i, filler);
        }

        if (plugin.getEconomyManager().getIsEnabled()) {
            if (plugin.getConfig().getBoolean("tradeInGameMoney")) {
                inv.setItem(48, getMoneyItem());
                inv.setItem(50, getOtherPlayerMoneyItem());
            }
        }


        if (trade.getTradeTimer() == null || !trade.getTradeTimer().isCountdown()) {
            if (trade.getReadyToComplete().contains(player.getUniqueId())) {
                inv.setItem(45, getClickedItem());
            } else {
                inv.setItem(45, getReadyItem());
            }
        } else {
            inv.setItem(45, getCountdownItem());
        }
    }

    public void updateItems() {
        inv.clear();
        setupGUI();

        for (Map.Entry<Integer, ItemStack> e : trade.getItems().get(player.getUniqueId()).entrySet()) {
            inv.setItem(e.getKey(), e.getValue());
        }

        for (Map.Entry<Integer, ItemStack> e : trade.getItems().get(otherPlayer.getUniqueId()).entrySet()) {
            inv.setItem(e.getKey() + 5, e.getValue());
        }
    }

    private ItemStack getMoneyItem() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageManager.getMessage("gui.moneyitem.name"));
        List<String> t = MessageManager.getMessageList("gui.moneyitem.lore");
        List<String> lore = new ArrayList<>();
        for (String str : t) {
            lore.add(str
                    .replace("%amount%", trade.getDepositFormatted(player) + ""));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getOtherPlayerMoneyItem() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageManager.getMessage("gui.othermoneyitem.name").replace("%player%", otherPlayer.getDisplayName()));
        List<String> l = MessageManager.getMessageList("gui.othermoneyitem.lore");
        List<String> lore = new ArrayList<>();
        for (String str : l) {
            lore.add(str
                    .replace("%amount%", trade.getDepositFormatted(otherPlayer) + "")
                    .replace("%player%", otherPlayer.getDisplayName()));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getReadyItem() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageManager.getMessage("gui.readyItem.name"));
        List<String> lore = MessageManager.getMessageList("gui.readyItem.lore");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getCountdownItem() {
        ItemStack item = new ItemStack(Material.DIAMOND);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageManager.getMessage("gui.timerItem.name").replace("%time%",
                trade.getTradeTimer().getTimeLeft() + ""));
        List<String> lore = MessageManager.getMessageList("gui.timerItem.lore");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addEnchant(Enchantment.DEPTH_STRIDER, 1, true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack getClickedItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageManager.getMessage("gui.confirmedItem.name"));
        List<String> l = MessageManager.getMessageList("gui.confirmedItem.lore");
        List<String> lore = new ArrayList<>();
        for (String str : l) {
            lore.add(str
                    .replace("%player%", otherPlayer.getDisplayName()));
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public Inventory getInv() { return inv; }
}
