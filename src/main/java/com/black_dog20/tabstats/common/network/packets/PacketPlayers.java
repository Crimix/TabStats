package com.black_dog20.tabstats.common.network.packets;

import com.black_dog20.bml.network.messages.api.PlayPacket;
import com.black_dog20.tabstats.TabStats;
import com.black_dog20.tabstats.client.ClientDataManager;
import com.black_dog20.tabstats.common.utils.PlayerStat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketPlayers extends PlayPacket<PacketPlayers.Data> {

    public static final ResourceLocation ID = new ResourceLocation(TabStats.MOD_ID, "send_players");

    public static final record Data(Map<UUID, PlayerStat> playerStats) implements CustomPacketPayload {

        public Data(FriendlyByteBuf friendlyByteBuf) {
            this(read(friendlyByteBuf));
        }

        @Override
        public void write(FriendlyByteBuf pBuffer) {
            pBuffer.writeInt(playerStats.size());
            for (Map.Entry<UUID, PlayerStat> playerKvP : playerStats.entrySet()) {
                PlayerStat stat = playerKvP.getValue();
                pBuffer.writeUUID(playerKvP.getKey());
                pBuffer.writeUtf(stat.playerName(), 32767);
                pBuffer.writeInt(stat.deaths());
                pBuffer.writeInt(stat.kills());
                pBuffer.writeInt(stat.playTime());
                pBuffer.writeLong(stat.lastOnline());
            }
        }

        @Override
        public @NotNull ResourceLocation id() {
            return ID;
        }
    }

    private static Map<UUID, PlayerStat> read(FriendlyByteBuf buffer) {
        Map<UUID, PlayerStat> map = new HashMap<>();
        int length = buffer.readInt();
        for (int i = 0; i < length; i++) {
            UUID uuid = buffer.readUUID();
            map.put(uuid, new PlayerStat(uuid, buffer.readUtf(32767), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readLong()));
        }
        return map;
    }


    @Override
    protected void handleClient(PacketPlayers.Data payload, IPayloadContext context) {
        context.workHandler().execute(() -> Handler.handle(payload));
    }

    @Override
    protected void handleServer(PacketPlayers.Data payload, IPayloadContext context) {

    }

    private static final class Handler {
        private static void handle(PacketPlayers.Data payload) {
            ClientDataManager.PLAYER_STATS = payload.playerStats;
        }
    }
}
