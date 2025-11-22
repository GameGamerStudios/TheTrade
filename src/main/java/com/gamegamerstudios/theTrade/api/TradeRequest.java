package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class TradeRequest {
    private final UUID requesterUUID;
    private final String requesterDisplay;
    private final UUID requestedUUID;
    private final String requestedDisplay;
    private final Plugin plugin;
    private BukkitTask task;
    public TradeRequest(UUID requesterUUID, String requesterDisplay, UUID requestedUUID, String requestedDisplay, Plugin plugin) {
        this.requesterUUID = requesterUUID;
        this.requesterDisplay = requesterDisplay;
        this.requestedUUID = requestedUUID;
        this.requestedDisplay = requestedDisplay;
        this.plugin = plugin;

        this.task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int timeLeft = plugin.getConfig().getInt("requestDuration") - 1;
            @Override
            public void run() {
                if (timeLeft == 0) {
                    Player requesterPlayer = Bukkit.getPlayer(requesterUUID);
                    if (requesterPlayer != null && requesterPlayer.isOnline()) {
                        requesterPlayer.sendMessage(MessageManager.getMessage("trade.sentExpired")
                                .replace("%player%", requestedDisplay));
                    }

                    Player requestedPlayer = Bukkit.getPlayer(requestedUUID);
                    if (requestedPlayer != null && requestedPlayer.isOnline()) {
                        requestedPlayer.sendMessage(MessageManager.getMessage("trade.receivedExpired")
                                .replace("%player%", requesterDisplay));
                    }

                    cancelRequest(false, false);
                }
                timeLeft--;
            }
        },
                0, 20);
    }

    public void cancelRequest(boolean fromPlayer, boolean byAdmin) {
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = null;

        Player target = Bukkit.getPlayer(requestedUUID);
        Player otherTarget = Bukkit.getPlayer(requesterUUID);

        if (fromPlayer) {
            if (target != null && target.isOnline()) {
                target.sendMessage(MessageManager.getMessage("trade.playerRequestCancel")
                        .replace("%player%", requesterDisplay));
            }
        }

        if (byAdmin) {
            if (otherTarget != null && otherTarget.isOnline()) {
                otherTarget.sendMessage(MessageManager.getMessage("trade.admincancelrequestRequesterPOV")
                        .replace("%requested%", requestedDisplay));
            }

            if (target != null && target.isOnline()) {
                target.sendMessage(MessageManager.getMessage("trade.admincancelrequestRequestedPOV")
                        .replace("%requester%", requesterDisplay));
            }
        }
    }

    public UUID getRequesterUUID() { return requesterUUID; }
    public String getRequesterDisplay() { return requesterDisplay; }
    public UUID getRequestedUUID() { return requestedUUID; }
    public String getRequestedDisplay() { return requestedDisplay; }
}
