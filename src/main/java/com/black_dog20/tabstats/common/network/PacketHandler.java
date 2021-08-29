package com.black_dog20.tabstats.common.network;

import com.black_dog20.tabstats.TabStats;
import com.black_dog20.tabstats.common.network.packets.PacketPlayers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {

	private static final String PROTOCOL_VERSION = Integer.toString(1);
	private static short index = 0;

	public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(TabStats.MOD_ID, "network"))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();

	public static void register() {
        registerMessage(PacketPlayers.class, PacketPlayers::encode, PacketPlayers::decode, PacketPlayers.Handler::handle);
	}

	public static void sendTo(Object msg, ServerPlayer player) {
		if (!(player instanceof FakePlayer))
			NETWORK.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendToAll(Object msg, Level world) {
		for (Player player : world.players()) {
			if (!(player instanceof FakePlayer))
				NETWORK.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public static void sendToAll(Object msg) {
		NETWORK.send(PacketDistributor.ALL.noArg(), msg);
	}

	public static void sendToServer(Object msg) {
		NETWORK.sendToServer(msg);
	}

	private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
		NETWORK.registerMessage(index, messageType, encoder, decoder, messageConsumer);
		index++;
		if (index > 0xFF)
			throw new RuntimeException("Too many messages!");
	}
}
