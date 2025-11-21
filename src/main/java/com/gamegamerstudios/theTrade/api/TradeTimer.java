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

    public boolean isRunning() { return countdownBool; }
    public int getTimeLeft() { return timeLeft; }

    public void start() {
        this.runTaskTimer(plugin, 0, 20);
    }
    public void end() {
        cancel();
        timeLeft = COUNTDOWN_SECONDS;
        countdownBool = false;
    }

    @Override
    public void run() {
        countdownBool = true;
        if (timeLeft == 0) {
            countdownBool = false;
            trade.complete();
            cancel();
            return;
        }
        trade.updateGUI();
        timeLeft--;
    }
}
