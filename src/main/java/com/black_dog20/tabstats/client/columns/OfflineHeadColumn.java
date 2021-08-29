package com.black_dog20.tabstats.client.columns;

import com.black_dog20.bml.client.rows.RowDrawingContext;
import com.black_dog20.bml.client.rows.columns.Column;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;

import java.util.Map;
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
            info = new PlayerInfo(new ClientboundPlayerInfoPacket.PlayerUpdate(new GameProfile(uuid, playerName), 0, GameType.SURVIVAL, null));
            info.getSkinLocation();
            playerInfoCache.put(uuid, info);
        } else {
            info = playerInfoCache.get(uuid);
        }

        if (display) {
            RenderSystem.setShaderTexture(0, info.getSkinLocation());
            int l2 = 8;
            int i3 = 8;
            GuiComponent.blit(context.poseStack, (int) context.x, (int) context.y, 8, 8, 8.0F, (float) l2, 8, i3, 64, 64);

            int j3 = 8;
            int k3 = 8;
            GuiComponent.blit(context.poseStack, (int) context.x, (int) context.y, 8, 8, 40.0F, (float) j3, 8, k3, 64, 64);
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
