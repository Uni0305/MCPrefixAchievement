package com.github.silvertreekr.mcprefixachievement.customconfig;

import com.github.silvertreekr.mcprefixachievement.MCPrefixAchievement;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class PrefixConfigLoader {
    private final File file = new File(MCPrefixAchievement.getInstance().getDataFolder(), "prefix.yml");
    private YamlConfiguration config;

    public void loadPrefixConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public YamlConfiguration getPrefixConfig() {
        return config;
    }

    public void savePrefixConfig() {
        try {
            config.save(file);
        } catch (Exception e) {
            MCPrefixAchievement.getInstance().getSLF4JLogger().error("Failed to save prefixes.yml", e);
        }
    }
}
