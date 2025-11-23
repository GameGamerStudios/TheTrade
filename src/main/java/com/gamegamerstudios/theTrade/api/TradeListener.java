package com.gamegamerstudios.theTrade.api;

import com.cryptomorin.xseries.XMaterial;
import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.ConfigManager;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import com.gamegamerstudios.theTrade.manager.TradeManager;
import com.gamegamerstudios.theTrade.util.InventoryViewCompact;
import com.gamegamerstudios.theTrade.util.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TradeListener implements Listener {
    private final Plugin plugin;
    private final TradeManager tradeManager;
    private final List<UUID> inputting = new ArrayList<>();
    private final Cache<UUID, Long> rightClicking = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();
    public TradeListener(Plugin plugin, TradeManager tradeManager) {
        this.plugin = plugin;
        this.tradeManager = tradeManager;
    }
    @EventHandler
    public void onTradeClick(InventoryClickEvent e) {
        String title = InventoryViewCompact.getTitle(e);
        if (!title.contains("You")) return;
        if (e.getClickedInventory() == null) return;
        Player p = (Player) e.getWhoClicked();

        Trade trade = tradeManager.getActiveTrades()
                .stream()
                .filter(t -> t.getPlayer1() == p || t.getPlayer2() == p)
                .findFirst()
                .orElse(null);
        if (trade == null) return;

        int topSize = InventoryViewCompact.getTopInventory(e).getSize();
        if (e.getRawSlot() >= topSize) {
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
                if (trade.getTradeTimer().isRunning()) {
                    e.setCancelled(true);
                    Bukkit.getLogger().info(trade.getTradeTimer().isRunning()  + " yes or no?");
                    return;
                }

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

                                if (amount > plugin.getEconomyManager().getEconomy().getBalance(p)) {
                                    p.sendMessage(MessageManager.getMessage("trade.noMoney"));
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
                    Bukkit.getLogger().severe(MessageManager.getMessage("general.unsupportedVersion"));
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

        if (trade.getTradeTimer().isRunning()) {
            e.setCancelled(true);
            return;
        }

        if (e.getCursor() != null && !Utils.isAir(e.getCursor())) {
            for (String str : plugin.getConfig().getStringList("blacklistItems")) {
                XMaterial mat = XMaterial.matchXMaterial(str).orElse(null);
                if (mat == null) continue;

                Material material = mat.parseMaterial();
                if (material == null) { continue; }
                Material currentType = (e.getCurrentItem() == null ? Material.AIR : e.getCurrentItem().getType());


                if (material == e.getCursor().getType() || material == currentType) {
                    p.sendMessage(MessageManager.getMessage("trade.blacklistedItem"));
                    e.setCancelled(true);
                    return;
                }
            }

            if (e.getCurrentItem() == null) {
                trade.updateItem(p, e.getCursor().clone(), e.getRawSlot());
                e.setCursor(null);
                return;
            }

            // swap items
            ItemStack invItem = e.getCurrentItem().clone();
            if (!invItem.getType().equals(e.getCursor().getType())) {
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

        if (e.getCurrentItem() != null && !Utils.isAir(e.getCurrentItem())) {
            ItemStack clone = e.getCurrentItem().clone();
            trade.removeItem(p, e.getRawSlot());
            e.setCursor(clone);
            return;
        }
    }

    @EventHandler
    public void onTradeDrag(InventoryDragEvent e) {
        if (!InventoryViewCompact.getTitle(e).contains("You")) return;

        for (int slot : e.getRawSlots()) {
            if (slot < InventoryViewCompact.getTopInventory(e).getSize()) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onTradeClose(InventoryCloseEvent e) {
        if (!InventoryViewCompact.getTitle(e).contains("You")) return;
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
        if (tradeManager.isTrading(p)) tradeManager.cancelTrade(p, false);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (plugin.getRequestManager().hasRequest(p.getUniqueId())) {
            plugin.getRequestManager().denyRequest(p.getUniqueId());
        }
        plugin.getRequestManager().cancelRequests(e.getPlayer().getUniqueId(), false);

        Trade trade = null;
        for (Trade t : tradeManager.getActiveTrades()) {
            if (t.getPlayer1() == p || t.getPlayer2() == p) {
                trade = t;
                break;
            }
        }
        if (trade == null) return;
        if (tradeManager.isTrading(p)) tradeManager.cancelTrade(p, false);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().hasPermission("thetrade.admin.updatenotify")) return;
        if (!plugin.getConfig().getBoolean("updateChecker")) return;
        if (!plugin.getUpdateChecker().updateAvailable()) return;

        BaseComponent[] components = TextComponent.fromLegacyText(MessageManager.getMessage("update.availableInGame")
                        .replace("%url%", "https://www.spigotmc.org/resources/" + plugin.getResourceId()));
        BaseComponent[] hoverText = new ComponentBuilder(MessageManager.getMessage("update.inGameHover"))
                .create();
        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL,
                "https://www.spigotmc.org/resources/" + plugin.getResourceId());
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
        for (BaseComponent b : components) {
            b.setClickEvent(click);
            b.setHoverEvent(hover);
        }

        e.getPlayer().spigot().sendMessage(components);
    }

    @EventHandler
    public void onShiftTrade(PlayerInteractAtEntityEvent e) {
        if (!plugin.getConfig().getBoolean("shiftClickTrade")) return;
        if (!(e.getRightClicked() instanceof Player)) return;
        Player p = e.getPlayer();
        if (!p.isSneaking()) return;
        if (rightClicking.asMap().containsKey(p.getUniqueId())) return;
        Player target = (Player) e.getRightClicked();

        if (plugin.getDataManager().isBanned(p.getUniqueId())) {
            p.sendMessage(MessageManager.getMessage("command.trade.banned"));
            return;
        }
        if (plugin.getDataManager().isBanned(target.getUniqueId())) {
            p.sendMessage(MessageManager.getMessage("command.trade.playerBanned"));
            return;
        }

        if (plugin.getRequestManager().hasRequest(p.getUniqueId(), target.getUniqueId())) {
            plugin.getRequestManager().acceptRequest(target.getUniqueId(), p.getUniqueId());
            return;
        }

        rightClicking.put(p.getUniqueId(), 1000L);
        plugin.getRequestManager().newRequest(
                p.getUniqueId(),
                p.getDisplayName(),
                target.getUniqueId(),
                target.getDisplayName()
        );
    }
}
