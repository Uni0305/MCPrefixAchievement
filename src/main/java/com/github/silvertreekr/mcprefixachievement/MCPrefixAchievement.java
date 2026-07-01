package com.github.silvertreekr.mcprefixachievement;
import com.github.silvertreekr.mcprefixachievement.customconfig.PrefixConfigLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.silvertreekr.mcprefixachievement.database.MysqlDatabase;
import org.jetbrains.annotations.NotNull;

public final class MCPrefixAchievement extends JavaPlugin {
    private static MCPrefixAchievement instance;
    public static MCPrefixAchievement getInstance() {
        return instance;
    }

    private static @NotNull MysqlDatabase mysqlDatabase;
    public static @NotNull MysqlDatabase getMysqlDatabase() {
        return mysqlDatabase;
    }


    private static PrefixConfigLoader prefixConfigLoader = new PrefixConfigLoader();
    public static PrefixConfigLoader getPrefixConfigLoader() {
        return prefixConfigLoader;
    }

    @Override
    public void onEnable() {
        new PrefixCommand(this);


        // Initialize the DefaultConfig
        saveDefaultConfig();
        reloadConfig();

        // Initialize the PrefixConfigLoader

        prefixConfigLoader.loadPrefixConfig();

        // Initialize the MySQL Database
        try {
            mysqlDatabase = MysqlDatabase.initialize(this);
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to initialize MySQL database", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
