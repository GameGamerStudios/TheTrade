package com.gamegamerstudios.theTrade.api;

import com.gamegamerstudios.theTrade.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TradeTimer extends BukkitRunnable {
    private final int COUNTDOWN_SECONDS;
    private final Trade trade;
    private final Plugin plugin;
    private int timeLeft;
    private boolean countdownBool = false;
    public TradeTimer(Trade trade, Plugin plugin, int seconds) {
        this.trade = trade;
        this.plugin = plugin;
        this.COUNTDOWN_SECONDS = seconds;
        timeLeft = COUNTDOWN_SECONDS;
    }

    public boolean isCountdown() { return countdownBool; }
    public int getTimeLeft() { return timeLeft; }

    public void start() {
        this.runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void run() {
        countdownBool = true;
        if (timeLeft == 0) {
            trade.complete();
            countdownBool = false;
            cancel();
        }
        trade.updateGUI();
        timeLeft--;
    }
}
