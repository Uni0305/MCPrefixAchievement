package com.github.silvertreekr.mcprefixachievement.listener;

import com.github.silvertreekr.mcprefixachievement.MCPrefixAchievement;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserPrefixManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserStatsManager;
import com.github.silvertreekr.mcprefixachievement.model.Prefix;
import com.github.silvertreekr.mcprefixachievement.model.PrefixStat;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerJoinEventListener implements Listener {

    private void grantPrefix(UUID uuid, int prefixID, Player player) {
        MCPrefixAchievement plugin = MCPrefixAchievement.getInstance();
        UserPrefixManager prefixManager = plugin.getUserPrefixManager();
        UserStatsManager statsManager = plugin.getUserStatsManager();
        PrefixConfigManager prefixConfigManager = plugin.getPrefixConfigManager();

        if (prefixID != -1) {
            Prefix prefix = prefixConfigManager.getPrefixById(prefixID);
            if (prefix != null) {
                prefixManager.addPrefix(uuid, prefixID);
                player.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>축하합니다 ! <prefix><reset>을 획득하셨습니다 !", Placeholder.component("prefix", prefix.getDisplayPrefix()));
            }
        }
    }

    public PlayerJoinEventListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MCPrefixAchievement plugin = MCPrefixAchievement.getInstance();
        UserPrefixManager prefixManager = plugin.getUserPrefixManager();
        UserStatsManager statsManager = plugin.getUserStatsManager();
        PrefixConfigManager prefixConfigManager = plugin.getPrefixConfigManager();

        UUID uuid = event.getPlayer().getUniqueId();
        int prefixID = -1;

        // load PlayerPrefixData & PlayerStatsData to each Cache
        prefixManager.loadPlayerPrefixData(uuid);
        statsManager.loadPlayerStatsData(uuid);

        if (statsManager.getStatValue(uuid, PrefixStat.FIRST_JOIN) == 0) {
            statsManager.setStatValue(uuid, PrefixStat.FIRST_JOIN, 1);

            // 첫걸음
            prefixID = 1;
            List<ItemStack> items = new ArrayList<>();
            items.add(new ItemStack(Material.BREAD, 16));
            items.add(new ItemStack(Material.TORCH, 32));
            event.getPlayer().give(items);
        }
        grantPrefix(uuid, prefixID, event.getPlayer());
    }
}
