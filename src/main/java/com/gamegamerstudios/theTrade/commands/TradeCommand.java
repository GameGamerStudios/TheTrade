package com.gamegamerstudios.theTrade.commands;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {
    private final Plugin plugin;
    public TradeCommand(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MessageManager.getMessage("general.notPlayer"));
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage(MessageManager.getMessage("command.trade.usage"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            p.sendMessage(MessageManager.getMessage("general.notOnline"));
            return true;
        }



        return false;
    }
}
