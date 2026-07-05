package com.github.silvertreekr.mcprefixachievement.listener;

import com.github.silvertreekr.mcprefixachievement.MCPrefixAchievement;
import com.github.silvertreekr.mcprefixachievement.dao.UserStatsManager;
import com.github.silvertreekr.mcprefixachievement.model.PrefixStat;
import com.github.silvertreekr.mcprefixachievement.util.PrefixGranter;
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
    private final UserStatsManager statsManager = plugin.getUserStatsManager();

    public BlockBreakEventListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerBreakAnyBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
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
        PrefixGranter.grantPrefix(player, prefixID);
    }

    @EventHandler
    public void onPlayerBreakDiamondOre(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
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
        PrefixGranter.grantPrefix(player, prefixID);
    }
}
