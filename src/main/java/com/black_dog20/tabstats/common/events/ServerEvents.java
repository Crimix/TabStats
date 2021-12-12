package com.black_dog20.tabstats.common.events;

import com.black_dog20.bml.utils.file.FileUtil;
import com.black_dog20.tabstats.Config;
import com.black_dog20.tabstats.TabStats;
import com.black_dog20.tabstats.common.network.PacketHandler;
import com.black_dog20.tabstats.common.network.packets.PacketPlayers;
import com.black_dog20.tabstats.common.utils.PlayerStat;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = TabStats.MOD_ID)
public class ServerEvents {

    private static ConcurrentHashMap<String, Long> LAST_SEEN_MAP = new ConcurrentHashMap<>();
    private static Map<UUID, PlayerStat> playerStatMap = new ConcurrentHashMap<>();
    private static int ticks = 0;
    private static int backupTicks = 0;

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.END)
            return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayerCount() == 0){
            ticks = 0;
            return;
        }

        if (ticks % Config.REFRESH_TICKS.get() == 0) {
            Map<UUID, ServerStatsCounter> serverStatsCounterMap = getPlayerStats(server);
            List<UUID> onlinePlayers = server.getPlayerList().getPlayers().stream()
                    .map(ServerPlayer::getUUID)
                    .collect(Collectors.toList());

            for (Map.Entry<UUID, String> playerEntry : UsernameCache.getMap().entrySet()) {
                UUID uuid = playerEntry.getKey();
                String name = playerEntry.getValue();

                if (onlinePlayers.contains(uuid)) {
                    ServerPlayer serverPlayer = server.getPlayerList().getPlayer(uuid);
                    if (serverPlayer != null) {
                        playerStatMap.put(uuid, PlayerStat.from(uuid, name, serverPlayer.getStats(), true));
                    } else {
                        addNotOnlinePlayer(serverStatsCounterMap, uuid, name);
                    }

                } else {
                    addNotOnlinePlayer(serverStatsCounterMap, uuid, name);
                }
            }

            PacketHandler.sendToAll(new PacketPlayers(playerStatMap));

            ticks = 1;
            return;
        }

        ticks++;
    }

    private static void addNotOnlinePlayer(Map<UUID, ServerStatsCounter> serverStatsCounterMap, UUID uuid, String name) {
        if (serverStatsCounterMap.containsKey(uuid)) {
            playerStatMap.put(uuid, PlayerStat.from(uuid, name, serverStatsCounterMap.get(uuid), false));
        } else {
            playerStatMap.put(uuid, new PlayerStat(uuid, name, -1, -1, -1, -1));
        }
    }

    private static Map<UUID, ServerStatsCounter> getPlayerStats(MinecraftServer server) {
        File folder = server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();

        File[] listOfFiles = folder.listFiles();

        return Optional.ofNullable(listOfFiles)
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toConcurrentMap(ServerEvents::getUuidFromFile, file -> new ServerStatsCounter(server, file)));
    }
    private static UUID getUuidFromFile(File file) {
        return UUID.fromString(file.getName().split(".json")[0]);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            ServerPlayer playerEntity = (ServerPlayer) event.getPlayer();
            playerStatMap.put(playerEntity.getUUID(), PlayerStat.from(playerEntity.getUUID(), playerEntity.getGameProfile().getName(), playerEntity.getStats(), true));

            PacketHandler.sendToAll(new PacketPlayers(playerStatMap));
        }
    }

    public static long getLastSeenAdjusted(UUID uuid) {
        if (EffectiveSide.get().isServer()) {
            long lastSeen = LAST_SEEN_MAP.getOrDefault(uuid.toString(), (long) -1);

            if (lastSeen != -1) {
                lastSeen = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - lastSeen;
            }

            return lastSeen;
        } else
            throw new IllegalStateException("Trying to get homes on non server side");
    }

    private static void loadLastSeen(ServerLevel world) {
        File warpDir = FileUtil.getDirRelativeToWorldFolder(world, "/tabstats");
        Type type = new TypeToken<ConcurrentHashMap<String, Long>>() {
        }.getType();
        LAST_SEEN_MAP = FileUtil.load(warpDir, "/lastseen.json", type, ConcurrentHashMap::new);
    }

    public static void saveLastSeen(ServerLevel world) {
        File warpDir = FileUtil.getDirRelativeToWorldFolder(world, "/tabstats");
        Type type = new TypeToken<ConcurrentHashMap<String, Long>>() {
        }.getType();
        FileUtil.save(warpDir, "/lastseen.json", LAST_SEEN_MAP, type);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            ServerPlayer playerEntity = (ServerPlayer) event.getPlayer();
            LAST_SEEN_MAP.put(playerEntity.getUUID().toString(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            if (playerEntity.getServer() != null) {
                saveLastSeen(playerEntity.getServer().overworld());
            }

            PacketHandler.sendToAll(new PacketPlayers(playerStatMap));
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        try {
            ServerLevel world = event.getServer().overworld();
            loadLastSeen(world);
        } catch (Exception e) {
            TabStats.LOGGER.error(e.getMessage());
        }
    }

    @SubscribeEvent
    public static void onServerShutdown(ServerStoppingEvent event) {
        try {
            ServerLevel world = event.getServer().overworld();
            saveLastSeen(world);
        } catch (Exception e) {
            TabStats.LOGGER.error(e.getMessage());
        }
    }

    @SubscribeEvent
    public static void onBackupTick(TickEvent.ServerTickEvent event) {
        if(event.phase == TickEvent.Phase.END)
            return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server.getPlayerList().getPlayerCount() == 0){
            backupTicks = 0;
            return;
        }

        if (backupTicks % 6000 == 0) {
            ServerLevel world = server.overworld();
            saveLastSeen(world);
            backupTicks = 1;
            return;
        }

        backupTicks++;
    }
}
