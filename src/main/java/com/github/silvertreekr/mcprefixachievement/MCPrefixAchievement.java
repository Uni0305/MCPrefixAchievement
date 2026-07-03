package com.github.silvertreekr.mcprefixachievement;

import com.github.silvertreekr.mcprefixachievement.command.PrefixCommand;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigLoader;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigManager;
import com.github.silvertreekr.mcprefixachievement.database.MysqlDatabase;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class MCPrefixAchievement extends JavaPlugin {
    private static MCPrefixAchievement instance;
    private MysqlDatabase mysqlDatabase;
    private PrefixConfigLoader prefixConfigLoader;
    private PrefixConfigManager prefixConfigManager;

    public static @NotNull MCPrefixAchievement getInstance() {
        return instance;
    }

    public @NotNull MysqlDatabase getMysqlDatabase() {
        return mysqlDatabase;
    }

    public @NotNull PrefixConfigLoader getPrefixConfigLoader() {
        return prefixConfigLoader;
    }

    public @NotNull PrefixConfigManager getPrefixConfigManager() {
        return prefixConfigManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Initialize the DefaultConfig
        saveDefaultConfig();
        reloadConfig();

        // Initialize the PrefixConfigLoader
        prefixConfigLoader = new PrefixConfigLoader(this);
        prefixConfigLoader.loadPrefixConfig();

        // Initialize the PrefixConfigManager
        prefixConfigManager = new PrefixConfigManager(this);
        prefixConfigManager.readConfig(prefixConfigLoader);

        // Initialize command
        new PrefixCommand(this);

        // Initialize the MySQL Database
        try {
            mysqlDatabase = MysqlDatabase.initialize(this);
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to initialize MySQL database", e);
            getServer().getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        if (mysqlDatabase != null) {
            mysqlDatabase.shutdown();
        }
    }
}
