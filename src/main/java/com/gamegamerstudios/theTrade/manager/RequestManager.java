package com.gamegamerstudios.theTrade.manager;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.api.TradeRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.xml.XmlEscapers;
import jdk.incubator.vector.ShortVector;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
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

        Player requesterPlayer = Bukkit.getPlayer(requester);
        if (requesterPlayer != null && requesterPlayer.isOnline()) {
            requesterPlayer.sendMessage(MessageManager.getMessage("trade.sent")
                    .replace("%player%", requestedDisplay)
                    .replace("%time%", REQUEST_DURATION + ""));
        }

        BaseComponent[] components = TextComponent.fromLegacyText(
                MessageManager.getMessage("trade.newRequest").replace("%player%", requesterDisplay)
        );

        BaseComponent[] hoverText = new ComponentBuilder(
                new ComponentBuilder(MessageManager.getMessage("trade.acceptCommandHoverMsg")
                        .replace("%player%", requesterDisplay))
        ).create();
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade " + requesterDisplay);
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
        for (BaseComponent b : components) {
            b.setClickEvent(click);
            b.setHoverEvent(hover);
        }

        player.spigot().sendMessage(components);
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
            request.cancelRequest(false);
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

        request.cancelRequest(false);
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

        cancelRequests(requester);
        for (TradeRequest r : requests.asMap().keySet()) {
            if (r.getRequestedUUID().equals(requested)) {
                denyRequest(r.getRequesterUUID(), r.getRequestedUUID());
            }
        }

        plugin.getTradeManager().newTrade(requesterPlayer, requestedPlayer);
    }

    public void cancelRequests(UUID requester) {
        Iterator<TradeRequest> iterator = requests.asMap().keySet().iterator();

        while (iterator.hasNext()) {
            if (iterator.next().getRequesterUUID().equals(requester)) {
                iterator.next().cancelRequest(true);
            }
        }
    }

    public boolean hasRequest(UUID player) {
        for (TradeRequest request : requests.asMap().keySet()) {
            if (request.getRequestedUUID().equals(player)) return true;
        }
        return false;
    }

    public boolean hasRequest(UUID player, UUID from) {
        for (TradeRequest request : requests.asMap().keySet()) {
            if (request.getRequestedUUID().equals(player) && request.getRequesterUUID().equals(from)) return true;
        }
        return false;
    }

    public void shutdown() {
        for (TradeRequest request : requests.asMap().keySet()) {
            request.cancelRequest(false);
        }
    }
}
