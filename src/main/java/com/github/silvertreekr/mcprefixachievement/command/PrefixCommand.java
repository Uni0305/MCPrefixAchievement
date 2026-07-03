package com.github.silvertreekr.mcprefixachievement.command;

import com.github.silvertreekr.mcprefixachievement.MCPrefixAchievement;
import com.github.silvertreekr.mcprefixachievement.config.PrefixConfigManager;
import com.github.silvertreekr.mcprefixachievement.model.Prefix;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.SortedMap;

public class PrefixCommand extends BukkitCommand {
    public PrefixCommand(@NotNull JavaPlugin plugin) {
        super("칭호");
        plugin.getServer().getCommandMap().register("mcprefixachievement", this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        // /칭호 정보 [칭호명] -> 특정 칭호의 달성 조건을 보는 커맨드
        // /칭호 목록 [페이지] -> 칭호들의 목록을 보는 커맨드 (15개씩 끊어서 페이지)
        // /칭호 -> 칭호 명령어들 반환
        if (args.length == 0) {
            sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>사용 가능한 명령어:");
            sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>/칭호 정보 [칭호명] - 특정 칭호의 정보를 확인합니다.");
            sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>/칭호 목록 [페이지] - 칭호들의 목록을 확인합니다.");

            return true;
        }

        PrefixConfigManager prefixConfigManager = MCPrefixAchievement.getInstance().getPrefixConfigManager();
        switch (args[0]) {
            case "정보" -> {
                if (args.length == 1) {
                    sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>사용법: /칭호 정보 [칭호명]");
                    return true;
                }
                if (args.length == 2) {
                    try {
                        int prefixId = Integer.parseInt(args[1]);
                        Prefix prefix = prefixConfigManager.getPrefixById(prefixId);

                        if (prefix == null) {
                            sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset><red>올바르지 않은 칭호 ID입니다.");
                            return false;
                        }
                        sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>칭호: <prefix>", Placeholder.component("prefix", prefix.getDisplayPrefix()));
                        sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>달성 조건: <description>", Placeholder.component("description", prefix.getDescription()));
                        sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>달성 보상: <reward>", Placeholder.component("reward", prefix.getReward()));
                    } catch (NumberFormatException e) {
                        sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset><red>올바르지 않은 칭호 ID입니다.");
                        return false;
                    }
                }
            }
            case "목록" -> {
                SortedMap<Integer, Prefix> prefixMap = prefixConfigManager.getPrefixMap();
                if (args.length == 1) {
                    int index = 1;
                    for (Map.Entry<Integer, Prefix> entry : prefixMap.entrySet()) {
                        if (index > 15) break;
                        sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>%d. <prefix>".formatted(entry.getKey()), Placeholder.component("prefix", entry.getValue().getDisplayPrefix()));
                        index++;
                    }
                    return true;
                }
                if (args.length == 2) {
                    try {
                        int page = Integer.parseInt(args[1]);
                        int pageSize = 15;
                        int skip = (page - 1) * pageSize;
                        if (skip >= prefixMap.size()) {
                            sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset><red>해당 페이지에는 칭호가 존재하지 않습니다.");
                            return false;
                        }
                        int shown = 0;
                        for (Map.Entry<Integer, Prefix> entry : prefixMap.entrySet()) {
                            if (shown >= skip) {
                                sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset>%d. <prefix>".formatted(entry.getKey()), Placeholder.component("prefix", entry.getValue().getDisplayPrefix()));
                            }
                            shown++;
                            if (shown >= skip + pageSize) break;
                        }
                        return true;
                    } catch (NumberFormatException e) {
                        sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset><red>올바르지 않은 페이지 번호입니다.");
                        return false;
                    }
                }
            }
            default -> {
                sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset><red>올바르지 않은 사용법입니다.");
                sender.sendRichMessage("<bold>[ 칭호 시스템 ] <reset><red>올바른 사용법: /칭호 정보 [칭호명] 또는 /칭호 목록 [페이지]");
                return false;
            }
        }
        return false;
    }
}
