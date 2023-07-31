package com.black_dog20.tabstats.common.network.packets;

import com.black_dog20.tabstats.client.ClientDataManager;
import com.black_dog20.tabstats.common.utils.PlayerStat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketPlayers {

    private final Map<UUID, PlayerStat> playerStats;

    public PacketPlayers(Map<UUID, PlayerStat> playerStats) {
        this.playerStats = playerStats;
    }

    public static void encode(PacketPlayers msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.playerStats.size());
        for (Map.Entry<UUID, PlayerStat> playerKvP : msg.playerStats.entrySet()) {
            PlayerStat stat = playerKvP.getValue();
            buffer.writeUUID(playerKvP.getKey());
            buffer.writeUtf(stat.playerName(), 32767);
            buffer.writeInt(stat.deaths());
            buffer.writeInt(stat.kills());
            buffer.writeInt(stat.playTime());
            buffer.writeLong(stat.lastOnline());
            buffer.writeInt(stat.advancementCount());
        }
    }

    public static PacketPlayers decode(FriendlyByteBuf buffer) {
        Map<UUID, PlayerStat> map = new HashMap<>();
        int length = buffer.readInt();
        for (int i = 0; i < length; i++) {
            UUID uuid = buffer.readUUID();
            map.put(uuid, new PlayerStat(uuid, buffer.readUtf(32767), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readLong(), buffer.readInt()));
        }
        return new PacketPlayers(map);
    }

    public static class Handler {
        public static void handle(PacketPlayers msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientDataManager.PLAYER_STATS = msg.playerStats;
            }));

            ctx.get().setPacketHandled(true);
        }
    }
}
