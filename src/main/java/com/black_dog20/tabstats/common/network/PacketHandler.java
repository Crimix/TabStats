package com.black_dog20.tabstats.common.network;

import com.black_dog20.tabstats.TabStats;
import com.black_dog20.tabstats.common.network.packets.PacketPlayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

@Mod.EventBusSubscriber(modid = TabStats.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PacketHandler {

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(TabStats.MOD_ID)
				.versioned("1.0.0")
				.play(PacketPlayers.ID, PacketPlayers.Data::new, new PacketPlayers().handlers());
	}
}
