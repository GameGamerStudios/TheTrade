package com.gamegamerstudios.theTrade.commands;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.api.Trade;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TradeAdminCommand implements CommandExecutor, TabCompleter {
    private final Plugin plugin;
    public TradeAdminCommand(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MessageManager.getMessage("command.admin.usage"));
            return true;
        }

        switch (args[0].toUpperCase()) {
            case "ACTIVETRADES":

                sender.sendMessage(MessageManager.getMessage("command.admin.activetrades.title")
                        .replace("%amount%", plugin.getTradeManager().getActiveTrades().size() + ""));

                int id = 1;
                for (Trade trade : plugin.getTradeManager().getActiveTrades()) {
                    sender.sendMessage(MessageManager.getMessage("command.admin.activetrades.format")
                            .replace("%id%", id + "")
                            .replace("%player1%", trade.getPlayer1().getDisplayName())
                            .replace("%player2%", trade.getPlayer2().getDisplayName()));
                    id++;
                }

                return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            results.add("activetrades");
            return results;
        }

        return results;
    }
}
