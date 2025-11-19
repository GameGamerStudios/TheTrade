package com.gamegamerstudios.theTrade.util;

import com.gamegamerstudios.theTrade.Plugin;
import com.gamegamerstudios.theTrade.manager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileUtil {
    public static void copy(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            String[] files = source.list();
            if (files == null) return;
            for (String file : files) {
                if (file.contains("uid.dat")) continue;
                File newSource = new File(source, file);
                File newDestination = new File(destination, file);
                copy(newSource, newDestination);
            }
        } else {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];

            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File child : files) {
                delete(child);
            }
        }

        file.delete();
    }

    public static void deleteExtraFiles(Plugin plugin) {
        File dir = new File(plugin.getDataFolder() + "/activeGameMaps");
        if (!dir.exists()) return;
        if (!dir.isDirectory()) return;
        File[] children = dir.listFiles();
        if (children == null) return;
        for (File child : children) {
            delete(child);
        }
    }

    public static void updateFile(String fileName, int oldVersion, int newVersion, Plugin plugin) {
        File file;
        String optionalPath = "";
        if (fileName.contains("lang")) {
            file = new File(plugin.getDataFolder() + "/lang", fileName);
            optionalPath = "lang/";
        } else {
            file = new File(plugin.getDataFolder(), fileName);
        }
        if (!file.exists()) {
            plugin.saveResource(optionalPath + fileName, false);
            return;
        }

        YamlConfiguration current = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(fileName);
        if (defaultStream == null) {
            Bukkit.getLogger().warning(MessageManager.getMessage("file.update.noresource") + fileName);
            return;
        }

        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));

        boolean changed = false;

        for (String key : defaults.getKeys(true)) {
            if (!current.contains(key)) {
                current.set(key, defaults.get(key));
                Bukkit.getLogger().info(MessageManager.getMessage("file.update.addkey").replace("%key%", key));
                changed = true;
                continue;
            }

            Object defVal = defaults.get(key);
            Object curVal = current.get(key);

            // --- Smart list merge ---
            if (defVal instanceof List<?> && curVal instanceof List<?>) {
                List<Object> merged = mergeLists((List<?>) curVal, (List<?>) defVal);
                if (!merged.equals(curVal)) {
                    current.set(key, merged);
                    Bukkit.getLogger().info(MessageManager.getMessage("file.update.addkey").replace("%key%", key) + fileName);
                    changed = true;
                }
            }
        }

        current.set("fileVersion", newVersion);

        if (changed) {
            try {
                current.save(file);
                Bukkit.getLogger().info(MessageManager.getMessage("file.update.success")
                        .replace("%file%", fileName)
                        .replace("%oldversion%", oldVersion + "")
                        .replace("%newversion%", newVersion + ""));
            } catch (IOException e) {
                Bukkit.getLogger().warning(MessageManager.getMessage("file.update.fail")
                        .replace("%file%", fileName));
                e.printStackTrace();
            }
        } else {
            Bukkit.getLogger().info(MessageManager.getMessage("file.update.skip").replace("%file%", fileName));
        }
    }

    private static List<Object> mergeLists(List<?> currentList, List<?> defaultList) {
        List<Object> merged = new ArrayList<>(currentList);

        for (Object defEntry : defaultList) {
            if (!containsEquivalent(merged, defEntry)) {
                merged.add(defEntry);
            }
        }
        return merged;
    }

    private static boolean containsEquivalent(List<?> list, Object entry) {
        for (Object existing : list) {
            if (Objects.equals(existing, entry)) {
                return true;
            }

            if (existing instanceof Map && entry instanceof Map) {
                if (mapsEqual((Map<?, ?>) existing, (Map<?, ?>) entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean mapsEqual(Map<?, ?> a, Map<?, ?> b) {
        if (a.size() != b.size()) return false;
        for (Object key : a.keySet()) {
            if (!Objects.equals(a.get(key), b.get(key))) return false;
        }
        return true;
    }

    public static void reloadFiles(Plugin plugin) {
        Bukkit.getLogger().info(MessageManager.getMessage("file.reloading"));
        long start = System.currentTimeMillis();

        plugin.reloadConfig();

        long end = System.currentTimeMillis();
        Bukkit.getLogger().info(MessageManager.getMessage("file.reloadingcomplete")
                .replace("%time%", (end - start) + ""));
    }

    public static int getLangFileVersionLatest() { return 0; }

    public static int configVersionLatest() { return 0; }
}
