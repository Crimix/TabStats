package com.black_dog20.tabstats.client;

import com.black_dog20.bml.client.overlay.OverlayRegistry;
import com.black_dog20.tabstats.TabStats;
import com.black_dog20.tabstats.client.keybinds.Keybinds;
import com.black_dog20.tabstats.client.overlays.PlayerTabStatListOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod.EventBusSubscriber( modid = TabStats.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientStartup {

    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        OverlayRegistry.register(new PlayerTabStatListOverlay());
    }

    @SubscribeEvent
    public static void registerKeyBinding(RegisterKeyMappingsEvent event) {
        event.register(Keybinds.SHOW);
    }
}
