package com.black_dog20.tabstats.client.columns;

import com.black_dog20.bml.client.rows.RowDrawingContext;
import com.black_dog20.bml.client.rows.columns.Column;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OfflineHeadColumn extends Column {

    private static final Map<UUID, PlayerInfo> playerInfoCache = new ConcurrentHashMap<>();

    private final UUID uuid;
    private final String playerName;
    private final boolean display;

    protected OfflineHeadColumn(String id, UUID uuid, String playerName, Alignment alignment) {
        super(id, alignment);
        this.uuid = uuid;
        this.playerName = playerName;
        this.display = Minecraft.getInstance().isLocalServer() || Minecraft.getInstance().getConnection().getConnection().isEncrypted();
    }

    /**
     * Creates a head column from the NetworkPlayerInfo.
     *
     * @param id   the id of the column.
     * @param uuid the uuid of the player.
     * @param playerName the players name.
     * @return a head column
     */
    public static OfflineHeadColumn of(String id, UUID uuid, String playerName) {
        return new OfflineHeadColumn(id, uuid, playerName, Alignment.CENTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        if (display)
            return 10; //Heads are 10 wide
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(RowDrawingContext context) {
        PlayerInfo info;
        if (!playerInfoCache.containsKey(uuid)) {
            Minecraft.getInstance().getCurrentServer();
            boolean flag = Optional.ofNullable(Minecraft.getInstance().getCurrentServer())
                    .map(ServerData::enforcesSecureChat)
                    .orElse(false);
            info = new PlayerInfo(new GameProfile(uuid, playerName), flag);
            info.getSkinLocation();
            playerInfoCache.put(uuid, info);
        } else {
            info = playerInfoCache.get(uuid);
        }

        if (display) {
            ResourceLocation location = info.getSkinLocation();
            RenderSystem.setShaderTexture(0, location);
            int l2 = 8;
            int i3 = 8;
            context.guiGraphics.blit(location, (int) context.x, (int) context.y, 8, 8, 8.0F, (float) l2, 8, i3, 64, 64);

            int j3 = 8;
            int k3 = 8;
            context.guiGraphics.blit(location, (int) context.x, (int) context.y, 8, 8, 40.0F, (float) j3, 8, k3, 64, 64);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        if (display)
            return Minecraft.getInstance().font.lineHeight;
        return 0;
    }
}
