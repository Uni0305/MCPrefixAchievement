package com.github.silvertreekr.mcprefixachievement.util;

import com.github.silvertreekr.mcprefixachievement.MCPrefixAchievement;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigManager;
import com.github.silvertreekr.mcprefixachievement.dao.UserPrefixManager;
import com.github.silvertreekr.mcprefixachievement.model.Prefix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PrefixGranter {
    private PrefixGranter() {

    }
    public static void grantPrefix(Player player, int prefixID) {
        MCPrefixAchievement plugin = MCPrefixAchievement.getInstance();
        PrefixConfigManager prefixConfigManager = plugin.getPrefixConfigManager();
        UserPrefixManager prefixManager = plugin.getUserPrefixManager();
        UUID uuid = player.getUniqueId();
        Prefix prefix = prefixConfigManager.getPrefixById(prefixID);

        if (prefix == null) {
            return;
        }
        if (prefixID == -1) {
            return;
        }

        prefixManager.addPrefix(uuid, prefixID);
        player.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>축하합니다 ! <prefix><reset>을 획득하셨습니다 !", Placeholder.component("prefix", prefix.getDisplayPrefix()));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    public static void broadcastPrefix(Player player, int prefixID) {
        MCPrefixAchievement plugin = MCPrefixAchievement.getInstance();
        PrefixConfigManager prefixConfigManager = plugin.getPrefixConfigManager();
        Prefix prefix = prefixConfigManager.getPrefixById(prefixID);

        if (prefix == null) {
            return;
        }

        if (prefixID == -1) {
            return;
        }

        Component playerName = Component.text(player.getName());
        Component message = MiniMessage.miniMessage().deserialize(
                "<bold>[ 칭호 시스템 ] <reset><green><player><reset>님께서 <prefix><reset>을 획득하셨습니다 !"
                ,Placeholder.component("player", playerName)
                ,Placeholder.component("prefix", prefix.getDisplayPrefix())
        );
        Bukkit.broadcast(message);
    }
}
