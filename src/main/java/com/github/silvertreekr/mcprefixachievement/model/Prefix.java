package com.github.silvertreekr.mcprefixachievement.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class Prefix {
    private final Component displayPrefix;
    private final PrefixStat requiredStat;
    private final int requiredStatValue;
    private final Component description;
    private final Component reward;

    private Prefix(@NotNull Component displayPrefix, @NotNull PrefixStat requiredStat, int requiredStatValue,
                   @NotNull Component description, @NotNull Component reward) {
        this.displayPrefix = displayPrefix;
        this.requiredStat = requiredStat;
        this.requiredStatValue = requiredStatValue;
        this.description = description;
        this.reward = reward;
    }

    public static @NotNull Prefix fromBukkitConfig(@NotNull ConfigurationSection config) throws IllegalArgumentException {

        String displayPrefix = config.getString("display-name");
        if (displayPrefix == null) {
            throw new IllegalArgumentException("display-name is null");
        }
        Component deserializedDisplayPrefix = MiniMessage.miniMessage().deserialize(displayPrefix);


        String requiredStatString = config.getString("required-stat");
        if (requiredStatString == null) {
            throw new IllegalArgumentException("required-stat is null");
        }
        PrefixStat requiredStat;
        try {
            requiredStat = PrefixStat.valueOf(requiredStatString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid required-stat: " + requiredStatString);
        }

        int requiredStatValue = config.getInt("required-stat-value", -1);
        if (requiredStatValue < 0) {
            throw new IllegalArgumentException("Invalid required-stat-value: " + requiredStatValue);
        }

        String descriptionString = config.getString("description");
        if (descriptionString == null) {
            throw new IllegalArgumentException("description is null");
        }
        Component description = MiniMessage.miniMessage().deserialize(descriptionString);

        String rewardString = config.getString("reward");
        if (rewardString == null) {
            throw new IllegalArgumentException("reward is null");
        }
        Component reward = MiniMessage.miniMessage().deserialize(rewardString);

        return new Prefix(deserializedDisplayPrefix, requiredStat, requiredStatValue, description, reward);
    }

    public @NotNull Component getDisplayPrefix() {
        return displayPrefix;
    }

    public @NotNull PrefixStat getRequiredStat() {
        return requiredStat;
    }

    public int getRequiredStatValue() {
        return requiredStatValue;
    }

    public @NotNull Component getDescription() {
        return description;
    }

    public @NotNull Component getReward() {
        return reward;
    }
}
