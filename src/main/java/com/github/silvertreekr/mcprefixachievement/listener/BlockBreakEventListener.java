package com.github.silvertreekr.mcprefixachievement.listener;

import com.github.silvertreekr.mcprefixachievement.MCPrefixAchievement;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserPrefixManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserStatsManager;
import com.github.silvertreekr.mcprefixachievement.model.Prefix;
import com.github.silvertreekr.mcprefixachievement.model.PrefixStat;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.UUID;

public class BlockBreakEventListener implements Listener {
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

    public BlockBreakEventListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerBreakAnyBlock(BlockBreakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int prefixID = -1;

        int count = statsManager.getStatValue(uuid, PrefixStat.BREAK_BLOCK);
        count++;
        statsManager.setStatValue(uuid, PrefixStat.BREAK_BLOCK, count);
        if (count == 10000) {
            // 전문 노가다꾼
            prefixID = 9;
            ItemStack diamondShovel = new ItemStack(Material.DIAMOND_SHOVEL);
            ItemMeta itemMeta = diamondShovel.getItemMeta();
            itemMeta.addEnchant(Enchantment.UNBREAKING, 3, false);
            diamondShovel.setItemMeta(itemMeta);
            diamondShovel.setAmount(1);

            event.getPlayer().give(List.of(diamondShovel));
        }
    }

    @EventHandler
    public void onPlayerBreakDiamondOre(BlockBreakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int prefixID = -1;

        if (event.getBlock().getType().equals(Material.DIAMOND_ORE) || event.getBlock().getType().equals(Material.DEEPSLATE_DIAMOND_ORE)) {
            int count = statsManager.getStatValue(uuid, PrefixStat.BREAK_DIAMOND_ORE);
            count++;
            statsManager.setStatValue(uuid, PrefixStat.BREAK_DIAMOND_ORE, count);
            if (count == 1) {
                // 보석 수집가
                prefixID = 2;
                PotionEffect potionEffect = new PotionEffect(PotionEffectType.HASTE, 20*60*5, 0);
                event.getPlayer().addPotionEffect(potionEffect);
            }
        }
        grantPrefix(uuid, prefixID, event.getPlayer());
    }
}
