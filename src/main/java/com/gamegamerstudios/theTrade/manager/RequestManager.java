package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.api.TradeRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.xml.XmlEscapers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RequestManager {
    private final Plugin plugin;
    private final int REQUEST_DURATION;
    private Cache<TradeRequest, Long> requests;
    public RequestManager(Plugin plugin) {
        this.plugin = plugin;
        this.REQUEST_DURATION = plugin.getConfig().getInt("requestDuration");
        this.requests = CacheBuilder.newBuilder().expireAfterWrite(REQUEST_DURATION, TimeUnit.SECONDS).build();
    }

    public void newRequest(UUID requester, String requesterDisplay, UUID requested, String requestedDisplay) {
        requests.put(new TradeRequest(requester, requesterDisplay, requested, requestedDisplay, plugin), REQUEST_DURATION * 1000L);

        Player player = Bukkit.getPlayer(requested);
        if (player == null || !player.isOnline()) {
            denyRequest(requested);
            return;
        }

        player.sendMessage(MessageManager.getMessage("trade.newRequest"));
    }

    public void cancelRequest(UUID requester) {
        Iterator<TradeRequest> iterator = requests.asMap().keySet().iterator();

        while (iterator.hasNext()) {
            TradeRequest request = iterator.next();
            if (request.getRequesterUUID().equals(requester)) {
                iterator.remove();
            }
        }
    }

    public void denyRequest(UUID requested) {
        Iterator<TradeRequest> iterator = requests.asMap().keySet().iterator();

        while (iterator.hasNext()) {
            TradeRequest request = iterator.next();
            if (request.getRequestedUUID().equals(requested)) {
                iterator.remove();
            }
        }
    }

    public void denyRequest(UUID requested, UUID requester) {
        TradeRequest request = null;

        for (TradeRequest r : requests.asMap().keySet()) {
            if (r.getRequesterUUID().equals(requester) && r.getRequestedUUID().equals(requested)) {
                request = r;
            }
        }

        if (request != null) {
            request.cancelRequest();
            requests.invalidate(request);
        }

        Player requesterPlayer = Bukkit.getPlayer(requester);
        if (requesterPlayer != null && requesterPlayer.isOnline()) {
            requesterPlayer.sendMessage(MessageManager.getMessage("trade.requesterDenied"));
        }

        Player requestedPlayer = Bukkit.getPlayer(requested);
        if (requestedPlayer != null && requesterPlayer.isOnline()) {
            requestedPlayer.sendMessage(MessageManager.getMessage("trade.requestedDenied"));
        }
    }

    public void acceptRequest(UUID requester, UUID requested) {
        TradeRequest request = null;

        for (TradeRequest r : requests.asMap().keySet()) {
            if (r.getRequesterUUID().equals(requester) && r.getRequestedUUID().equals(requested)) {
                request = r;
            }
        }

        if (request == null) {
            return;
        }

        request.cancelRequest();
        requests.invalidate(request);

        Player requesterPlayer = Bukkit.getPlayer(requester);
        if (requesterPlayer == null || !requesterPlayer.isOnline()) {
            requesterPlayer = null;
        }
        Player requestedPlayer = Bukkit.getPlayer(requested);
        if (requestedPlayer == null || !requestedPlayer.isOnline()) {
            requestedPlayer = null;
        }

        if (requesterPlayer == null || requestedPlayer == null) {
            if (requesterPlayer == null) {
                requestedPlayer.sendMessage(MessageManager.getMessage("trade.playerOffline")
                        .replace("%player%", request.getRequesterDisplay()));
            }

            if (requestedPlayer == null) {
                requesterPlayer.sendMessage(MessageManager.getMessage("trade.playerOffline")
                        .replace("%player%", request.getRequestedDisplay()));
            }
            return;
        }

        plugin.getTradeManager().newTrade(requesterPlayer, requestedPlayer);
    }

    public void shutdown() {
        for (TradeRequest request : requests.asMap().keySet()) {
            request.cancelRequest();
        }
    }
}
