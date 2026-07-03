package com.github.silvertreekr.mcprefixachievement.config;

import com.github.silvertreekr.mcprefixachievement.model.Prefix;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class PrefixConfigManager {
    private final JavaPlugin plugin;
    private final SortedMap<Integer, Prefix> prefixMap = new TreeMap<>();

    public PrefixConfigManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void readConfig(@NotNull PrefixConfigLoader loader) {
        YamlConfiguration config = loader.getPrefixConfig();
        for (String key : config.getKeys(false)) {
            try {
                int index = Integer.parseInt(key);
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section != null) {
                    prefixMap.put(index, Prefix.fromBukkitConfig(section));
                }
            } catch (NumberFormatException e) {
                plugin.getSLF4JLogger().warn("Invalid prefix key (not a number): {}, skipping", key);
            } catch (IllegalArgumentException e) {
                plugin.getSLF4JLogger().warn("Failed to load prefix '{}': {}", key, e.getMessage());
            }
        }
    }

    public void reloadConfig(@NotNull PrefixConfigLoader loader) {
        loader.loadPrefixConfig();
        prefixMap.clear();
        readConfig(loader);
    }

    public @NotNull SortedMap<Integer, Prefix> getPrefixMap() {
        return Collections.unmodifiableSortedMap(prefixMap);
    }

    public @Nullable Prefix getPrefixById(int id) {
        return prefixMap.get(id);
    }

    public @NotNull Set<Component> getAllDisplayPrefixes() {
        return prefixMap.values().stream()
                .map(Prefix::getDisplayPrefix)
                .collect(Collectors.toSet());
    }
}
