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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

public class PlayerDeathEventListener implements Listener {
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

    public PlayerDeathEventListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerAnyDeath(PlayerDeathEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int prefixID = -1;

        int anyDeathCount = statsManager.getStatValue(uuid, PrefixStat.ANY_DEATH_COUNT);
        anyDeathCount++;
        statsManager.setStatValue(uuid, PrefixStat.ANY_DEATH_COUNT, anyDeathCount);

        if (anyDeathCount == 1) {
            // 죽음을 거부하는 자
            prefixID = 3;
        }


        grantPrefix(uuid, prefixID, event.getPlayer());
    }

    @EventHandler
    public void onPlayerLavaDeath(PlayerDeathEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int prefixID = -1;

        if (event.getDamageSource().getDamageType().equals(DamageType.LAVA)) {
            int lavaDeathCount = statsManager.getStatValue(uuid, PrefixStat.LAVA_DEATH_COUNT);
            lavaDeathCount++;
            statsManager.setStatValue(uuid, PrefixStat.LAVA_DEATH_COUNT, lavaDeathCount);

            if (lavaDeathCount == 1) {
                // 라바 치킨
                prefixID = 4;

                ItemStack potion = new ItemStack(Material.POTION);
                PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();

                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20*60*3, 0), true);
                potion.setItemMeta(potionMeta);
                potion.setAmount(1);

                event.getPlayer().give(List.of(potion));
            }
        }
        grantPrefix(uuid, prefixID, event.getPlayer());
    }

    @EventHandler
    public void onPlayerVoidDeath(PlayerDeathEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int prefixID = -1;

        if (event.getDamageSource().getDamageType().equals(DamageType.OUT_OF_WORLD)) {
            int voidDeathCount = statsManager.getStatValue(uuid, PrefixStat.VOID_DEATH_COUNT);
            voidDeathCount++;
            statsManager.setStatValue(uuid, PrefixStat.VOID_DEATH_COUNT, voidDeathCount);

            if (voidDeathCount == 1) {
                // 나는 카이사가 될거야
                prefixID = 5;
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
                if (headMeta != null) {
                    headMeta.setOwningPlayer(event.getPlayer());
                }
                playerHead.setItemMeta(headMeta);
                playerHead.setAmount(1);

                event.getPlayer().give(playerHead);
            }
        }
        grantPrefix(uuid, prefixID, event.getPlayer());
    }

}
