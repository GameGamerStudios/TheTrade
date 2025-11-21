package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.ConfigManager;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import com.gamegamerstudios.theTrade.manager.TradeManager;
import com.gamegamerstudios.theTrade.util.Utils;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TradeListener implements Listener {
    private final Plugin plugin;
    private final TradeManager tradeManager;
    private final List<UUID> inputting = new ArrayList<>();
    public TradeListener(Plugin plugin, TradeManager tradeManager) {
        this.plugin = plugin;
        this.tradeManager = tradeManager;
    }
    @EventHandler
    public void onTradeClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().contains("You")) return;
        if (e.getClickedInventory() == null) return;
        Player p = (Player) e.getWhoClicked();

        Trade trade = tradeManager.getActiveTrades().stream().filter(t -> t.getPlayer1() == p || t.getPlayer2() == p).findFirst().orElse(null);
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

        if (plugin.getEconomyManager().getIsEnabled()) {
            if (plugin.getConfig().getBoolean("tradeInGameMoney") && e.getRawSlot() == 48) {
                inputting.add(p.getUniqueId());

                try {
                    SignGUI gui = SignGUI.builder()
                            .setLine(1, "↑↑↑↑↑↑↑↑↑↑")
                            .setLine(2, "INSERT NUMBER")
                            .setLine(3, "ABOVE")
                            .setHandler((pp, result) -> {
                                String line0 = result.getLine(0);

                                if (line0.isEmpty()) {
                                    return Arrays.asList(
                                            SignGUIAction.openInventory(plugin, trade.getInventory(p)),
                                            SignGUIAction.run(() ->
                                                    Bukkit.getScheduler().runTask(
                                                            plugin,
                                                            () -> inputting.remove(p.getUniqueId()
                                                            )))
                                    );
                                }

                                double amount = 0;
                                try {
                                    amount = Utils.parseMoney(line0);
                                } catch (NumberFormatException ex) {
                                    p.sendMessage(MessageManager.getMessage("trade.invalidMoneyInput"));
                                    return Arrays.asList(
                                            SignGUIAction.openInventory(plugin, trade.getInventory(p)),
                                            SignGUIAction.run(() ->
                                                    Bukkit.getScheduler().runTask(
                                                            plugin,
                                                            () -> inputting.remove(p.getUniqueId()
                                                            )))
                                    );
                                }

                                trade.setDeposit(p, amount);
                                return Arrays.asList(
                                        SignGUIAction.openInventory(plugin, trade.getInventory(p)),
                                        SignGUIAction.run(() ->
                                                Bukkit.getScheduler().runTask(
                                                        plugin,
                                                        () -> inputting.remove(p.getUniqueId()
                                                        )))
                                );
                            }).build();
                    gui.open(p);
                    return;
                } catch (SignGUIVersionException ex) {
                    Bukkit.getLogger().severe(MessageManager.getMessage("unsupportedVersion"));
                    return;
                }
            }
        }

        if (e.getRawSlot() == 45) {
            if (!trade.getReadyToComplete().contains(p.getUniqueId())) {
                trade.setReadyToComplete(p, true);
            } else {
                trade.setReadyToComplete(p, false);
            }
            trade.updateGUI();
            return;
        }

        if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
            if (e.getCurrentItem() == null) {
                for (String str : plugin.getConfig().getStringList("blacklistItems")) {
                    Material material = Material.matchMaterial(str);
                    if (material == null) { continue; }

                    if (material == e.getCursor().getType()) return;
                }
                trade.updateItem(p, e.getCursor().clone(), e.getRawSlot());
                e.setCursor(null);
                return;
            }

            // swap items
            ItemStack invItem = e.getCurrentItem().clone();
            if (!invItem.isSimilar(e.getCursor().clone())) {
                trade.updateItem(p, e.getCursor().clone(), e.getRawSlot());
                e.setCursor(invItem);
                return;
            }

            // ADD ITEMS IF AMOUNT ALLOWS
            int total = invItem.getAmount() + e.getCursor().clone().getAmount();
            if (total <= invItem.getMaxStackSize()) {
                ItemStack toUpdate = invItem.clone();
                toUpdate.setAmount(invItem.getAmount() + e.getCursor().getAmount());
                trade.updateItem(p, toUpdate, e.getRawSlot());
                e.setCursor(null);
                return;
            }

            //IF AMOUNT DOES NOT ALLOW, THEN GIVE REMAINDER TO PLAYER
            ItemStack toUpdate = invItem.clone();
            ItemStack cursor = e.getCursor().clone();
            toUpdate.setAmount(toUpdate.getMaxStackSize());
            cursor.setAmount(total - toUpdate.getMaxStackSize());
            trade.updateItem(p, toUpdate, e.getRawSlot());
            e.setCursor(cursor);

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
        if (inputting.contains(p.getUniqueId())) return;

        Trade trade = null;
        for (Trade t : tradeManager.getActiveTrades()) {
            if (t.getPlayer1() == p || t.getPlayer2() == p) {
                trade = t;
                break;
            }
        }
        if (trade == null) return;
        if (tradeManager.isTrading(p)) tradeManager.cancelTrade(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (plugin.getRequestManager().hasRequest(p.getUniqueId())) {
            plugin.getRequestManager().cancelRequests(p.getUniqueId());
            plugin.getRequestManager().denyRequest(p.getUniqueId());
        }

        Trade trade = null;
        for (Trade t : tradeManager.getActiveTrades()) {
            if (t.getPlayer1() == p || t.getPlayer2() == p) {
                trade = t;
                break;
            }
        }
        if (trade == null) return;
        if (tradeManager.isTrading(p)) tradeManager.cancelTrade(p);
    }
}
