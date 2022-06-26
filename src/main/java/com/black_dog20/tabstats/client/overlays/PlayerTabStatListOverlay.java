package com.black_dog20.tabstats.client.overlays;

import com.black_dog20.bml.client.DrawingContext;
import com.black_dog20.bml.client.overlay.GameOverlay;
import com.black_dog20.bml.client.rows.Row;
import com.black_dog20.bml.client.rows.RowHelper;
import com.black_dog20.bml.client.rows.columns.BlankColumn;
import com.black_dog20.bml.client.rows.columns.Column;
import com.black_dog20.bml.client.rows.columns.HeadColumn;
import com.black_dog20.bml.client.rows.columns.ITextComponentColumn;
import com.black_dog20.tabstats.client.ClientDataManager;
import com.black_dog20.tabstats.client.columns.OfflineHeadColumn;
import com.black_dog20.tabstats.client.keybinds.Keybinds;
import com.black_dog20.tabstats.common.utils.PlayerStat;
import com.black_dog20.tabstats.common.utils.Translations;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.black_dog20.tabstats.common.utils.Translations.*;

@OnlyIn(Dist.CLIENT)
public class PlayerTabStatListOverlay extends GameOverlay.PreLayer {

    private static final Ordering<PlayerStat> ENTRY_ORDERING = Ordering.from(new PlayerComparator());
    private final Minecraft minecraft;
    private final Font fontRenderer;
    private final ItemRenderer itemRenderer;
    private long lastRenderTime = Util.getMillis();
    private int ticks = 0;
    private int page = 1;

    public PlayerTabStatListOverlay() {
        this.minecraft = Minecraft.getInstance();
        this.fontRenderer = minecraft.font;
        this.itemRenderer = minecraft.getItemRenderer();
    }

    @Override
    public void onRender(PoseStack matrixStack, int width, int height) {
        int y = 10;
        int z = 0;
        if (Util.getMillis() - 2000 > lastRenderTime) {
            page = 1;
            ticks = 1;
        }
        int itemsPerPage = (int) Math.floor((height - 7 * y) / fontRenderer.lineHeight);
        List<Row> rows = getRows();

        int maxPages = (int) Math.ceil(rows.size() / (double) itemsPerPage);

        if (ticks % 300 == 0) {
            if (page >= maxPages)
                page = 1;
            else
                page++;
            ticks = 1;
        }

        rows = getPagedRows(rows, itemsPerPage);
        int maxWidth = RowHelper.getMaxWidth(rows);
        int x = width / 2 - maxWidth / 2;

        DrawingContext drawingContext = new DrawingContext(matrixStack, width, height, x, y, z, fontRenderer, itemRenderer);
        y = RowHelper.drawRowsWithBackground(drawingContext, rows);
        fontRenderer.drawShadow(matrixStack, PAGE.get(page, maxPages), width / 2 + 2, y + 2, -1);
        ticks++;
        lastRenderTime = Util.getMillis();
    }

    @Override
    public boolean doRender(IIngameOverlay iIngameOverlay) {
        if (iIngameOverlay == ForgeIngameGui.CROSSHAIR_ELEMENT) {
            return Keybinds.SHOW.isDown();
        }

        return false;
    }

    @Override
    public boolean doesCancelEvent() {
        return true;
    }

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
            if (event.isCancelable()) {
                if (Keybinds.SHOW.isDown()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private List<Row> getRows() {
        List<PlayerStat> playerStats = ClientDataManager.PLAYER_STATS.values().stream()
                .filter(playerStat -> playerStat.deaths() != -1)
                .collect(Collectors.toList());
        List<PlayerStat> list = ENTRY_ORDERING.sortedCopy(playerStats);
        ClientPacketListener nethandlerplayclient = this.minecraft.player.connection;
        Collection<PlayerInfo> playerInfos = nethandlerplayclient.getOnlinePlayers();
        List<UUID> onlineUuids = playerInfos.stream()
                .map(PlayerInfo::getProfile)
                .map(GameProfile::getId)
                .collect(Collectors.toList());


        return list.stream()
                .map(playerStat -> buildRow(playerStat, onlineUuids))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private Row buildRow(PlayerStat playerStat, List<UUID> onlineUuids) {
        Row.RowBuilder builder = new Row.RowBuilder();

        Player entityplayer = Minecraft.getInstance().level.getPlayerByUUID(playerStat.uuid());

        return builder
                .withColumn(getHeadColumn(playerStat, onlineUuids), this::isNotLocalOnly)
                .withColumn(ITextComponentColumn.of("name", getPlayerName(playerStat)))
                .withColumn(BlankColumn.of("nameSpace", 6))
                .withColumn(ITextComponentColumn.of("lastSeen", getPlayerLastSeen(playerStat), Column.Alignment.CENTER), this::isNotLocalOnly)
                .withColumn(BlankColumn.of("lastSeenSpace", 6), this::isNotLocalOnly)
                .withColumn(ITextComponentColumn.of("timePlayed", getPlayerTimePlayed(playerStat), Column.Alignment.CENTER))
                .withColumn(BlankColumn.of("timePlayedSpace", 6))
                .withColumn(ITextComponentColumn.of("kill", getPlayerKills(playerStat), Column.Alignment.CENTER))
                .withColumn(BlankColumn.of("killSpace", 6))
                .withColumn(ITextComponentColumn.of("death", getPlayerDeaths(playerStat), Column.Alignment.CENTER))
                .withColumn(BlankColumn.of("deathSpace", 6))
                .withColumn(ITextComponentColumn.of("deathPerHour", getPlayerDeathsPerHour(playerStat), Column.Alignment.CENTER))
                .withColumn(BlankColumn.of("deathPerHourSpace", 6))
                .build();
    }

    private Column getHeadColumn(PlayerStat playerStat, List<UUID> onlineUuids) {
        if (onlineUuids.contains(playerStat.uuid()) && this.minecraft.player.connection.getPlayerInfo(playerStat.uuid()) != null) {
            PlayerInfo playerInfo = this.minecraft.player.connection.getPlayerInfo(playerStat.uuid());
            return HeadColumn.of("head", playerInfo);
        } else {
            return OfflineHeadColumn.of("head", playerStat.uuid(), playerStat.playerName());
        }
    }

    private Row buildTitleRow() {
        Row.RowBuilder builder = new Row.RowBuilder();

        return builder
                .withColumn(BlankColumn.of("head", 10), this::isNotLocalOnly)
                .withColumn(ITextComponentColumn.of("name", getTitleComponent(NAME)))
                .withColumn(BlankColumn.of("nameSpace", 6))
                .withColumn(ITextComponentColumn.of("lastSeen", getTitleComponent(LAST_SEEN), Column.Alignment.CENTER), this::isNotLocalOnly)
                .withColumn(BlankColumn.of("lastSeenSpace", 6), this::isNotLocalOnly)
                .withColumn(ITextComponentColumn.of("timePlayed", getTitleComponent(TIME_PLAYED), Column.Alignment.CENTER))
                .withColumn(BlankColumn.of("timePlayedSpace", 6))
                .withColumn(ITextComponentColumn.of("kill", getTitleComponent(KILLS), Column.Alignment.CENTER))
                .withColumn(BlankColumn.of("killSpace", 6))
                .withColumn(ITextComponentColumn.of("death", getTitleComponent(DEATHS), Column.Alignment.CENTER))
                .withColumn(BlankColumn.of("deathSpace", 6))
                .withColumn(ITextComponentColumn.of("deathPerHour", getTitleComponent(DEATHS_PER_HOUR), Column.Alignment.CENTER))
                .withColumn(BlankColumn.of("deathPerHourSpace", 6))
                .build();
    }

    private boolean isNotLocalOnly() {
        return !Minecraft.getInstance().hasSingleplayerServer();
    }

    private Component getTitleComponent(Translations title) {
        return title.get(ChatFormatting.DARK_GREEN);
    }

    private List<Row> getPagedRows(List<Row> rows, int itemsPerPage) {
        LinkedList<Row> result =  rows.stream()
                .skip((page - 1) * itemsPerPage)
                .limit(itemsPerPage)
                .collect(Collectors.toCollection(LinkedList::new));
       result.addFirst(buildTitleRow());

       return result;
    }

    private Component getPlayerName(PlayerStat playerStat) {
        return Component.literal(playerStat.playerName());
    }

    private Component getPlayerLastSeen(PlayerStat playerStat) {
        if (playerStat.lastOnline() == 0) {
            return NOW.get(ChatFormatting.GREEN);
        } else if (playerStat.lastOnline() < 0) {
            return Component.literal("-").withStyle(ChatFormatting.RED);
        } else {
            return Component.literal(formatTime(playerStat.lastOnline()));
        }
    }

    private Component getPlayerTimePlayed(PlayerStat playerStat) {
        int playTime = playerStat.playTime();

        if (playTime < 0) {
            return Component.literal("-").withStyle(ChatFormatting.RED);
        }

        return Component.literal(formatTime(playTime / 20L));
    }

    private Component getPlayerKills(PlayerStat playerStat) {
        int kills = playerStat.kills();

        if (kills < 0) {
            return Component.literal("-").withStyle(ChatFormatting.RED);
        }

        return Component.literal(Integer.toString(kills));
    }

    private Component getPlayerDeaths(PlayerStat playerStat) {
        int deaths = playerStat.deaths();

        if (deaths < 0) {
            return Component.literal("-").withStyle(ChatFormatting.RED);
        }

        return Component.literal(Integer.toString(deaths));
    }

    private Component getPlayerDeathsPerHour(PlayerStat playerStat) {
        int playTime = playerStat.playTime();
        int deaths = playerStat.deaths();

        if (playTime < 0 || deaths < 0) {
            return Component.literal("-").withStyle(ChatFormatting.RED);
        }

        double hours = ((double) (playTime / 20L) / 3600L);
        if (hours != 0) {
            double deathsPerHour = (double) deaths / hours;
            return Component.literal(deathsPerHour < 0D ? "-" : String.format("%.2f", deathsPerHour));
        } else {
            return Component.literal("-").withStyle(ChatFormatting.RED);
        }

    }

    public static String formatTime(long secs)
    {
        StringBuilder sb = new StringBuilder();
        long days = (secs / 86400L);
        long hours = (secs / 3600L) % 24;
        long minutes = (secs / 60L) % 60L;

        if (days > 0)
        {
            sb.append(days);
            sb.append("d ");
        }

        if (hours > 0 || days > 0)
        {
            if (hours < 10)
            {
                sb.append("0");
            }
            sb.append(hours);
            sb.append("h ");
        }

        if (minutes < 10)
        {
            sb.append("0");
        }
        sb.append(minutes);
        sb.append("m");

        return sb.toString();
    }


    @OnlyIn(Dist.CLIENT)
    static class PlayerComparator implements Comparator<PlayerStat> {

        public int compare(PlayerStat player1, PlayerStat player2) {
            return Comparator.comparing(PlayerStat::getLastOnlineOrNull, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(PlayerStat::playTime).reversed()
                    .thenComparing(PlayerStat::deaths)
                    .thenComparing(PlayerStat::kills).reversed()
                    .compare(player1, player2);
        }
    }
}
