package com.gamegamerstudios.theTrade.util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern PATTERN =
            Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*([kKmMbBtT]*)");

    public static double parseMoney(String input) {
        if (input == null) return 0;

        input = input.trim().toLowerCase();

        Matcher matcher = PATTERN.matcher(input);

        if (!matcher.matches())
            throw new NumberFormatException("Invalid money format: " + input);

        double base = Double.parseDouble(matcher.group(1));
        String suffix = matcher.group(2);

        double multiplier = 1;

        for (char c : suffix.toCharArray()) {
            switch (c) {
                case 'k': multiplier *= 1_000; break;
                case 'm': multiplier *= 1_000_000; break;
                case 'b': multiplier *= 1_000_000_000; break;
                case 't': multiplier *= 1_000_000_000_000L; break;
            }
        }

        return base * multiplier;
    }

    public static String formatDate(double value) {
        if (value >= 1_000_000_000_000L)
            return String.format("%.2ft", value / 1_000_000_000_000L);
        if (value >= 1_000_000_000)
            return String.format("%.2fb", value / 1_000_000_000);
        if (value >= 1_000_000)
            return String.format("%.2fm", value / 1_000_000);
        if (value >= 1_000)
            return String.format("%.2fk", value / 1_000);

        return String.valueOf((long) value);
    }

    public static int getOpenSlots(Inventory inventory) {
        int openSlots = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                openSlots++;
            }
        }
        return openSlots;
    }

    public static boolean withinRadius(Location loc1, Location loc2, double radius) {
        if (!loc1.getWorld().equals(loc2.getWorld())) return false;

        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();

        return ((dx * dx + dz * dz) <= radius * radius);
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("M/d/yyyy : h:mm a");

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
    public static String formatLocation(Location loc) {
        return String.format(ChatColor.GRAY + "(X:%.2f, Y:%.2f, Z:%.2f, Yaw:%.2f, Pitch:%.2f)",
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );
    }

    public static void giveItem(Player player, ItemStack item) {
        if (Utils.getOpenSlots(player.getInventory()) <= 0) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        player.getInventory().addItem(item);
    }

    public static String formatDouble(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    public static String getVanillaName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "Air";

        String name = item.getType().name().toLowerCase().replace("_", " ");

        String[] words = name.split(" ");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            builder.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1))
                    .append(" ");
        }

        return builder.toString().trim();
    }
}
