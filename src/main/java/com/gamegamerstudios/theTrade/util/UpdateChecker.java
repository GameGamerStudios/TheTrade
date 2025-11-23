package com.gamegamerstudios.theTrade.util;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;

public class UpdateChecker {
    private final Plugin plugin;
    private final int resourceId;
    private final Duration checkInterval;
    private Instant lastCheckTime;
    private boolean update = false;

    public UpdateChecker(Plugin plugin, int resourceId, Duration checkInterval) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.checkInterval = checkInterval;
        this.lastCheckTime = Instant.MIN;
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Instant currentTime = Instant.now();
            if (Duration.between(lastCheckTime, currentTime).compareTo(checkInterval) < 0) {
                return; // Skip update check if the interval hasn't elapsed yet
            }

            try {
                URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                String latestVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();

                String currentVersion = plugin.getDescription().getVersion();
                if (latestVersion != null && !latestVersion.equalsIgnoreCase(currentVersion) &&
                        Double.parseDouble(latestVersion) > Double.parseDouble(currentVersion)) {
                    plugin.getLogger().info(MessageManager.getMessage("update.available")
                            .replace("%latest%", latestVersion)
                            .replace("%current%", currentVersion)
                            .replace("%url%", "https://www.spigotmc.org/resources/" + resourceId));
                    update = true;
                }
                lastCheckTime = currentTime; // Update the last check time
            } catch (IOException e) {
                Bukkit.getLogger().warning(MessageManager.getMessage("update.checkFailed") + e.getMessage());
            }
        });
    }

    public boolean updateAvailable() { return update; }
}