package com.github.silvertreekr.mcprefixachievement;

import com.github.silvertreekr.mcprefixachievement.command.Debug;
import com.github.silvertreekr.mcprefixachievement.command.HammerOnCommnad;
import com.github.silvertreekr.mcprefixachievement.command.PrefixCommand;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigLoader;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserPrefixManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserPrefixesDAO;
import com.github.silvertreekr.mcprefixachievement.dao.UserStatsDAO;
import com.github.silvertreekr.mcprefixachievement.dao.UserStatsManager;
import com.github.silvertreekr.mcprefixachievement.database.MysqlDatabase;
import com.github.silvertreekr.mcprefixachievement.listener.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class MCPrefixAchievement extends JavaPlugin {
    private static MCPrefixAchievement instance;
    private MysqlDatabase mysqlDatabase;
    private PrefixConfigLoader prefixConfigLoader;
    private PrefixConfigManager prefixConfigManager;
    private UserPrefixManager userPrefixManager;
    private UserStatsManager userStatsManager;

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

    public @NotNull UserPrefixManager getUserPrefixManager() {
        return userPrefixManager;
    }

    public @NotNull UserStatsManager getUserStatsManager() {
        return userStatsManager;
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
        new HammerOnCommnad(this);
        new Debug(this);

        // Initialize the MySQL Database
        try {
            mysqlDatabase = MysqlDatabase.initialize(this);

        } catch (Exception e) {
            getSLF4JLogger().error("Failed to initialize MySQL database", e);
            getServer().getPluginManager().disablePlugin(this);
        }

        UserPrefixesDAO userPrefixesDAO = new UserPrefixesDAO(mysqlDatabase);
        userPrefixesDAO.initialize();

        userPrefixManager = new UserPrefixManager(userPrefixesDAO);

        UserStatsDAO userStatsDAO  = new UserStatsDAO(mysqlDatabase);
        userStatsDAO.initialize();

        userStatsManager = new UserStatsManager(userStatsDAO);

        // Register EventListeners
        new BlockBreakEventListener(this);
        new BlockPlaceEventListener(this);
        new EntityDeathEventListener(this);
        new EntityPickupItemEventListener(this);
        new PlayerDeathEventListener(this);
        new PlayerGetDragonBreathEventListener(this);
        new PlayerJoinEventListener(this);
        new PlayerQuitEventListener(this);
    }

    @Override
    public void onDisable() {
        if (mysqlDatabase != null) {
            mysqlDatabase.shutdown();
        }
    }
}
