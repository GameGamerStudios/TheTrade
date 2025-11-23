package com.gamegamerstudios.theTrade.util;

import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.lang.reflect.Method;

public final class InventoryViewCompact {
    private static final Method GET_TITLE;
    private static final Method GET_TOP_INVENTORY;

    static {
        try {
            Class<?> viewClass = Class.forName("org.bukkit.inventory.InventoryView");
            GET_TITLE = viewClass.getMethod("getTitle");
            GET_TOP_INVENTORY = viewClass.getMethod("getTopInventory");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize InventoryViewCompact", e);
        }
    }

    private InventoryViewCompact() {}

    public static String getTitle(InventoryEvent event) {
        try {
            Object view = event.getView();
            return (String) GET_TITLE.invoke(view);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get inventory title", e);
        }
    }

    public static Inventory getTopInventory(InventoryEvent event) {
        try {
            Object view = event.getView();
            return (Inventory) GET_TOP_INVENTORY.invoke(view);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get top inventory", e);
        }
    }
}
