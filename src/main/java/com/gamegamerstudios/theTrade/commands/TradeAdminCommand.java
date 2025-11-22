package com.gamegamerstudios.theTrade.commands;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.api.Trade;
import com.gamegamerstudios.theTrade.api.TradeRequest;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import com.sun.org.apache.xalan.internal.res.XSLTErrorResources_en;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TradeAdminCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    public TradeAdminCommand(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("thetrade.admin.command")) {
            sender.sendMessage(MessageManager.getMessage("general.noPermission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(MessageManager.getMessage("command.admin.usage"));
            return true;
        }

        switch (args[0].toUpperCase()) {
            case "ACTIVETRADES":

                if (sender instanceof Player) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.activetrades.title")
                            .replace("%amount%", plugin.getTradeManager().getActiveTrades().size() + ""));
                } else {
                    sender.sendMessage(MessageManager.getMessage("command.admin.activetradesconsole.title")
                            .replace("%amount%", plugin.getTradeManager().getActiveTrades().size() + ""));
                }

                int id = 1;
                for (Trade trade : plugin.getTradeManager().getActiveTrades()) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(MessageManager.getMessage("command.admin.activetradesconsole.format")
                                .replace("%id%", id + "")
                                .replace("%player1%", trade.getPlayer1().getDisplayName())
                                .replace("%player2%", trade.getPlayer2().getDisplayName()));
                        id++;
                        continue;
                    }

                    BaseComponent[] components = TextComponent.fromLegacyText(
                            MessageManager.getMessage("command.admin.activetrades.format")
                                    .replace("%id%", id + "")
                                    .replace("%player1%", trade.getPlayer1().getDisplayName())
                                    .replace("%player2%", trade.getPlayer2().getDisplayName()));
                    BaseComponent[] hoverText = new ComponentBuilder(MessageManager.getMessage("command.admin.cancelHover"))
                            .create();
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
                    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradeadmin canceltrade " +
                            trade.getPlayer1().getDisplayName());
                    for (BaseComponent b : components) {
                        b.setHoverEvent(hoverEvent);
                        b.setClickEvent(clickEvent);
                    }
                    ((Player) sender).spigot().sendMessage(components);
                    id++;
                }

                return true;
            case "ACTIVEREQUESTS":

                if (sender instanceof Player) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.activerequests.title")
                            .replace("%amount%",
                                    plugin.getRequestManager().getRequests().asMap().keySet().size() + ""));
                } else {
                    sender.sendMessage(MessageManager.getMessage("command.admin.activerequestsconsole.title")
                            .replace("%amount%",
                            plugin.getRequestManager().getRequests().asMap().keySet().size() + ""));
                }

                int idd = 1;
                for (TradeRequest request : plugin.getRequestManager().getRequests().asMap().keySet()) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(MessageManager.getMessage("command.admin.activerequestsconsole.format")
                                .replace("%id%", idd + "")
                                .replace("%player1%", request.getRequesterDisplay())
                                .replace("%player2%", request.getRequestedDisplay()));
                        idd++;
                        continue;
                    }

                    BaseComponent[] components = TextComponent.fromLegacyText(
                            MessageManager.getMessage("command.admin.activerequests.format")
                            .replace("%id%", idd + "")
                            .replace("%player1%", request.getRequesterDisplay())
                            .replace("%player2%", request.getRequestedDisplay()));
                    BaseComponent[] hoverText = new ComponentBuilder(MessageManager.getMessage("command.admin.cancelHover"))
                            .create();
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
                    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tradeadmin cancelrequest " +
                            request.getRequesterDisplay());
                    for (BaseComponent b : components) {
                        b.setHoverEvent(hoverEvent);
                        b.setClickEvent(clickEvent);
                    }
                    ((Player) sender).spigot().sendMessage(components);
                    idd++;
                }

                return true;
            case "CANCELTRADE":
                if (!sender.hasPermission("thetrade.admin.canceltrades")) {
                    sender.sendMessage(MessageManager.getMessage("general.noPermission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.canceltradeusage"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(MessageManager.getMessage("general.notOnline"));
                    return true;
                }

                Trade trade = plugin.getTradeManager().getTrade(target);
                if (trade == null) {
                    sender.sendMessage("command.admin.nottrading");
                    return true;
                }

                plugin.getTradeManager().cancelTrade(target, true);
                sender.sendMessage(MessageManager.getMessage("command.admin.tradecanceled")
                        .replace("%player%", target.getDisplayName()));
                return true;
            case "CANCELREQUEST":
                if (!sender.hasPermission("thetrade.admin.cancelrequests")) {
                    sender.sendMessage(MessageManager.getMessage("general.noPermission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.cancelrequestusage"));
                    return true;
                }

                Player t = Bukkit.getPlayer(args[1]);
                if (t == null || !t.isOnline()) {
                    sender.sendMessage(MessageManager.getMessage("general.notOnline"));
                    return true;
                }

                if (!plugin.getRequestManager().hasOutgoingRequest(t)) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.nooutgoingrequest"));
                    return true;
                }

                plugin.getRequestManager().cancelRequests(t.getUniqueId(), true);
                sender.sendMessage(MessageManager.getMessage("command.admin.requestcanceled")
                        .replace("%player%", t.getDisplayName()));
                return true;
            case "HELP":
                for (String str : MessageManager.getMessageList("command.admin.help")) {
                    sender.sendMessage(str);
                }
                return true;
            case "BAN":
                if (!sender.hasPermission("thetrade.admin.ban")) {
                    sender.sendMessage(MessageManager.getMessage("general.noPermission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.banusage"));
                    return true;
                }

                OfflinePlayer banned;
                if (Bukkit.getPlayer(args[1]) != null && Bukkit.getPlayer(args[1]).isOnline()) {
                    banned = Bukkit.getPlayer(args[1]);
                } else {
                    banned = Bukkit.getOfflinePlayer(args[1]);
                }

                if (plugin.getDataManager().isBanned(banned.getUniqueId())) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.alreadyBanned"));
                    return true;
                }

                plugin.getDataManager().ban(banned, sender);
                sender.sendMessage(MessageManager.getMessage("command.admin.banned")
                        .replace("%player%", ((banned instanceof Player) ? Bukkit.getPlayer(args[1]).getDisplayName()
                        : banned.getName())));
                return true;
            case "UNBAN":
                if (!sender.hasPermission("thetrade.admin.unban")) {
                    sender.sendMessage(MessageManager.getMessage("general.noPermission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.unbanusage"));
                    return true;
                }

                OfflinePlayer b;
                if (Bukkit.getPlayer(args[1]) != null && Bukkit.getPlayer(args[1]).isOnline()) {
                    b = Bukkit.getPlayer(args[1]);
                } else {
                    b = Bukkit.getOfflinePlayer(args[1]);
                }

                if (!plugin.getDataManager().isBanned(b.getUniqueId())) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.notBanned"));
                    return true;
                }

                plugin.getDataManager().unban(b, sender);
                return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            results.add("help");
            results.add("activetrades");
            results.add("activerequests");
            if (sender.hasPermission("thetrade.admin.canceltrades")) results.add("canceltrade");
            if (sender.hasPermission("thetrade.admin.cancelrequests")) results.add("cancelrequest");
            if (sender.hasPermission("thetrade.admin.ban")) results.add("ban");
            if (sender.hasPermission("thetrade.admin.unban")) results.add("unban");
            return StringUtil.copyPartialMatches(args[0], results, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("canceltrade") && sender.hasPermission("thetrade.admin.canceltrades")) {
            for (Trade trade : plugin.getTradeManager().getActiveTrades()) {
                if (!results.contains(trade.getPlayer1().getDisplayName())) {
                    results.add(trade.getPlayer1().getDisplayName());
                }

                if (!results.contains(trade.getPlayer2().getDisplayName())) {
                    results.add(trade.getPlayer2().getDisplayName());
                }
            }
            return StringUtil.copyPartialMatches(args[1], results, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("cancelrequest") && sender.hasPermission("thetrade.admin.cancelrequests")) {
            for (TradeRequest request : plugin.getRequestManager().getRequests().asMap().keySet()) {
                if (results.contains(request.getRequesterDisplay())) continue;
                results.add(request.getRequesterDisplay());
            }
            return StringUtil.copyPartialMatches(args[1], results, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("ban") && sender.hasPermission("thetrade.admin.ban")) {
            Bukkit.getOnlinePlayers().forEach(player -> results.add(player.getDisplayName()));
            return StringUtil.copyPartialMatches(args[1], results, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("unban") && sender.hasPermission("thetrade.admin.unban")) {
            return StringUtil.copyPartialMatches(args[1], plugin.getDataManager().getBannedNames(), new ArrayList<>());
        }

        return results;
    }
}
