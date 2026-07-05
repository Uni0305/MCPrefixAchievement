package com.github.silvertreekr.mcprefixachievement.listener;

import com.github.silvertreekr.mcprefixachievement.MCPrefixAchievement;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserPrefixManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserStatsManager;
import com.github.silvertreekr.mcprefixachievement.model.Prefix;
import com.github.silvertreekr.mcprefixachievement.model.PrefixStat;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class EntityDeathEventListener implements Listener {
    private final MCPrefixAchievement plugin = MCPrefixAchievement.getInstance();
    private final UserPrefixManager prefixManager = plugin.getUserPrefixManager();
    private final UserStatsManager statsManager = plugin.getUserStatsManager();
    private final PrefixConfigManager prefixConfigManager = plugin.getPrefixConfigManager();

    private void grantPrefix(UUID uuid, int prefixID, Player player) {
        if (prefixID != -1) {
            Prefix prefix = prefixConfigManager.getPrefixById(prefixID);

            if (prefix != null) {
                prefixManager.addPrefix(uuid, prefixID);
                player.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>축하합니다 ! <prefix><reset>을 획득하셨습니다 !", Placeholder.component("prefix", prefix.getDisplayPrefix()));
            }
        }
    }

    public EntityDeathEventListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent event) {
        int prefixID = -1;

        if (event.getEntityType().equals(EntityType.ENDER_DRAGON)) {
            if (event.getEntity().getKiller() != null) {
                UUID uuid = event.getEntity().getKiller().getUniqueId();
                int count = statsManager.getStatValue(uuid, PrefixStat.KILL_ENDER_DRAGON);
                count++;
                statsManager.setStatValue(uuid, PrefixStat.KILL_ENDER_DRAGON, count);
                if (count == 1) {
                    // 용살자
                    prefixID = 6;
                }
                grantPrefix(uuid, prefixID, event.getEntity().getKiller());
            }
        }
    }

    @EventHandler
    public void onEndermandDeath(EntityDeathEvent event) {
        int prefixID = -1;

        if (event.getEntityType().equals(EntityType.ENDERMAN)) {
            if (event.getEntity().getKiller() != null) {
                UUID uuid = event.getEntity().getKiller().getUniqueId();
                Player killer = event.getEntity().getKiller();
                boolean killedWithMace = killer.getInventory().getItemInMainHand().getType().equals(Material.MACE);
                if (killedWithMace) {
                    int count = statsManager.getStatValue(uuid, PrefixStat.KILL_ENDERMAN_BY_MACE);
                    count++;
                    statsManager.setStatValue(uuid, PrefixStat.KILL_ENDERMAN_BY_MACE, count);
                    if (count == 1) {
                        // 망치 나가신다
                        prefixID = 7;
                    }
                }
                grantPrefix(uuid, prefixID, event.getEntity().getKiller());
            }
        }
    }
}
