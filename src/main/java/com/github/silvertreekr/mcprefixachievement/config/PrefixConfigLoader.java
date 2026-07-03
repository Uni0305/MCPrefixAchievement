package com.github.silvertreekr.mcprefixachievement.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PrefixConfigLoader {
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public PrefixConfigLoader(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "prefix.yml");
    }

    public void loadPrefixConfig() {
        if (!file.exists()) {
            plugin.saveResource("prefix.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public @NotNull YamlConfiguration getPrefixConfig() {
        return config;
    }

    public void savePrefixConfig() {
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getSLF4JLogger().error("Failed to save prefix.yml", e);
        }
    }
}
