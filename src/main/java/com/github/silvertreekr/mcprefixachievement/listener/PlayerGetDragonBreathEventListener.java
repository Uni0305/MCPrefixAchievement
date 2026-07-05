package com.github.silvertreekr.mcprefixachievement.listener;

import com.github.silvertreekr.mcprefixachievement.MCPrefixAchievement;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserPrefixManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserStatsManager;
import com.github.silvertreekr.mcprefixachievement.model.Prefix;
import com.github.silvertreekr.mcprefixachievement.model.PrefixStat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerGetDragonBreathEventListener implements Listener {
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

    private int countDragonBreath(Player player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.DRAGON_BREATH) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    public PlayerGetDragonBreathEventListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        EquipmentSlot hand = event.getHand();
        if (hand == null) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemBefore = player.getInventory().getItem(hand);
        if (itemBefore == null || !itemBefore.getType().equals(Material.GLASS_BOTTLE)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        int dragonBreathBefore = countDragonBreath(player);


        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            int dragonBreathAfter = countDragonBreath(player);
            if (dragonBreathAfter <= dragonBreathBefore) {
                return;
            }

            int count = statsManager.getStatValue(uuid, PrefixStat.GET_DRAGON_BREATH);
            count++;
            statsManager.setStatValue(uuid, PrefixStat.GET_DRAGON_BREATH, count);

            int prefixID = -1;
            if (count == 1) {
                prefixID = 11;
                ItemStack dragonBreath = new ItemStack(Material.DRAGON_BREATH);
                ItemMeta itemMeta = dragonBreath.getItemMeta();
                itemMeta.customName(Component.text("용의 콧물").decoration(TextDecoration.ITALIC, false));
                dragonBreath.setItemMeta(itemMeta);
                dragonBreath.setAmount(1);

                player.give(dragonBreath);
            }
            grantPrefix(uuid, prefixID, player);
        });
    }
}