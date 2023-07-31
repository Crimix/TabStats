package com.black_dog20.tabstats.common.utils;

import com.black_dog20.tabstats.common.events.ServerEvents;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;

import java.util.UUID;

public record PlayerStat(
        UUID uuid,
        String playerName,
        int deaths,
        int kills,
        int playTime,
        long lastOnline,
        int advancementCount) {

    public static PlayerStat from(UUID uuid, String name, StatsCounter statsCounter, boolean isOnline) {

        int deaths = statsCounter.getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int kills = statsCounter.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        int playTime = statsCounter.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
        int advancementCount = statsCounter.getValue(Stats.CUSTOM.get(CustomStats.ADVANCEMENTS_GAINED));
        long lastOnline = isOnline ? 0 : ServerEvents.getLastSeenAdjusted(uuid);

        return new PlayerStat(uuid, name, deaths, kills, playTime, lastOnline, advancementCount);
    }

    public Long getLastOnlineOrNull() {
        if (lastOnline() < 0) {
            return null;
        } else {
            return lastOnline();
        }
    }
}
